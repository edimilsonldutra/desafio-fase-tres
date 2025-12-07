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
# VPC Variables
# ================================
variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of availability zones"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b", "us-east-1c"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24", "10.0.13.0/24"]
}

# ================================
# ECR Variables
# ================================
variable "ecr_repository_name" {
  description = "Name of the ECR repository"
  type        = string
  default     = "oficina-service"
}

# ================================
# ECS Variables
# ================================
variable "container_name" {
  description = "Name of the container"
  type        = string
  default     = "oficina-service"
}

variable "container_image" {
  description = "Container image (leave empty to use ECR repository)"
  type        = string
  default     = ""
}

variable "image_tag" {
  description = "Container image tag"
  type        = string
  default     = "latest"
}

variable "container_port" {
  description = "Port exposed by the container"
  type        = number
  default     = 8080
}

variable "task_cpu" {
  description = "CPU units for Fargate task (256, 512, 1024, 2048, 4096)"
  type        = string
  default     = "512"
}

variable "task_memory" {
  description = "Memory for Fargate task in MiB"
  type        = string
  default     = "1024"
}

variable "desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 2
}

variable "autoscaling_min_capacity" {
  description = "Minimum number of tasks for autoscaling"
  type        = number
  default     = 2
}

variable "autoscaling_max_capacity" {
  description = "Maximum number of tasks for autoscaling"
  type        = number
  default     = 10
}

variable "enable_ecs_exec" {
  description = "Enable ECS Exec for debugging"
  type        = bool
  default     = false
}

# ================================
# RDS Variables
# ================================
variable "rds_identifier" {
  description = "RDS instance identifier"
  type        = string
  default     = "oficina-db-dev"
}

variable "rds_engine_version" {
  description = "PostgreSQL engine version"
  type        = string
  default     = "15.5"
}

variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "rds_allocated_storage" {
  description = "Allocated storage in GB"
  type        = number
  default     = 20
}

variable "rds_database_name" {
  description = "Database name"
  type        = string
  default     = "oficina_db"
}

variable "rds_master_username" {
  description = "Master username for RDS"
  type        = string
  default     = "postgres"
}

variable "rds_master_password" {
  description = "Master password for RDS (null = auto-generate)"
  type        = string
  default     = null
  sensitive   = true
}

variable "rds_backup_retention_period" {
  description = "Backup retention period in days"
  type        = number
  default     = 7
}

variable "rds_multi_az" {
  description = "Enable Multi-AZ for RDS"
  type        = bool
  default     = false
}

variable "rds_deletion_protection" {
  description = "Enable deletion protection for RDS"
  type        = bool
  default     = false
}

variable "rds_skip_final_snapshot" {
  description = "Skip final snapshot on deletion"
  type        = bool
  default     = false
}

# ================================
# Application Variables
# ================================
variable "jwt_secret_key" {
  description = "JWT secret key for Lambda authentication"
  type        = string
  sensitive   = true
}

variable "cors_allowed_origins" {
  description = "CORS allowed origins"
  type        = string
  default     = "*"
}

# ================================
# ALB Variables
# ================================
variable "enable_https" {
  description = "Enable HTTPS on ALB"
  type        = bool
  default     = false
}

variable "acm_certificate_arn" {
  description = "ARN of ACM certificate for HTTPS"
  type        = string
  default     = ""
}

# ================================
# API Gateway Variables
# ================================
variable "enable_api_gateway" {
  description = "Enable API Gateway integration"
  type        = bool
  default     = false
}

variable "api_gateway_name" {
  description = "API Gateway name"
  type        = string
  default     = "oficina-api-dev"
}

variable "lambda_function_arn" {
  description = "ARN of Lambda function for authentication"
  type        = string
  default     = ""
}

variable "lambda_function_name" {
  description = "Name of Lambda function for authentication"
  type        = string
  default     = ""
}

variable "enable_custom_domain" {
  description = "Enable custom domain for API Gateway"
  type        = bool
  default     = false
}

variable "custom_domain_name" {
  description = "Custom domain name for API Gateway"
  type        = string
  default     = ""
}

variable "enable_waf" {
  description = "Enable WAF for API Gateway"
  type        = bool
  default     = false
}

variable "api_throttle_burst_limit" {
  description = "API Gateway throttle burst limit"
  type        = number
  default     = 5000
}

variable "api_throttle_rate_limit" {
  description = "API Gateway throttle rate limit"
  type        = number
  default     = 10000
}

# ================================
# Tags
# ================================
variable "tags" {
  description = "Additional tags"
  type        = map(string)
  default     = {}
}
