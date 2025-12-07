# ================================
# General Variables
# ================================
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "oficina-mecanica"
}

# ================================
# Database Variables
# ================================
variable "db_name" {
  description = "Name of the database"
  type        = string
  default     = "oficina_db"
}

variable "db_username" {
  description = "Master username for the database"
  type        = string
  default     = "dbadmin"
  sensitive   = true
}

variable "db_engine_version" {
  description = "PostgreSQL engine version"
  type        = string
  default     = "15.5"
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "Allocated storage in GB"
  type        = number
  default     = 20
}

variable "db_max_allocated_storage" {
  description = "Maximum allocated storage for autoscaling in GB"
  type        = number
  default     = 100
}

variable "db_storage_type" {
  description = "Storage type (gp3, gp2, io1)"
  type        = string
  default     = "gp3"
}

variable "multi_az" {
  description = "Enable Multi-AZ deployment"
  type        = bool
  default     = false
}

# ================================
# Backup Variables
# ================================
variable "backup_retention_period" {
  description = "Number of days to retain backups"
  type        = number
  default     = 7
}

variable "backup_window" {
  description = "Preferred backup window"
  type        = string
  default     = "03:00-04:00"
}

variable "maintenance_window" {
  description = "Preferred maintenance window"
  type        = string
  default     = "Mon:04:00-Mon:05:00"
}

# ================================
# Monitoring Variables
# ================================
variable "enable_enhanced_monitoring" {
  description = "Enable enhanced monitoring"
  type        = bool
  default     = true
}

variable "monitoring_interval" {
  description = "Enhanced monitoring interval in seconds"
  type        = number
  default     = 60
}

variable "enable_performance_insights" {
  description = "Enable Performance Insights"
  type        = bool
  default     = true
}

variable "enable_iam_auth" {
  description = "Enable IAM database authentication"
  type        = bool
  default     = false
}

# ================================
# Security Group Variables
# ================================
variable "ecs_security_group_id" {
  description = "Security group ID of ECS tasks"
  type        = string
  default     = ""
}

variable "lambda_security_group_id" {
  description = "Security group ID of Lambda functions"
  type        = string
  default     = ""
}

variable "eks_security_group_id" {
  description = "Security group ID of EKS cluster"
  type        = string
  default     = ""
}

# ================================
# Secrets Variables
# ================================
variable "enable_secret_rotation" {
  description = "Enable automatic secret rotation"
  type        = bool
  default     = false
}

# ================================
# Lambda Variables
# ================================
variable "jwt_secret" {
  description = "JWT secret key for Lambda authentication"
  type        = string
  sensitive   = true
}

variable "jwt_expiration_ms" {
  description = "JWT expiration time in milliseconds"
  type        = number
  default     = 3600000 # 1 hour
}

variable "lambda_memory_size" {
  description = "Lambda function memory size in MB"
  type        = number
  default     = 512
}

variable "lambda_jar_path" {
  description = "Path to Lambda JAR file"
  type        = string
  default     = "../../lambda-auth-service/target/ValidaPessoa-1.0.jar"
}

variable "lambda_reserved_concurrent_executions" {
  description = "Reserved concurrent executions for Lambda (-1 for unreserved)"
  type        = number
  default     = -1
}

variable "lambda_error_threshold" {
  description = "Number of errors before CloudWatch alarm triggers"
  type        = number
  default     = 5
}

variable "lambda_duration_alarm_threshold_percentage" {
  description = "Percentage of timeout duration before alarm triggers"
  type        = number
  default     = 80
}

# ================================
# API Gateway Variables
# ================================
variable "api_throttle_burst_limit" {
  description = "API Gateway throttle burst limit"
  type        = number
  default     = 5000
}

variable "enable_api_cache" {
  description = "Enable API Gateway caching"
  type        = bool
  default     = false
}

variable "api_cache_ttl" {
  description = "API Gateway cache TTL in seconds"
  type        = number
  default     = 300
}

# ================================
# Monitoring & Tracing Variables
# ================================
variable "enable_xray_tracing" {
  description = "Enable AWS X-Ray tracing for Lambda"
  type        = bool
  default     = false
}

variable "create_sample_data" {
  description = "Create sample data in database (dev only)"
  type        = bool
  default     = true
}

# ================================
# Additional Lambda & API Variables
# ================================
variable "lambda_timeout" {
  description = "Lambda function timeout in seconds"
  type        = number
  default     = 30
}

variable "enable_lambda_insights" {
  description = "Enable Lambda Insights for enhanced monitoring"
  type        = bool
  default     = false
}

variable "log_retention_days" {
  description = "CloudWatch log retention period in days"
  type        = number
  default     = 14
}

variable "api_throttle_rate_limit" {
  description = "API Gateway throttle rate limit (requests per second)"
  type        = number
  default     = 10000
}

variable "enable_alarms" {
  description = "Enable CloudWatch alarms"
  type        = bool
  default     = true
}

variable "alarm_evaluation_periods" {
  description = "Number of periods before CloudWatch alarm triggers"
  type        = number
  default     = 2
}

variable "enable_cors" {
  description = "Enable CORS for API Gateway"
  type        = bool
  default     = true
}

variable "additional_tags" {
  description = "Additional tags to apply to all resources"
  type        = map(string)
  default     = {}
}
