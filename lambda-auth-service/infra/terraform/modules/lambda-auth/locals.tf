# Local values for resource naming and tagging
locals {
  # Resource naming convention: {project}-{environment}-{resource}
  name_prefix = "${var.project_name}-${var.environment}"

  # Lambda resources
  lambda_function_name = "${local.name_prefix}-validator"
  lambda_role_name     = "${local.name_prefix}-lambda-role"
  lambda_log_group     = "/aws/lambda/${local.lambda_function_name}"
  lambda_dlq_name      = "${local.name_prefix}-lambda-dlq"

  # API Gateway resources
  api_gateway_name  = "${local.name_prefix}-api"
  api_gateway_stage = var.environment

  # Database resources
  db_identifier        = "${local.name_prefix}-postgres"
  db_subnet_group_name = "${local.name_prefix}-db-subnets"
  db_secret_name       = "${local.name_prefix}/database/credentials"

  # Network resources
  vpc_name = "${local.name_prefix}-vpc"

  # Security Groups
  lambda_sg_name       = "${local.name_prefix}-lambda-sg"
  rds_sg_name          = "${local.name_prefix}-rds-sg"
  vpc_endpoints_sg_name = "${local.name_prefix}-vpc-endpoints-sg"

  # KMS
  kms_alias = "alias/${local.name_prefix}-encryption"

  # Common tags applied to all resources
  common_tags = merge(
    var.additional_tags,
    {
      Project      = var.project_name
      Environment  = var.environment
      ManagedBy    = "Terraform"
      Owner        = var.owner
      CostCenter   = var.cost_center
      Application  = "customer-validation"
      Compliance   = "LGPD"
      Backup       = var.environment == "prod" ? "required" : "optional"
      CreatedDate  = formatdate("YYYY-MM-DD", timestamp())
    }
  )

  # Availability zones (use first 2 for HA)
  azs = slice(data.aws_availability_zones.available.names, 0, min(2, length(data.aws_availability_zones.available.names)))

  # CIDR calculations for subnets
  public_subnet_cidrs  = [for i in range(2) : cidrsubnet(var.vpc_cidr, 4, i)]
  private_subnet_cidrs = [for i in range(2) : cidrsubnet(var.vpc_cidr, 4, i + 10)]
  db_subnet_cidrs      = [for i in range(2) : cidrsubnet(var.vpc_cidr, 4, i + 20)]
}

