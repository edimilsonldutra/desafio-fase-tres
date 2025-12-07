variable "cluster_name" {
  description = "Name of the ECS cluster"
  type        = string
}

variable "service_name" {
  description = "Name of the ECS service"
  type        = string
}

variable "container_name" {
  description = "Name of the container"
  type        = string
}

variable "container_image" {
  description = "Docker image to run in the ECS cluster"
  type        = string
}

variable "container_port" {
  description = "Port exposed by the container"
  type        = number
  default     = 8080
}

variable "task_cpu" {
  description = "The number of CPU units used by the task"
  type        = string
  default     = "256"
}

variable "task_memory" {
  description = "The amount of memory (in MiB) used by the task"
  type        = string
  default     = "512"
}

variable "desired_count" {
  description = "Number of instances of the task definition"
  type        = number
  default     = 2
}

variable "platform_version" {
  description = "Platform version on which to run your service"
  type        = string
  default     = "LATEST"
}

variable "vpc_id" {
  description = "VPC ID where ECS service will be deployed"
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnet IDs for ECS tasks"
  type        = list(string)
}

variable "alb_security_groups" {
  description = "Security groups of the ALB"
  type        = list(string)
}

variable "target_group_arn" {
  description = "ARN of the target group"
  type        = string
}

variable "aws_region" {
  description = "AWS region"
  type        = string
}

# Environment and Secrets
variable "environment_variables" {
  description = "Environment variables to pass to the container"
  type        = map(string)
  default     = {}
}

variable "secrets" {
  description = "Secrets to pass to the container (from Secrets Manager or Parameter Store)"
  type        = map(string)
  default     = {}
}

variable "secrets_arns" {
  description = "ARNs of secrets that the task execution role needs access to"
  type        = list(string)
  default     = []
}

variable "kms_key_arns" {
  description = "ARNs of KMS keys used to encrypt secrets"
  type        = list(string)
  default     = []
}

# IAM
variable "task_role_policy" {
  description = "IAM policy document for the task role"
  type        = string
  default     = null
}

# Logging
variable "log_retention_days" {
  description = "CloudWatch log retention in days"
  type        = number
  default     = 7
}

variable "enable_container_insights" {
  description = "Enable CloudWatch Container Insights"
  type        = bool
  default     = true
}

# Health Check
variable "container_health_check" {
  description = "Container health check configuration"
  type = object({
    command     = list(string)
    interval    = number
    timeout     = number
    retries     = number
    startPeriod = number
  })
  default = null
}

variable "health_check_grace_period" {
  description = "Seconds to ignore failing load balancer health checks on newly instantiated tasks"
  type        = number
  default     = 60
}

# Deployment
variable "deployment_maximum_percent" {
  description = "Upper limit on the number of tasks running during a deployment"
  type        = number
  default     = 200
}

variable "deployment_minimum_healthy_percent" {
  description = "Lower limit on the number of tasks that must remain running during a deployment"
  type        = number
  default     = 100
}

variable "enable_deployment_circuit_breaker" {
  description = "Enable deployment circuit breaker"
  type        = bool
  default     = true
}

variable "enable_deployment_rollback" {
  description = "Enable automatic rollback on deployment failure"
  type        = bool
  default     = true
}

variable "enable_execute_command" {
  description = "Enable ECS Exec for debugging"
  type        = bool
  default     = false
}

# Auto Scaling
variable "enable_autoscaling" {
  description = "Enable auto scaling for ECS service"
  type        = bool
  default     = true
}

variable "autoscaling_min_capacity" {
  description = "Minimum number of tasks"
  type        = number
  default     = 2
}

variable "autoscaling_max_capacity" {
  description = "Maximum number of tasks"
  type        = number
  default     = 10
}

variable "autoscaling_cpu_target" {
  description = "Target CPU utilization percentage"
  type        = number
  default     = 70
}

variable "autoscaling_memory_target" {
  description = "Target memory utilization percentage"
  type        = number
  default     = 80
}

variable "autoscaling_scale_in_cooldown" {
  description = "Cooldown period (in seconds) after a scale in activity"
  type        = number
  default     = 300
}

variable "autoscaling_scale_out_cooldown" {
  description = "Cooldown period (in seconds) after a scale out activity"
  type        = number
  default     = 60
}

variable "tags" {
  description = "A map of tags to add to all resources"
  type        = map(string)
  default     = {}
}
