# ================================
# Backend Configuration - Development
# ================================
# Development uses local backend
# For staging/prod, use S3 backend

terraform {
  backend "local" {
    path = "terraform.tfstate"
  }
}

# For staging/prod, uncomment and configure:
# terraform {
#   backend "s3" {
#     bucket         = "fiap-oficina-terraform-state"
#     key            = "lambda-auth/dev/terraform.tfstate"
#     region         = "us-east-1"
#     encrypt        = true
#     dynamodb_table = "fiap-oficina-terraform-locks"
#   }
# }
