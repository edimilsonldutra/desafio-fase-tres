# Backend configuration for Terraform state
# IMPORTANTE: Descomente após criar o bucket S3 e tabela DynamoDB
# Veja ARQUITETURA-TERRAFORM.md para instruções de criação

# terraform {
#   backend "s3" {
#     bucket         = "fiap-oficina-terraform-state"
#     key            = "infra-kubernetes/dev/terraform.tfstate"
#     region         = "us-east-1"
#     encrypt        = true
#     dynamodb_table = "fiap-oficina-terraform-locks"
#   }
# }
