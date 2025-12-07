# ================================
# Data Source - RDS from infra-database
# ================================
# TEMPORARIAMENTE DESABILITADO - Deploy infra-kubernetes primeiro, depois infra-database
# OPÇÃO 1: Usar remote state local (desenvolvimento)
# data "terraform_remote_state" "database" {
#   backend = "local"
#   config = {
#     path = "../../../infra-database-terraform/environments/staging/terraform.tfstate"
#   }
# }

# OPÇÃO 2: Usar remote state S3 (recomendado para staging/prod)
# data "terraform_remote_state" "database" {
#   backend = "s3"
#   config = {
#     bucket = "fiap-oficina-terraform-state"
#     key    = "infra-database/staging/terraform.tfstate"
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
    Repository  = "infra-kubernetes-terraform"
  }

  # RDS data from remote state - TEMPORARIAMENTE DESABILITADO
  # rds_endpoint              = data.terraform_remote_state.database.outputs.rds_endpoint
  # rds_port                  = data.terraform_remote_state.database.outputs.rds_port
  # rds_database_name         = data.terraform_remote_state.database.outputs.rds_database_name
  # db_credentials_secret_arn = data.terraform_remote_state.database.outputs.db_credentials_secret_arn

  # app_secrets = {
  #   DB_USERNAME = "${local.db_credentials_secret_arn}:username::"
  #   DB_PASSWORD = "${local.db_credentials_secret_arn}:password::"
  #   DB_HOST     = "${local.db_credentials_secret_arn}:host::"
  #   DB_PORT     = "${local.db_credentials_secret_arn}:port::"
  #   DB_NAME     = "${local.db_credentials_secret_arn}:dbname::"
  # }
}

# ================================
# VPC Module
# ================================
module "vpc" {
  source = "../../modules/vpc"

  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
  environment        = var.environment
  project_name       = var.project_name
  aws_region         = var.aws_region

  # Subnets
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs

  # NAT Gateway (HA para staging)
  enable_nat_gateway = true
  single_nat_gateway = false

  # VPC Endpoints
  enable_s3_endpoint             = true
  enable_ecr_endpoint            = true
  enable_logs_endpoint           = true
  enable_secretsmanager_endpoint = true

  tags = local.common_tags
}

# ================================
# ECR Repository Module
# ================================
module "ecr" {
  source = "../../modules/ecr"

  repository_name      = var.ecr_repository_name
  image_tag_mutability = "MUTABLE"
  scan_on_push         = true

  enable_lifecycle_policy = true
  lifecycle_keep_count    = 15  # Mais imagens para staging
  lifecycle_untagged_days = 7

  tags = local.common_tags
}

# ================================
# JWT Secret for Lambda Auth
# ================================
module "jwt_secret" {
  source = "../../modules/secrets"

  secret_name = "${var.project_name}-${var.environment}-jwt-secret"
  description = "JWT Secret Key for Lambda Authentication - Staging"

  secret_string = jsonencode({
    JWT_SECRET = var.jwt_secret_key
  })

  recovery_window_in_days = 7

  tags = local.common_tags
}

# ================================
# Security Group for ALB
# ================================
resource "aws_security_group" "alb" {
  name        = "${var.project_name}-${var.environment}-alb-sg"
  description = "Security group for Application Load Balancer - Staging"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description = "HTTP from internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS from internet"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow all outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-alb-sg"
    }
  )

  depends_on = [module.vpc]
}

# ================================
# Application Load Balancer Module
# ================================
module "alb" {
  source = "../../modules/alb"

  alb_name        = "${var.project_name}-${var.environment}-alb"
  internal        = false
  security_groups = [aws_security_group.alb.id]
  subnets         = module.vpc.public_subnet_ids
  vpc_id          = module.vpc.vpc_id

  # Target Group
  target_group_name = "${var.project_name}-${var.environment}-tg"
  target_port       = var.container_port
  target_protocol   = "HTTP"

  # Health Check
  health_check_path                = "/actuator/health"
  health_check_interval            = 30
  health_check_timeout             = 5
  health_check_healthy_threshold   = 2
  health_check_unhealthy_threshold = 3
  health_check_matcher             = "200"

  # HTTPS (recomendado para staging)
  enable_https    = var.enable_https
  certificate_arn = var.acm_certificate_arn
  ssl_policy      = "ELBSecurityPolicy-TLS-1-2-2017-01"

  # Settings
  enable_deletion_protection = false  # Pode ser true para staging
  enable_http2               = true
  idle_timeout               = 60

  tags = local.common_tags

  depends_on = [module.vpc]
}

# ================================
# ECS Fargate Module
# ================================
module "ecs" {
  source = "../../modules/ecs"

  cluster_name    = "${var.project_name}-${var.environment}-cluster"
  service_name    = "${var.project_name}-${var.environment}-service"
  container_name  = var.container_name
  container_image = var.container_image != "" ? var.container_image : "${module.ecr.repository_url}:${var.image_tag}"
  container_port  = var.container_port

  # Task Configuration
  task_cpu    = var.task_cpu
  task_memory = var.task_memory

  # Service Configuration
  desired_count    = var.desired_count
  platform_version = "LATEST"

  # Networking
  vpc_id              = module.vpc.vpc_id
  private_subnet_ids  = module.vpc.private_subnet_ids
  alb_security_groups = [aws_security_group.alb.id]
  target_group_arn    = module.alb.target_group_arn

  aws_region = var.aws_region

  # Environment Variables
  environment_variables = {
    SPRING_PROFILES_ACTIVE = var.environment
    SERVER_PORT            = tostring(var.container_port)
    JWT_EXPIRATION_MS      = "86400000"
    CORS_ALLOWED_ORIGINS   = var.cors_allowed_origins
  }

  # Secrets - TEMPORARIAMENTE SEM RDS (deploy fase 1)
  secrets = {}

  secrets_arns = [
    # local.db_credentials_secret_arn,  # Habilitar após deploy do database
    module.jwt_secret.secret_arn
  ]

  kms_key_arns = []

  # Logging
  log_retention_days        = 30  # Mais retenção para staging
  enable_container_insights = true

  # Health Check
  health_check_grace_period = 60

  # Deployment
  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  enable_deployment_circuit_breaker  = true
  enable_deployment_rollback         = true

  # ECS Exec
  enable_execute_command = var.enable_ecs_exec

  # Auto Scaling
  enable_autoscaling             = true
  autoscaling_min_capacity       = var.autoscaling_min_capacity
  autoscaling_max_capacity       = var.autoscaling_max_capacity
  autoscaling_cpu_target         = 70
  autoscaling_memory_target      = 80
  autoscaling_scale_in_cooldown  = 300
  autoscaling_scale_out_cooldown = 60

  tags = local.common_tags

  depends_on = [module.alb, module.jwt_secret]
}

# ================================
# CloudWatch Alarms
# ================================
resource "aws_cloudwatch_metric_alarm" "ecs_cpu_high" {
  alarm_name          = "${var.project_name}-${var.environment}-ecs-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "Staging - ECS CPU utilization high"
  treat_missing_data  = "notBreaching"

  dimensions = {
    ClusterName = module.ecs.cluster_name
    ServiceName = module.ecs.service_name
  }

  tags = local.common_tags
}

resource "aws_cloudwatch_metric_alarm" "ecs_memory_high" {
  alarm_name          = "${var.project_name}-${var.environment}-ecs-memory-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "MemoryUtilization"
  namespace           = "AWS/ECS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "Staging - ECS memory utilization high"
  treat_missing_data  = "notBreaching"

  dimensions = {
    ClusterName = module.ecs.cluster_name
    ServiceName = module.ecs.service_name
  }

  tags = local.common_tags
}

# ================================
# API Aprovação Orçamento Module
# ================================
module "api_aprovacao" {
  source = "../../modules/api-aprovacao"

  # Environment
  environment  = var.environment
  project_name = var.project_name
  app_name     = "api-aprovacao-orcamento"

  # Kubernetes
  namespace        = "default"
  create_namespace = false

  # Image (update with your ECR repository URL)
  image_name = "${module.ecr.repository_url}:latest"

  # Deployment
  replicas       = 3
  container_port = 8081
  service_port   = 80
  service_type   = "ClusterIP"

  # Application Config
  api_principal_url = "http://oficina-service.default.svc.cluster.local:8080"
  log_level         = "INFO"

  # Java Config
  java_memory_min = "512m"
  java_memory_max = "1g"

  # Resource Limits
  resources_requests_cpu    = "200m"
  resources_requests_memory = "512Mi"
  resources_limits_cpu      = "1000m"
  resources_limits_memory   = "1Gi"

  # HPA
  enable_hpa         = true
  hpa_min_replicas   = 3
  hpa_max_replicas   = 8
  hpa_cpu_target     = 70
  hpa_memory_target  = 80

  # Ingress (disabled by default)
  create_ingress = false
}
