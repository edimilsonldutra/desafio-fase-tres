# ================================
# Secrets Module Outputs
# ================================

output "secret_id" {
  description = "ID of the secret"
  value       = aws_secretsmanager_secret.db.id
}

output "secret_arn" {
  description = "ARN of the secret"
  value       = aws_secretsmanager_secret.db.arn
}

output "secret_name" {
  description = "Name of the secret"
  value       = aws_secretsmanager_secret.db.name
}

output "secret_version_id" {
  description = "Version ID of the secret"
  value       = aws_secretsmanager_secret_version.db_current.version_id
}
