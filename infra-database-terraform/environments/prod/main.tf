# ================================
# Data Source - VPC from infra-kubernetes
# ================================
# OPÇÃO 1: Usar remote state local (desenvolvimento)
data "terraform_remote_state" "vpc" {
  backend = "local"
  config = {
    path = "../../../infra-kubernetes-terraform/environments/prod/terraform.tfstate"
  }
}

# OPÇÃO 2: Usar remote state S3 (produção - descomente quando criar bucket)
# data "terraform_remote_state" "vpc" {
#   backend = "s3"
#   config = {
#     bucket = "fiap-oficina-terraform-state"
#     key    = "infra-kubernetes/prod/terraform.tfstate"
#     region = "us-east-1"
#   }
# }

# ================================
# Local Variables
# ================================
locals {
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
    Repository  = "infra-database-terraform"
  }

  # VPC data from remote state
  vpc_id             = data.terraform_remote_state.vpc.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.vpc.outputs.private_subnet_ids
  availability_zones = data.terraform_remote_state.vpc.outputs.availability_zones
}

# ================================
# RDS Module
# ================================
module "rds" {
  source = "../../modules/rds"

  # Required Parameters
  project_name = var.project_name
  environment  = var.environment

  # Database Configuration
  db_name               = var.db_name
  db_engine             = "postgres"
  db_engine_version     = var.db_engine_version
  db_instance_class     = var.db_instance_class
  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = var.db_max_allocated_storage
  storage_type          = var.db_storage_type
  storage_encrypted     = true
  kms_key_id            = aws_kms_key.rds.arn

  # Multi-AZ
  multi_az = var.multi_az

  # Credentials
  db_username = var.db_username
  db_password = random_password.db_password.result

  # Networking
  vpc_id               = local.vpc_id
  db_subnet_group_name = aws_db_subnet_group.rds.name
  security_group_ids   = [aws_security_group.rds.id]

  # Backup
  backup_retention_period   = var.backup_retention_period
  backup_window             = var.backup_window
  maintenance_window        = var.maintenance_window
  skip_final_snapshot       = var.environment == "dev" ? true : false
  final_snapshot_identifier = var.environment != "dev" ? "${var.db_name}-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}" : null
  copy_tags_to_snapshot     = true

  # Enhanced Monitoring
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]
  monitoring_interval             = var.enable_enhanced_monitoring ? var.monitoring_interval : 0
  monitoring_role_arn             = var.enable_enhanced_monitoring ? aws_iam_role.rds_enhanced_monitoring[0].arn : null

  # Performance Insights
  performance_insights_enabled          = var.enable_performance_insights
  performance_insights_retention_period = var.enable_performance_insights ? 7 : null
  performance_insights_kms_key_id       = var.enable_performance_insights ? aws_kms_key.rds.arn : null

  # IAM Database Authentication
  iam_database_authentication_enabled = var.enable_iam_auth

  # Parameter Group
  parameter_group_name = aws_db_parameter_group.postgres.name

  # Auto Minor Version Upgrade
  auto_minor_version_upgrade = true

  # Deletion Protection
  deletion_protection = var.environment == "prod" ? true : false

  # Tags
  tags = local.common_tags

  depends_on = [aws_db_subnet_group.rds, aws_security_group.rds]
}

# ================================
# DB Subnet Group
# ================================
resource "aws_db_subnet_group" "rds" {
  name       = "${var.project_name}-${var.environment}-rds-subnet-group"
  subnet_ids = local.private_subnet_ids

  tags = merge(
    local.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-subnet-group"
    }
  )
}

# ================================
# Security Group
# ================================
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${var.environment}-rds-sg"
  description = "Security group for RDS PostgreSQL"
  vpc_id      = local.vpc_id

  tags = merge(
    local.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-sg"
    }
  )
}

# Ingress rule - ECS Security Group
resource "aws_security_group_rule" "rds_from_ecs" {
  count = var.ecs_security_group_id != "" ? 1 : 0

  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = var.ecs_security_group_id
  description              = "PostgreSQL from ECS"
}

# Ingress rule - Lambda Security Group
resource "aws_security_group_rule" "rds_from_lambda" {
  count = var.lambda_security_group_id != "" ? 1 : 0

  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = var.lambda_security_group_id
  description              = "PostgreSQL from Lambda"
}

# Ingress rule - EKS Security Group
resource "aws_security_group_rule" "rds_from_eks" {
  count = var.eks_security_group_id != "" ? 1 : 0

  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = var.eks_security_group_id
  description              = "PostgreSQL from EKS"
}

# Egress rule - Allow all outbound
resource "aws_security_group_rule" "rds_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.rds.id
  description       = "Allow all outbound"
}

# ================================
# KMS Key for RDS Encryption
# ================================
resource "aws_kms_key" "rds" {
  description             = "KMS key for RDS encryption - ${var.environment}"
  deletion_window_in_days = 10
  enable_key_rotation     = true

  tags = merge(
    local.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-kms"
    }
  )
}

resource "aws_kms_alias" "rds" {
  name          = "alias/${var.project_name}-${var.environment}-rds"
  target_key_id = aws_kms_key.rds.key_id
}

# ================================
# DB Parameter Group
# ================================
resource "aws_db_parameter_group" "postgres" {
  name   = "${var.project_name}-${var.environment}-postgres15"
  family = "postgres15"

  parameter {
    name  = "log_connections"
    value = "1"
  }

  parameter {
    name  = "log_disconnections"
    value = "1"
  }

  parameter {
    name  = "log_statement"
    value = "ddl"
  }

  parameter {
    name  = "log_min_duration_statement"
    value = "1000" # Log queries slower than 1 second
  }

  parameter {
    name  = "shared_preload_libraries"
    value = "pg_stat_statements"
  }

  parameter {
    name  = "rds.force_ssl"
    value = "1" # Force SSL connections
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-postgres15"
    }
  )
}

# ================================
# Random Password for DB
# ================================
resource "random_password" "db_password" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# ================================
# Secrets Manager for DB Credentials
# ================================
module "secrets" {
  source = "../../modules/secrets"

  secret_name = "${var.project_name}-${var.environment}-rds-credentials"
  description = "RDS PostgreSQL credentials for ${var.environment}"

  secret_value = jsonencode({
    username = var.db_username
    password = random_password.db_password.result
    engine   = "postgres"
    host     = module.rds.endpoint
    port     = module.rds.port
    dbname   = var.db_name
  })

  # Rotation
  enable_rotation = var.enable_secret_rotation
  rotation_days   = 30

  tags = local.common_tags

  depends_on = [module.rds]
}

# ================================
# IAM Role for Enhanced Monitoring
# ================================
resource "aws_iam_role" "rds_enhanced_monitoring" {
  count = var.enable_enhanced_monitoring ? 1 : 0

  name = "${var.project_name}-${var.environment}-rds-monitoring-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "monitoring.rds.amazonaws.com"
        }
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "rds_enhanced_monitoring" {
  count = var.enable_enhanced_monitoring ? 1 : 0

  role       = aws_iam_role.rds_enhanced_monitoring[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}

# ================================
# CloudWatch Alarms
# ================================
resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
  alarm_name          = "${var.project_name}-${var.environment}-rds-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors RDS CPU utilization"
  treat_missing_data  = "notBreaching"

  dimensions = {
    DBInstanceIdentifier = module.rds.db_instance_id
  }

  tags = local.common_tags
}

resource "aws_cloudwatch_metric_alarm" "rds_storage" {
  alarm_name          = "${var.project_name}-${var.environment}-rds-storage-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "2000000000" # 2 GB in bytes
  alarm_description   = "This metric monitors RDS free storage space"
  treat_missing_data  = "notBreaching"

  dimensions = {
    DBInstanceIdentifier = module.rds.db_instance_id
  }

  tags = local.common_tags
}

resource "aws_cloudwatch_metric_alarm" "rds_connections" {
  alarm_name          = "${var.project_name}-${var.environment}-rds-connections-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors RDS database connections"
  treat_missing_data  = "notBreaching"

  dimensions = {
    DBInstanceIdentifier = module.rds.db_instance_id
  }

  tags = local.common_tags
}
