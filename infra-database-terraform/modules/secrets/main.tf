# ================================
# Secrets Manager Module
# ================================

# Secret
resource "aws_secretsmanager_secret" "db" {
  name                    = var.secret_name
  description             = var.description
  kms_key_id              = var.kms_key_id
  recovery_window_in_days = var.recovery_window_in_days

  tags = var.tags
}

# Secret Version
resource "aws_secretsmanager_secret_version" "db_current" {
  secret_id     = aws_secretsmanager_secret.db.id
  secret_string = var.secret_value
}

# Secret Rotation (optional)
resource "aws_secretsmanager_secret_rotation" "db_rotation" {
  count = var.enable_rotation && var.rotation_lambda_arn != null ? 1 : 0

  secret_id           = aws_secretsmanager_secret.db.id
  rotation_lambda_arn = var.rotation_lambda_arn

  rotation_rules {
    automatically_after_days = var.rotation_days
  }
}

