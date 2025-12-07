# ================================
# Environment Configuration - PRODUCTION
# ================================
aws_region   = "us-east-1"
environment  = "prod"
project_name = "oficina-mecanica"

# ================================
# VPC Configuration
# ================================
vpc_cidr             = "10.2.0.0/16"
availability_zones   = ["us-east-1a", "us-east-1b", "us-east-1c"]
public_subnet_cidrs  = ["10.2.1.0/24", "10.2.2.0/24", "10.2.3.0/24"]
private_subnet_cidrs = ["10.2.10.0/24", "10.2.20.0/24", "10.2.30.0/24"]

# ================================
# ECR Configuration
# ================================
ecr_repository_name = "oficina-service"

# ================================
# JWT Secret for Lambda Auth
# ================================
jwt_secret_key        = "production-jwt-secret-MUST-CHANGE-minimum-32-characters-required-secure"
jwt_expiration_hours  = 8

# ================================
# ECS Configuration
# ================================
ecs_cluster_name           = "oficina-ecs-prod"
oficina_service_name       = "oficina-service"
oficina_task_cpu           = "1024"
oficina_task_memory        = "2048"
oficina_desired_count      = 3
oficina_container_port     = 8080
enable_ecs_exec            = false

# API Gateway Configuration
api_gateway_name           = "oficina-api-gateway"
api_gateway_service_name   = "api-gateway-service"
api_gateway_task_cpu       = "1024"
api_gateway_task_memory    = "2048"
api_gateway_desired_count  = 3
api_gateway_container_port = 8080

# API Aprovação Configuration
api_aprovacao_name           = "api-aprovacao"
api_aprovacao_service_name   = "api-aprovacao-service"
api_aprovacao_task_cpu       = "1024"
api_aprovacao_task_memory    = "2048"
api_aprovacao_desired_count  = 3
api_aprovacao_container_port = 8080

# ================================
# ALB Configuration
# ================================
alb_name = "oficina-alb-prod"

# ================================
# Monitoring & Logs
# ================================
log_retention_days         = 30
enable_container_insights  = true

# ================================
# Auto Scaling
# ================================
autoscaling_min_capacity = 3
autoscaling_max_capacity = 20
