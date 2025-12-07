# ================================
# Lambda Auth Service - Production Environment
# ================================

# Data Source - VPC from infra-kubernetes-terraform
data "terraform_remote_state" "vpc" {
  backend = "s3"
  config = {
    bucket = "fiap-oficina-terraform-state"
    key    = "infra-kubernetes/prod/terraform.tfstate"
    region = "us-east-1"
  }
}

# Data Source - RDS from infra-database-terraform  
data "terraform_remote_state" "database" {
  backend = "s3"
  config = {
    bucket = "fiap-oficina-terraform-state"
    key    = "infra-database/prod/terraform.tfstate"
    region = "us-east-1"
  }
}

module "lambda_auth" {
  source = "../../modules/lambda-auth"

  # Environment
  environment  = "prod"
  project_name = "fiap-oficina"
  aws_region   = "us-east-1"

  # VPC Configuration - usando VPC compartilhada via remote state
  vpc_id              = data.terraform_remote_state.vpc.outputs.vpc_id
  vpc_cidr            = data.terraform_remote_state.vpc.outputs.vpc_cidr
  private_subnet_ids  = data.terraform_remote_state.vpc.outputs.private_subnet_ids
  public_subnet_ids   = data.terraform_remote_state.vpc.outputs.public_subnet_ids
  enable_nat_gateway  = false
  enable_vpc_endpoints = false

  # Database Configuration - dados do RDS compartilhado via remote state
  db_host               = data.terraform_remote_state.database.outputs.rds_endpoint
  db_name               = "oficina_db_prod"
  db_username           = "dbadmin"
  db_password           = var.db_password
  db_instance_class     = "db.t3.medium"
  db_allocated_storage  = 100
  db_engine_version     = "15.5"
  db_multi_az           = true
  db_backup_retention   = 30
  db_deletion_protection = true
  db_skip_final_snapshot = false
  db_schema             = "public"
  db_table              = "pessoas"
  db_secret_name        = "fiap-oficina-prod-db-credentials"

  # Lambda Configuration
  lambda_timeout     = 30
  lambda_memory_size = 1024
  lambda_jar_path    = "../../../../target/ValidaPessoa-1.0.jar"
  lambda_reserved_concurrent_executions = -1
  
  # JWT Configuration
  jwt_secret        = var.jwt_secret
  jwt_expiration_ms = 3600000

  # CloudWatch
  log_retention_days     = 30
  enable_xray_tracing    = true
  enable_lambda_insights = true

  # API Gateway
  api_throttle_rate_limit  = 10000
  api_throttle_burst_limit = 5000
  enable_api_cache         = true
  api_cache_ttl            = 300

  # Alarms
  enable_alarms                              = true
  lambda_error_threshold                     = 3
  lambda_duration_alarm_threshold_percentage = 80
  alarm_evaluation_periods                   = 2
  alert_email_recipients                     = "devops@fiap.com.br"
  enable_slack_notifications                 = false
  enable_pagerduty                           = false
}

# ================================
# Outputs
# ================================

output "lambda_function_arn" {
  description = "ARN of the Lambda function"
  value       = module.lambda_auth.lambda_function_arn
}

output "api_gateway_url" {
  description = "API Gateway URL"
  value       = module.lambda_auth.api_gateway_url
}

# Removido: RDS e VPC outputs - usar remote state dos módulos compartilhados
# output "rds_endpoint" {
#   description = "RDS endpoint"
#   value       = data.terraform_remote_state.database.outputs.rds_endpoint
#   sensitive   = true
# }

# output "vpc_id" {
#   description = "VPC ID"
#   value       = data.terraform_remote_state.vpc.outputs.vpc_id
# }
