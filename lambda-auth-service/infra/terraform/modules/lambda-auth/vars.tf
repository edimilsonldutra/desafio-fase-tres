# ============================================================================
# Terraform Variables
# ============================================================================
#
# NOTA: Algumas variáveis declaradas neste arquivo não são utilizadas
# atualmente nos recursos Terraform, mas são mantidas para:
#
# 1. Configuração futura e extensibilidade
# 2. Padronização com terraform.tfvars
# 3. Documentação de opções disponíveis
# 4. Compatibilidade com módulos e workflows externos
#
# TFLint irá reportar warnings para variáveis não utilizadas, mas isso
# é intencional e não representa um problema.
# ============================================================================

# AWS Region
variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "us-east-1"

  validation {
    condition     = can(regex("^(us|eu|ap|sa|ca|me|af)-(north|south|east|west|central|northeast|southeast)-[1-9]$", var.aws_region))
    error_message = "Must be a valid AWS region name."
  }
}

# Environment
variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

# Project Name
variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "valida-pessoa"

  validation {
    condition     = can(regex("^[a-z0-9-]+$", var.project_name))
    error_message = "Project name must contain only lowercase letters, numbers, and hyphens."
  }
}

# JWT Configuration
variable "jwt_secret" {
  description = "Secret key for JWT generation (minimum 32 characters)"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.jwt_secret) >= 32
    error_message = "JWT secret must be at least 32 characters long."
  }
}

variable "jwt_expiration_ms" {
  description = "JWT token expiration time in milliseconds"
  type        = number
  default     = 3600000 # 1 hour

  validation {
    condition     = var.jwt_expiration_ms > 0 && var.jwt_expiration_ms <= 86400000
    error_message = "JWT expiration must be between 1ms and 24 hours."
  }
}

# Lambda Configuration
variable "lambda_timeout" {
  description = "Lambda function timeout in seconds"
  type        = number
  default     = 30

  validation {
    condition     = var.lambda_timeout >= 3 && var.lambda_timeout <= 900
    error_message = "Lambda timeout must be between 3 and 900 seconds."
  }
}

variable "lambda_memory_size" {
  description = "Lambda function memory size in MB"
  type        = number
  default     = 512

  validation {
    condition     = var.lambda_memory_size >= 128 && var.lambda_memory_size <= 10240
    error_message = "Lambda memory must be between 128 and 10240 MB."
  }
}

variable "lambda_jar_path" {
  description = "Path to Lambda JAR file relative to terraform directory"
  type        = string
  default     = "../../LambdaValidaPessoa/target/HelloWorld-1.0.jar"
}

variable "lambda_runtime" {
  description = "Lambda function runtime"
  type        = string
  default     = "java17"
}

variable "lambda_architecture" {
  description = "Lambda function architecture"
  type        = string
  default     = "x86_64"
  
  validation {
    condition     = contains(["x86_64", "arm64"], var.lambda_architecture)
    error_message = "Lambda architecture must be x86_64 or arm64."
  }
}

# KMS Configuration
variable "enable_kms_encryption" {
  description = "Enable KMS encryption for resources"
  type        = bool
  default     = false
}

variable "kms_key_deletion_window" {
  description = "KMS key deletion window in days"
  type        = number
  default     = 30

  validation {
    condition     = var.kms_key_deletion_window >= 7 && var.kms_key_deletion_window <= 30
    error_message = "KMS key deletion window must be between 7 and 30 days."
  }
}

# Secret Rotation Configuration
variable "enable_secret_rotation" {
  description = "Enable automatic secret rotation"
  type        = bool
  default     = false
}


# CloudWatch Configuration
variable "log_retention_days" {
  description = "CloudWatch log retention in days"
  type        = number
  default     = 14

  validation {
    condition = contains([
      1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653
    ], var.log_retention_days)
    error_message = "Log retention must be a valid CloudWatch retention period."
  }
}

# API Gateway Configuration
variable "api_throttle_burst_limit" {
  description = "API Gateway throttle burst limit"
  type        = number
  default     = 5000
}

variable "api_throttle_rate_limit" {
  description = "API Gateway throttle rate limit (requests per second)"
  type        = number
  default     = 10000
}

# Tags
variable "additional_tags" {
  description = "Additional tags to apply to all resources"
  type        = map(string)
  default     = {}
}

# ============================================================================
# Additional Lambda Configuration
# ============================================================================

variable "lambda_reserved_concurrent_executions" {
  description = "Amount of reserved concurrent executions for the Lambda function (-1 for unreserved)"
  type        = number
  default     = -1

  validation {
    condition     = var.lambda_reserved_concurrent_executions >= -1
    error_message = "Reserved concurrent executions must be -1 (unreserved) or a positive number."
  }
}


# ============================================================================
# Additional CloudWatch Configuration
# ============================================================================

variable "enable_lambda_insights" {
  description = "Enable Lambda Insights for enhanced monitoring"
  type        = bool
  default     = false
}

# ============================================================================
# Additional API Gateway Configuration
# ============================================================================

variable "enable_api_cache" {
  description = "Enable API Gateway caching"
  type        = bool
  default     = false
}

variable "api_enable_caching" {
  description = "Enable API Gateway caching (alias)"
  type        = bool
  default     = false
}

variable "api_cache_size" {
  description = "API Gateway cache size in GB"
  type        = string
  default     = "0.5"
  
  validation {
    condition     = contains(["0.5", "1.6", "6.1", "13.5", "28.4", "58.2", "118", "237"], var.api_cache_size)
    error_message = "Cache size must be a valid AWS API Gateway cache size."
  }
}

variable "api_cache_ttl" {
  description = "API Gateway cache TTL in seconds"
  type        = number
  default     = 300

  validation {
    condition     = var.api_cache_ttl >= 0 && var.api_cache_ttl <= 3600
    error_message = "Cache TTL must be between 0 and 3600 seconds."
  }
}

variable "secret_rotation_days" {
  description = "Number of days between automatic secret rotations"
  type        = number
  default     = 30
  
  validation {
    condition     = var.secret_rotation_days >= 1 && var.secret_rotation_days <= 365
    error_message = "Secret rotation days must be between 1 and 365."
  }
}

# ============================================================================
# Alarm Configuration
# ============================================================================

variable "enable_alarms" {
  description = "Enable CloudWatch alarms for monitoring"
  type        = bool
  default     = true
}

variable "lambda_error_threshold" {
  description = "Number of Lambda errors before triggering alarm"
  type        = number
  default     = 5

  validation {
    condition     = var.lambda_error_threshold > 0
    error_message = "Error threshold must be greater than 0."
  }
}

variable "lambda_duration_alarm_threshold_percentage" {
  description = "Percentage of timeout before triggering duration alarm (e.g., 80 for 80%)"
  type        = number
  default     = 80

  validation {
    condition     = var.lambda_duration_alarm_threshold_percentage > 0 && var.lambda_duration_alarm_threshold_percentage <= 100
    error_message = "Duration alarm threshold must be between 0 and 100 percent."
  }
}

variable "alarm_evaluation_periods" {
  description = "Number of periods to evaluate before triggering alarm"
  type        = number
  default     = 2

  validation {
    condition     = var.alarm_evaluation_periods >= 1
    error_message = "Evaluation periods must be at least 1."
  }
}

# ============================================================================
# VPC Configuration
# ============================================================================

variable "vpc_cidr" {
  description = "CIDR block for VPC (not used when using shared VPC)"
  type        = string
  default     = "10.0.0.0/16"
}

variable "vpc_id" {
  description = "ID of the VPC to use (from shared infrastructure)"
  type        = string
  default     = ""
}

variable "private_subnet_ids" {
  description = "IDs of private subnets for Lambda (from shared infrastructure)"
  type        = list(string)
  default     = []
}

variable "public_subnet_ids" {
  description = "IDs of public subnets (from shared infrastructure)"
  type        = list(string)
  default     = []
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24"]
}

variable "enable_nat_gateway" {
  description = "Enable NAT Gateway for private subnets"
  type        = bool
  default     = true
}

variable "enable_vpc_endpoints" {
  description = "Enable VPC endpoints for AWS services"
  type        = bool
  default     = true
}

# Remove old VPC variables that are now obsolete
# variable "vpc_id" - now created by vpc.tf
# variable "rds_subnet_ids" - now created by vpc.tf
# variable "lambda_security_group_ids" - now created by lambda.tf

# ============================================================================
# Feature Flags
# ============================================================================

variable "enable_xray_tracing" {
  description = "Enable X-Ray tracing for Lambda and API Gateway"
  type        = bool
  default     = false
}

variable "create_sample_data" {
  description = "Create sample customer data in DynamoDB (only for dev environment)"
  type        = bool
  default     = true
}

variable "enable_cors" {
  description = "Enable CORS configuration for API Gateway"
  type        = bool
  default     = true
}

# REMOVED: variable "db_secret_arn" - This should be computed from secrets.tf resource
# Use aws_secretsmanager_secret.db.arn instead

variable "db_schema" {
  description = "Database schema name"
  type        = string
  default     = "public"
}

variable "db_table" {
  description = "Database table name for people/customers"
  type        = string
  default     = "pessoas"
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "Allocated storage (GB)"
  type        = number
  default     = 20
}

variable "db_engine_version" {
  description = "PostgreSQL engine version"
  type        = string
  default     = "16.3"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "valida_pessoa"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

variable "db_endpoint" {
  description = "RDS endpoint from shared infrastructure (host:port)"
  type        = string
  default     = ""
}

variable "db_host" {
  description = "RDS host from shared infrastructure"
  type        = string
  default     = ""
}

variable "db_multi_az" {
  description = "Enable Multi-AZ"
  type        = bool
  default     = false
}

variable "db_backup_retention" {
  description = "Backup retention period (days)"
  type        = number
  default     = 0
}

variable "db_deletion_protection" {
  description = "Enable deletion protection"
  type        = bool
  default     = false
}

variable "db_skip_final_snapshot" {
  description = "Skip final snapshot on destroy"
  type        = bool
  default     = true
}

variable "db_secret_name" {
  description = "Name of the Secrets Manager secret for DB credentials"
  type        = string
  default     = "valida-pessoa-db-credentials"
}

# ============================================================================
# New Relic Monitoring Configuration
# ============================================================================

variable "new_relic_license_key" {
  description = "New Relic license key for APM monitoring"
  type        = string
  sensitive   = true
  default     = ""
}

variable "new_relic_account_id" {
  description = "New Relic account ID"
  type        = number
  sensitive   = true
  default     = 0 # 0 means disabled
}

variable "new_relic_api_key" {
  description = "New Relic API key for provider authentication"
  type        = string
  sensitive   = true
  default     = ""
}

variable "new_relic_region" {
  description = "New Relic region (US or EU)"
  type        = string
  default     = "US"

  validation {
    condition     = contains(["US", "EU"], var.new_relic_region)
    error_message = "New Relic region must be either US or EU."
  }
}

variable "new_relic_app_name" {
  description = "Application name in New Relic"
  type        = string
  default     = "lambda-valida-pessoa"
}

variable "new_relic_lambda_layer_arn" {
  description = "ARN of the New Relic Lambda Layer for Java"
  type        = string
  default     = "arn:aws:lambda:us-east-1:451483290750:layer:NewRelicJava21:1"
}

variable "enable_new_relic_monitoring" {
  description = "Enable New Relic APM monitoring"
  type        = bool
  default     = false # Disabled by default - set to true and provide credentials to enable
}

variable "new_relic_log_level" {
  description = "New Relic agent log level (off, severe, warning, info, fine, finer, finest)"
  type        = string
  default     = "info"

  validation {
    condition     = contains(["off", "severe", "warning", "info", "fine", "finer", "finest"], var.new_relic_log_level)
    error_message = "New Relic log level must be one of: off, severe, warning, info, fine, finer, finest."
  }
}

# ============================================================================
# Alert Configuration
# ============================================================================

variable "alert_email_recipients" {
  description = "Email recipients for alerts (comma-separated)"
  type        = string
  default     = ""
}

variable "alarm_email_endpoints" {
  description = "List of email addresses for alarm notifications"
  type        = list(string)
  default     = []
}

variable "enable_slack_notifications" {
  description = "Enable Slack notifications for alerts"
  type        = bool
  default     = false
}

variable "slack_webhook_url" {
  description = "Slack webhook URL for notifications"
  type        = string
  sensitive   = true
  default     = ""
}

variable "slack_channel" {
  description = "Slack channel for notifications"
  type        = string
  default     = "#alerts"
}

variable "enable_pagerduty" {
  description = "Enable PagerDuty integration"
  type        = bool
  default     = false
}

variable "pagerduty_service_key" {
  description = "PagerDuty service integration key"
  type        = string
  sensitive   = true
  default     = ""
}

# ================================
# Additional Variables
# ================================

variable "owner" {
  description = "Owner tag for resources"
  type        = string
  default     = "FIAP"
}

variable "cost_center" {
  description = "Cost center tag for resources"
  type        = string
  default     = "Engineering"
}

variable "assume_role_arn" {
  description = "ARN of IAM role to assume for AWS operations"
  type        = string
  default     = ""
}

variable "enable_vpc_flow_logs" {
  description = "Enable VPC Flow Logs"
  type        = bool
  default     = false
}
