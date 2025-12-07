# ================================
# VPC Outputs
# ================================
output "vpc_id" {
  description = "ID of the VPC"
  value       = module.vpc.vpc_id
}

output "vpc_cidr" {
  description = "CIDR block of the VPC"
  value       = module.vpc.vpc_cidr
}

output "public_subnet_ids" {
  description = "IDs of public subnets"
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "IDs of private subnets"
  value       = module.vpc.private_subnet_ids
}

output "availability_zones" {
  description = "List of availability zones"
  value       = var.availability_zones
}

output "nat_gateway_id" {
  description = "ID of the NAT Gateway"
  value       = module.vpc.nat_gateway_id
}

output "internet_gateway_id" {
  description = "ID of the Internet Gateway"
  value       = module.vpc.internet_gateway_id
}

# ================================
# ECR Outputs
# ================================
output "ecr_repository_url" {
  description = "URL of the ECR repository"
  value       = module.ecr.repository_url
}

output "ecr_repository_arn" {
  description = "ARN of the ECR repository"
  value       = module.ecr.repository_arn
}

# ================================
# ALB Outputs
# ================================
output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = module.alb.alb_dns_name
}

output "alb_arn" {
  description = "ARN of the Application Load Balancer"
  value       = module.alb.alb_arn
}

output "alb_zone_id" {
  description = "Zone ID of the Application Load Balancer"
  value       = module.alb.alb_zone_id
}

# ================================
# ECS Outputs
# ================================
output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = module.ecs.cluster_name
}

output "ecs_cluster_arn" {
  description = "ARN of the ECS cluster"
  value       = module.ecs.cluster_arn
}

output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = module.ecs.service_name
}

output "ecs_task_definition_arn" {
  description = "ARN of the ECS task definition"
  value       = module.ecs.task_definition_arn
}

output "ecs_security_group_id" {
  description = "ID of the ECS security group"
  value       = module.ecs.security_group_id
}

# ================================
# RDS Outputs (from remote state) - TEMPORARIAMENTE DESABILITADO
# ================================
# output "rds_endpoint" {
#   description = "RDS connection endpoint (from infra-database)"
#   value       = local.rds_endpoint
# }

# output "rds_port" {
#   description = "RDS instance port (from infra-database)"
#   value       = local.rds_port
# }

# output "rds_database_name" {
#   description = "Name of the database (from infra-database)"
#   value       = local.rds_database_name
# }

# output "rds_secrets_manager_arn" {
#   description = "ARN of the Secrets Manager secret containing DB credentials (from infra-database)"
#   value       = local.db_credentials_secret_arn
#   sensitive   = true
# }

# ================================
# Secrets Outputs
# ================================
output "jwt_secret_arn" {
  description = "ARN of the JWT secret in Secrets Manager"
  value       = module.jwt_secret.secret_arn
}

# ================================
# API Gateway Outputs - NÃO IMPLEMENTADO NESTE AMBIENTE
# ================================
# output "api_gateway_invoke_url" {
#   description = "Invoke URL of the API Gateway"
#   value       = var.enable_api_gateway ? module.api_gateway[0].invoke_url : ""
# }

# output "api_gateway_id" {
#   description = "ID of the API Gateway"
#   value       = var.enable_api_gateway ? module.api_gateway[0].api_id : ""
# }

# ================================
# Application URL
# ================================
output "application_url" {
  description = "Application URL (ALB DNS)"
  value       = "http://${module.alb.alb_dns_name}"
}

output "application_health_check" {
  description = "Health check endpoint"
  value       = "http://${module.alb.alb_dns_name}/actuator/health"
}
