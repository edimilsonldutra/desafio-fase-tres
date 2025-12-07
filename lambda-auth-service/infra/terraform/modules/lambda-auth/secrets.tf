# Secrets Manager - DB credentials
resource "aws_secretsmanager_secret" "db" {
  name                    = var.db_secret_name
  description             = "Database credentials for ${local.name_prefix} PostgreSQL"
  kms_key_id              = var.enable_kms_encryption ? aws_kms_key.main[0].id : null
  recovery_window_in_days = 7

  tags = merge(
    local.common_tags,
    {
      Name = var.db_secret_name
      Type = "Database-Credentials"
    }
  )
}

resource "aws_secretsmanager_secret_version" "db_current" {
  secret_id = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    username = var.db_username,
    password = var.db_password,
    host     = var.db_host,  # Using shared RDS from remote state
    port     = 5432,
    dbname   = var.db_name,
    engine   = "postgres"
  })
}

# Secrets Manager Rotation Configuration (se habilitado)
resource "aws_secretsmanager_secret_rotation" "db" {
  count = var.enable_secret_rotation ? 1 : 0

  secret_id           = aws_secretsmanager_secret.db.id
  rotation_lambda_arn = aws_lambda_function.rotate_secret[0].arn

  rotation_rules {
    automatically_after_days = var.secret_rotation_days
  }

  depends_on = [
    aws_lambda_permission.allow_secret_rotation,
    aws_iam_role_policy_attachment.rotation_lambda_vpc,
    aws_iam_role_policy_attachment.rotation_lambda_secrets
    # Removido: aws_iam_role_policy_attachment.rotation_lambda_rds - RDS compartilhado
  ]
}

# Lambda Function para Rotation (se habilitado)
resource "aws_lambda_function" "rotate_secret" {
  count = var.enable_secret_rotation ? 1 : 0

  function_name = "${local.name_prefix}-rotate-secret"
  role          = aws_iam_role.rotation_lambda[0].arn
  handler       = "lambda_function.lambda_handler"
  runtime       = "python3.11"
  timeout       = 30
  memory_size   = 256

  filename         = "${path.module}/rotation-lambda.zip"
  source_code_hash = fileexists("${path.module}/rotation-lambda.zip") ? filebase64sha256("${path.module}/rotation-lambda.zip") : null

  vpc_config {
    subnet_ids         = var.private_subnet_ids  # Using shared VPC subnets from remote state
    security_group_ids = [aws_security_group.lambda_sg.id]
  }

  environment {
    variables = {
      SECRETS_MANAGER_ENDPOINT = "https://secretsmanager.${data.aws_region.current.name}.amazonaws.com"
    }
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-rotate-secret"
      Type = "Lambda-Rotation"
    }
  )
}

# IAM Role para Lambda Rotation
resource "aws_iam_role" "rotation_lambda" {
  count = var.enable_secret_rotation ? 1 : 0

  name = "${local.name_prefix}-rotation-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-rotation-lambda-role"
    }
  )
}

# IAM Policy Attachments para Rotation Lambda
resource "aws_iam_role_policy_attachment" "rotation_lambda_vpc" {
  count = var.enable_secret_rotation ? 1 : 0

  role       = aws_iam_role.rotation_lambda[0].name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

resource "aws_iam_role_policy_attachment" "rotation_lambda_secrets" {
  count = var.enable_secret_rotation ? 1 : 0

  role       = aws_iam_role.rotation_lambda[0].name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/SecretsManagerReadWrite"
}

# Custom IAM Policy para RDS access
resource "aws_iam_role_policy" "rotation_lambda_rds" {
  count = var.enable_secret_rotation ? 1 : 0

  name = "${local.name_prefix}-rotation-lambda-rds-policy"
  role = aws_iam_role.rotation_lambda[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # Removido: RDS compartilhado gerenciado pelo módulo infra-database-terraform
      # {
      #   Effect = "Allow"
      #   Action = [
      #     "rds:DescribeDBInstances",
      #     "rds:ModifyDBInstance"
      #   ]
      #   Resource = aws_db_instance.postgres.arn
      # }
    ]
  })
}

# Lambda Permission para Secrets Manager invocar
resource "aws_lambda_permission" "allow_secret_rotation" {
  count = var.enable_secret_rotation ? 1 : 0

  statement_id  = "AllowExecutionFromSecretsManager"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.rotate_secret[0].function_name
  principal     = "secretsmanager.amazonaws.com"
}

