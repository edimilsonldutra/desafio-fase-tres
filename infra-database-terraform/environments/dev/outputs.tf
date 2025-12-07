# ================================
# RDS Outputs
# ================================
output "rds_endpoint" {
  description = "RDS connection endpoint"
  value       = module.rds.endpoint
}

output "rds_address" {
  description = "RDS instance address"
  value       = module.rds.address
}

output "rds_port" {
  description = "RDS instance port"
  value       = module.rds.port
}

output "rds_database_name" {
  description = "Name of the database"
  value       = module.rds.database_name
}

output "rds_instance_id" {
  description = "ID of the RDS instance"
  value       = module.rds.db_instance_id
}

output "rds_instance_arn" {
  description = "ARN of the RDS instance"
  value       = module.rds.db_instance_arn
}

output "rds_security_group_id" {
  description = "ID of the RDS security group"
  value       = aws_security_group.rds.id
}

# ================================
# Secrets Manager Outputs
# ================================
output "db_credentials_secret_arn" {
  description = "ARN of the database credentials secret"
  value       = module.secrets.secret_arn
  sensitive   = true
}

output "db_credentials_secret_name" {
  description = "Name of the database credentials secret"
  value       = module.secrets.secret_name
}

# ================================
# KMS Outputs
# ================================
output "rds_kms_key_id" {
  description = "ID of the KMS key used for RDS encryption"
  value       = aws_kms_key.rds.id
}

output "rds_kms_key_arn" {
  description = "ARN of the KMS key used for RDS encryption"
  value       = aws_kms_key.rds.arn
}
