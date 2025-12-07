# ================================
# Secrets Manager Secret
# ================================
resource "aws_secretsmanager_secret" "this" {
  name        = var.secret_name
  description = var.description

  recovery_window_in_days = var.recovery_window_in_days

  kms_key_id = var.kms_key_id

  tags = merge(
    var.tags,
    {
      Name = var.secret_name
    }
  )
}

# ================================
# Secret Version (Optional)
# ================================
resource "aws_secretsmanager_secret_version" "this" {
  count         = var.secret_string != null || var.secret_binary != null ? 1 : 0
  secret_id     = aws_secretsmanager_secret.this.id
  secret_string = var.secret_string
  secret_binary = var.secret_binary
}

# ================================
# Secret Rotation (Optional)
# ================================
resource "aws_secretsmanager_secret_rotation" "this" {
  count               = var.enable_rotation ? 1 : 0
  secret_id           = aws_secretsmanager_secret.this.id
  rotation_lambda_arn = var.rotation_lambda_arn

  rotation_rules {
    automatically_after_days = var.rotation_days
  }
}
