# ================================
# RDS Module Outputs
# ================================

output "db_instance_id" {
  description = "RDS instance ID"
  value       = aws_db_instance.postgres.id
}

output "db_instance_arn" {
  description = "RDS instance ARN"
  value       = aws_db_instance.postgres.arn
}

output "endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.postgres.endpoint
}

output "address" {
  description = "RDS instance address"
  value       = aws_db_instance.postgres.address
}

output "port" {
  description = "RDS instance port"
  value       = aws_db_instance.postgres.port
}

output "database_name" {
  description = "Name of the database"
  value       = aws_db_instance.postgres.db_name
}

output "username" {
  description = "Master username"
  value       = aws_db_instance.postgres.username
  sensitive   = true
}

output "resource_id" {
  description = "RDS resource ID"
  value       = aws_db_instance.postgres.resource_id
}

output "hosted_zone_id" {
  description = "Hosted zone ID for the RDS instance"
  value       = aws_db_instance.postgres.hosted_zone_id
}

output "availability_zone" {
  description = "Availability zone of the instance"
  value       = aws_db_instance.postgres.availability_zone
}

output "multi_az" {
  description = "Whether Multi-AZ is enabled"
  value       = aws_db_instance.postgres.multi_az
}

output "backup_retention_period" {
  description = "Backup retention period"
  value       = aws_db_instance.postgres.backup_retention_period
}

output "engine" {
  description = "Database engine"
  value       = aws_db_instance.postgres.engine
}

output "engine_version" {
  description = "Database engine version"
  value       = aws_db_instance.postgres.engine_version
}
