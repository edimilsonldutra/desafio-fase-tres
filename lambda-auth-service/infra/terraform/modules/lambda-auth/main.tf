# Main Terraform Configuration
# This file serves as the entry point for the infrastructure
# Individual resources are organized in separate files for better maintainability

# Architecture Overview:
# - vpc.tf: VPC, Subnets (public/private), NAT Gateway, Internet Gateway, Route Tables, VPC Endpoints
# - rds.tf: RDS PostgreSQL instance, DB subnet group, Security Groups
# - secrets.tf: AWS Secrets Manager for database credentials
# - lambda.tf: Lambda function with VPC configuration, Security Groups
# - iam.tf: IAM roles and policies for Lambda
# - api-gateway.tf: API Gateway REST API configuration
# - outputs.tf: Output values for reference
# - vars.tf: Input variables
# - locals.tf: Local values and computed variables
# - provider.tf: Provider configuration
# - backend.tf: Backend configuration (optional)

# Network Architecture:
# - VPC with 2 public and 2 private subnets across 2 AZs
# - NAT Gateway in public subnet for private subnet internet access
# - VPC Endpoints for Secrets Manager (optional, reduces NAT costs)
# - Lambda runs in private subnets with access to RDS
# - RDS in private subnets with Multi-AZ option

# Security:
# - Lambda SG allows outbound only
# - RDS SG allows inbound only from Lambda SG on port 5432
# - Database credentials stored in Secrets Manager
# - RDS encryption enabled
# - CloudWatch logs for monitoring
