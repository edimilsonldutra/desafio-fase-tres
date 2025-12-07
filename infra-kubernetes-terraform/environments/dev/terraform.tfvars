# ================================
# Environment Configuration
# ================================
aws_region   = "us-east-1"
environment  = "dev"
project_name = "oficina-mecanica"

# ================================
# VPC Configuration
# ================================
vpc_cidr             = "10.0.0.0/16"
availability_zones   = ["us-east-1a", "us-east-1b"]
public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
private_subnet_cidrs = ["10.0.10.0/24", "10.0.20.0/24"]

# ================================
# ECR Configuration
# ================================
ecr_repository_name = "oficina-service"

# ================================
# JWT Secret for Lambda Auth
# ================================
jwt_secret_key        = "meu-jwt-secret-super-seguro-change-in-production-min-32-chars"
jwt_expiration_hours  = 24

# ================================
# ECS Configuration
# ================================
ecs_cluster_name           = "oficina-ecs-dev"
oficina_service_name       = "oficina-service"
oficina_task_cpu           = "256"
oficina_task_memory        = "512"
oficina_desired_count      = 2
oficina_container_port     = 8080
enable_ecs_exec            = true

# API Gateway Configuration
api_gateway_name           = "oficina-api-gateway"
api_gateway_service_name   = "api-gateway-service"
api_gateway_task_cpu       = "256"
api_gateway_task_memory    = "512"
api_gateway_desired_count  = 2
api_gateway_container_port = 8080

# API Aprovação Configuration
api_aprovacao_name           = "api-aprovacao"
api_aprovacao_service_name   = "api-aprovacao-service"
api_aprovacao_task_cpu       = "256"
api_aprovacao_task_memory    = "512"
api_aprovacao_desired_count  = 2
api_aprovacao_container_port = 8080

# ================================
# ALB Configuration
# ================================
alb_name = "oficina-alb-dev"

# ================================
# Monitoring & Logs
# ================================
log_retention_days         = 7
enable_container_insights  = true

# ================================
# Auto Scaling
# ================================
autoscaling_min_capacity = 2
autoscaling_max_capacity = 10
