# ========================================
# API Gateway Outputs
# ========================================

output "api_gateway_url" {
  description = "URL of the API Gateway endpoint"
  value       = "${aws_api_gateway_stage.stage.invoke_url}/auth"
}

output "api_gateway_id" {
  description = "ID of the API Gateway"
  value       = aws_api_gateway_rest_api.api.id
}

output "api_gateway_stage_name" {
  description = "Name of the API Gateway stage"
  value       = aws_api_gateway_stage.stage.stage_name
}

output "api_gateway_execution_arn" {
  description = "Execution ARN of the API Gateway"
  value       = aws_api_gateway_rest_api.api.execution_arn
}

# ========================================
# Lambda Outputs
# ========================================

output "lambda_function_name" {
  description = "Name of the Lambda function"
  value       = aws_lambda_function.valida_pessoa.function_name
}

output "lambda_function_arn" {
  description = "ARN of the Lambda function"
  value       = aws_lambda_function.valida_pessoa.arn
}

output "lambda_invoke_arn" {
  description = "Invoke ARN of the Lambda function"
  value       = aws_lambda_function.valida_pessoa.invoke_arn
}

output "lambda_role_arn" {
  description = "ARN of the Lambda IAM role"
  value       = aws_iam_role.lambda_role.arn
}

output "lambda_security_group_id" {
  description = "Security Group ID for Lambda"
  value       = aws_security_group.lambda_sg.id
}

output "lambda_dlq_url" {
  description = "URL of the Lambda Dead Letter Queue"
  value       = aws_sqs_queue.lambda_dlq.url
}

output "lambda_dlq_arn" {
  description = "ARN of the Lambda Dead Letter Queue"
  value       = aws_sqs_queue.lambda_dlq.arn
}

# ========================================
# RDS Outputs
# ========================================
# Removido: Este módulo usa RDS compartilhado via remote state
# Os outputs de RDS devem vir do módulo infra-database-terraform

# output "rds_endpoint" {
#   description = "RDS PostgreSQL endpoint"
#   value       = aws_db_instance.postgres.endpoint
#   sensitive   = true
# }

# output "rds_address" {
#   description = "RDS PostgreSQL address (hostname only)"
#   value       = aws_db_instance.postgres.address
# }

# output "rds_port" {
#   description = "RDS PostgreSQL port"
#   value       = aws_db_instance.postgres.port
# }

# output "rds_instance_id" {
#   description = "RDS Instance ID"
#   value       = aws_db_instance.postgres.id
# }

# output "rds_arn" {
#   description = "RDS Instance ARN"
#   value       = aws_db_instance.postgres.arn
# }

# output "rds_security_group_id" {
#   description = "RDS Security Group ID"
#   value       = aws_security_group.rds_sg.id
# }

output "db_secret_arn" {
  description = "ARN of the DB credentials secret"
  value       = aws_secretsmanager_secret.db.arn
  sensitive   = true
}

output "db_secret_name" {
  description = "Name of the DB credentials secret"
  value       = aws_secretsmanager_secret.db.name
}

# ========================================
# VPC Outputs
# ========================================
# Removido: Este módulo usa VPC compartilhada via remote state
# Os outputs de VPC devem vir do módulo infra-kubernetes-terraform

# output "vpc_id" {
#   description = "VPC ID"
#   value       = aws_vpc.main.id
# }

# output "vpc_cidr_block" {
#   description = "VPC CIDR block"
#   value       = aws_vpc.main.cidr_block
# }

# output "public_subnet_ids" {
#   description = "Public subnet IDs"
#   value       = aws_subnet.public[*].id
# }

# output "private_subnet_ids" {
#   description = "Private subnet IDs"
#   value       = aws_subnet.private[*].id
# }

# output "public_subnet_cidrs" {
#   description = "Public subnet CIDR blocks"
#   value       = aws_subnet.public[*].cidr_block
# }

# output "private_subnet_cidrs" {
#   description = "Private subnet CIDR blocks"
#   value       = aws_subnet.private[*].cidr_block
# }

# output "availability_zones" {
#   description = "Availability zones used"
#   value       = aws_subnet.private[*].availability_zone
# }

# output "nat_gateway_id" {
#   description = "NAT Gateway ID"
#   value       = var.enable_nat_gateway ? aws_nat_gateway.main[0].id : null
# }

# output "nat_gateway_ip" {
#   description = "NAT Gateway public IP"
#   value       = var.enable_nat_gateway ? aws_eip.nat[0].public_ip : null
# }

# output "vpc_endpoints_sg_id" {
#   description = "Security Group ID for VPC Endpoints"
#   value       = aws_security_group.vpc_endpoints.id
# }

# ========================================
# KMS Outputs
# ========================================

output "kms_key_id" {
  description = "KMS Key ID"
  value       = var.enable_kms_encryption ? aws_kms_key.main[0].id : null
}

output "kms_key_arn" {
  description = "KMS Key ARN"
  value       = var.enable_kms_encryption ? aws_kms_key.main[0].arn : null
}

output "kms_key_alias" {
  description = "KMS Key Alias"
  value       = var.enable_kms_encryption ? aws_kms_alias.main[0].name : null
}

# ========================================
# CloudWatch Outputs
# ========================================

output "lambda_log_group_name" {
  description = "Name of the Lambda CloudWatch log group"
  value       = aws_cloudwatch_log_group.lambda_log_group.name
}

output "api_gateway_log_group_name" {
  description = "Name of the API Gateway CloudWatch log group"
  value       = aws_cloudwatch_log_group.api_gateway_logs.name
}

# Removido: VPC Flow Logs não criados neste módulo
# output "vpc_flow_logs_log_group_name" {
#   description = "Name of the VPC Flow Logs CloudWatch log group"
#   value       = var.enable_vpc_flow_logs ? aws_cloudwatch_log_group.vpc_flow_logs[0].name : null
# }

# ========================================
# SNS Outputs
# ========================================

output "sns_topic_arn" {
  description = "ARN of the SNS topic for alarms"
  value       = var.enable_alarms ? aws_sns_topic.alarms[0].arn : null
}

# ========================================
# General Outputs
# ========================================

output "aws_region" {
  description = "AWS region where resources are deployed"
  value       = data.aws_region.current.name
}

output "aws_account_id" {
  description = "AWS Account ID"
  value       = data.aws_caller_identity.current.account_id
}

output "environment" {
  description = "Environment name"
  value       = var.environment
}

output "project_name" {
  description = "Project name"
  value       = var.project_name
}

# ========================================
# Test Command
# ========================================

output "test_command" {
  description = "Command to test the API"
  value       = "curl -X POST ${aws_api_gateway_stage.stage.invoke_url}/auth -H 'Content-Type: application/json' -d '{\"cpf\":\"11144477735\"}'"
}

# Lambda Security Group output já declarado na linha 49
# Removido duplicação

# ========================================
# CI/CD Outputs - GitHub Actions Setup
# ========================================

# Development Environment
output "github_secret_aws_access_key_id_dev" {
  description = "AWS Access Key ID for GitHub Actions - Development (Add this to GitHub Secrets as AWS_ACCESS_KEY_ID_DEV)"
  value       = aws_iam_access_key.github_actions_dev.id
  sensitive   = false
}

output "github_secret_aws_secret_access_key_dev" {
  description = "AWS Secret Access Key for GitHub Actions - Development (Add this to GitHub Secrets as AWS_SECRET_ACCESS_KEY_DEV)"
  value       = aws_iam_access_key.github_actions_dev.secret
  sensitive   = true
}

output "github_secret_s3_deployment_bucket_dev" {
  description = "S3 Deployment Bucket for Development (Add this to GitHub Secrets as S3_DEPLOYMENT_BUCKET_DEV)"
  value       = aws_s3_bucket.deployment_dev.id
  sensitive   = false
}

# Production Environment
output "github_secret_aws_access_key_id_prod" {
  description = "AWS Access Key ID for GitHub Actions - Production (Add this to GitHub Secrets as AWS_ACCESS_KEY_ID_PROD)"
  value       = aws_iam_access_key.github_actions_prod.id
  sensitive   = false
}

output "github_secret_aws_secret_access_key_prod" {
  description = "AWS Secret Access Key for GitHub Actions - Production (Add this to GitHub Secrets as AWS_SECRET_ACCESS_KEY_PROD)"
  value       = aws_iam_access_key.github_actions_prod.secret
  sensitive   = true
}

output "github_secret_s3_deployment_bucket_prod" {
  description = "S3 Deployment Bucket for Production (Add this to GitHub Secrets as S3_DEPLOYMENT_BUCKET_PROD)"
  value       = aws_s3_bucket.deployment_prod.id
  sensitive   = false
}

# Summary output for easy copy-paste
output "github_actions_setup_summary" {
  description = "Summary of all GitHub Secrets to configure"
  value       = <<-EOT

    ========================================
    🔐 GitHub Secrets Configuration
    ========================================

    Go to: GitHub Repository → Settings → Secrets and variables → Actions

    Add the following Repository Secrets:

    📌 Development Environment:
    ---------------------------
    Name: AWS_ACCESS_KEY_ID_DEV
    Value: ${aws_iam_access_key.github_actions_dev.id}

    Name: AWS_SECRET_ACCESS_KEY_DEV
    Value: <sensitive - use: terraform output -raw github_secret_aws_secret_access_key_dev>

    Name: S3_DEPLOYMENT_BUCKET_DEV
    Value: ${aws_s3_bucket.deployment_dev.id}

    📌 Production Environment:
    --------------------------
    Name: AWS_ACCESS_KEY_ID_PROD
    Value: ${aws_iam_access_key.github_actions_prod.id}

    Name: AWS_SECRET_ACCESS_KEY_PROD
    Value: <sensitive - use: terraform output -raw github_secret_aws_secret_access_key_prod>

    Name: S3_DEPLOYMENT_BUCKET_PROD
    Value: ${aws_s3_bucket.deployment_prod.id}

    ========================================
    💡 To view sensitive values:
    ========================================
    terraform output -raw github_secret_aws_secret_access_key_dev
    terraform output -raw github_secret_aws_secret_access_key_prod

  EOT
  sensitive   = false
}

# Lambda Security Group (continuing from above)
output "lambda_security_group_id_continued" {
  description = "Lambda Security Group ID"
  value       = aws_security_group.lambda_sg.id
}

