# Local variables for API Gateway module

locals {
  api_gateway_name = "${var.project_name}-${var.api_name}-${var.environment}"

  common_tags = merge(
    var.tags,
    {
      Environment  = var.environment
      ManagedBy    = "Terraform"
      Project      = var.project_name
      Module       = "api-gateway"
    }
  )
}
