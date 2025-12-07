terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    # New Relic provider is optional - only needed if enable_new_relic_monitoring = true
    # To use New Relic, uncomment the block below and provide credentials
    # newrelic = {
    #   source  = "newrelic/newrelic"
    #   version = "~> 3.0"
    # }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Owner       = "FIAP"
    }
  }
}

# New Relic provider configuration
# Uncomment this block only if you want to use New Relic monitoring
# You must provide valid credentials:
# - new_relic_account_id (your New Relic account ID)
# - new_relic_api_key (your New Relic User API Key)
# - new_relic_region (US or EU)
#
# To enable:
# 1. Uncomment the newrelic provider in required_providers above
# 2. Uncomment this provider block below
# 3. Set enable_new_relic_monitoring = true in your terraform.tfvars
# 4. Provide valid credentials in terraform.tfvars or as environment variables

# provider "newrelic" {
#   account_id = var.new_relic_account_id
#   api_key    = var.new_relic_api_key
#   region     = var.new_relic_region
# }
