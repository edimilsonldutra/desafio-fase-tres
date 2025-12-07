# API Gateway Name
variable "api_name" {
  description = "Name of the API Gateway"
  type        = string
}

# Environment
variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
}

# Project Name
variable "project_name" {
  description = "Project name for resource naming"
  type        = string
}

# Lambda Integration
variable "lambda_function_arn" {
  description = "ARN of the Lambda function for auth integration"
  type        = string
}

variable "lambda_function_name" {
  description = "Name of the Lambda function for auth integration"
  type        = string
}

# ALB Integration
variable "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  type        = string
  default     = ""
}

variable "alb_listener_arn" {
  description = "ARN of the ALB listener"
  type        = string
  default     = ""
}

variable "vpc_link_id" {
  description = "ID of the VPC Link for private integration"
  type        = string
  default     = ""
}

# Custom Domain
variable "enable_custom_domain" {
  description = "Enable custom domain for API Gateway"
  type        = bool
  default     = false
}

variable "domain_name" {
  description = "Custom domain name for API Gateway"
  type        = string
  default     = ""
}

variable "certificate_arn" {
  description = "ARN of ACM certificate for custom domain"
  type        = string
  default     = ""
}

# WAF
variable "enable_waf" {
  description = "Enable AWS WAF for API Gateway"
  type        = bool
  default     = false
}

# Logging
variable "enable_access_logs" {
  description = "Enable access logs for API Gateway"
  type        = bool
  default     = true
}

variable "log_retention_days" {
  description = "CloudWatch log retention in days"
  type        = number
  default     = 7
}

variable "enable_execution_logs" {
  description = "Enable execution logs for API Gateway"
  type        = bool
  default     = true
}

variable "enable_xray_tracing" {
  description = "Enable AWS X-Ray tracing"
  type        = bool
  default     = false
}

# Throttling
variable "api_throttle_burst_limit" {
  description = "API throttle burst limit"
  type        = number
  default     = 5000
}

variable "api_throttle_rate_limit" {
  description = "API throttle rate limit (requests per second)"
  type        = number
  default     = 2000
}

# Tags
variable "tags" {
  description = "Additional tags for resources"
  type        = map(string)
  default     = {}
}
