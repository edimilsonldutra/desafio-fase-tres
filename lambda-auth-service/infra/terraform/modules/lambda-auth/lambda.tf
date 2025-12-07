# Security Group for Lambda
resource "aws_security_group" "lambda_sg" {
  name        = "${local.name_prefix}-lambda-sg"
  description = "Security group for Lambda function"
  vpc_id      = var.vpc_id  # Using shared VPC from remote state

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-lambda-sg"
    }
  )
}

# CloudWatch Log Group for Lambda
resource "aws_cloudwatch_log_group" "lambda_log_group" {
  name              = "/aws/lambda/${local.lambda_function_name}"
  retention_in_days = var.log_retention_days
  kms_key_id        = var.enable_kms_encryption ? aws_kms_key.main[0].arn : null

  tags = merge(
    local.common_tags,
    {
      Name = "${local.lambda_function_name}-logs"
      Type = "CloudWatch-LogGroup"
    }
  )
}

# Dead Letter Queue para Lambda
resource "aws_sqs_queue" "lambda_dlq" {
  name                       = "${local.lambda_function_name}-dlq"
  message_retention_seconds  = 1209600 # 14 days
  visibility_timeout_seconds = var.lambda_timeout * 6
  kms_master_key_id          = var.enable_kms_encryption ? aws_kms_key.main[0].id : null

  tags = merge(
    local.common_tags,
    {
      Name = "${local.lambda_function_name}-dlq"
      Type = "DLQ"
    }
  )
}

# Lambda Function
resource "aws_lambda_function" "valida_pessoa" {
  function_name    = local.lambda_function_name
  role             = aws_iam_role.lambda_role.arn
  filename         = var.lambda_jar_path
  source_code_hash = fileexists(var.lambda_jar_path) ? filebase64sha256(var.lambda_jar_path) : null
  handler          = "lambdavalida.ValidaPessoaFunction::handleRequest"
  runtime          = var.lambda_runtime
  timeout          = var.lambda_timeout
  memory_size      = var.lambda_memory_size
  architectures    = [var.lambda_architecture]

  # Reserved Concurrent Executions (previne runaway costs)
  reserved_concurrent_executions = var.lambda_reserved_concurrent_executions

  # VPC Configuration - usando subnets compartilhadas
  vpc_config {
    subnet_ids         = var.private_subnet_ids  # Using shared VPC subnets from remote state
    security_group_ids = [aws_security_group.lambda_sg.id]
  }

  # Dead Letter Queue
  dead_letter_config {
    target_arn = aws_sqs_queue.lambda_dlq.arn
  }

  environment {
    variables = merge(
      {
        JWT_SECRET        = var.jwt_secret
        JWT_EXPIRATION_MS = tostring(var.jwt_expiration_ms)
        ENVIRONMENT       = var.environment
        LOG_LEVEL         = var.environment == "prod" ? "INFO" : "DEBUG"
        DB_SECRET_ARN     = aws_secretsmanager_secret.db.arn
        DB_SCHEMA         = var.db_schema
        DB_TABLE          = var.db_table
      },
      # Add New Relic environment variables only if monitoring is enabled
      var.enable_new_relic_monitoring ? {
        NEW_RELIC_LICENSE_KEY                  = var.new_relic_license_key
        NEW_RELIC_APP_NAME                     = "${local.lambda_function_name}-${var.environment}"
        NEW_RELIC_LOG_LEVEL                    = var.environment == "prod" ? "info" : "debug"
        NEW_RELIC_DISTRIBUTED_TRACING_ENABLED  = "true"
        NEW_RELIC_EXTENSION_SEND_FUNCTION_LOGS = "true"
        NEW_RELIC_EXTENSION_LOG_LEVEL          = "INFO"
      } : {}
    )
  }

  # Lambda Layers (New Relic + Lambda Insights)
  layers = concat(
    var.enable_new_relic_monitoring ? [var.new_relic_lambda_layer_arn] : [],
    var.enable_lambda_insights ? [data.aws_lambda_layer_version.lambda_insights[0].arn] : []
  )

  tracing_config {
    mode = var.enable_xray_tracing ? "Active" : "PassThrough"
  }

  tags = merge(
    local.common_tags,
    {
      Name         = local.lambda_function_name
      Type         = "Lambda"
      Runtime      = var.lambda_runtime
      Architecture = var.lambda_architecture
    }
  )

  depends_on = [
    aws_cloudwatch_log_group.lambda_log_group,
    aws_iam_role_policy_attachment.lambda_basic_execution,
    aws_iam_role_policy_attachment.lambda_vpc_execution,
    aws_iam_role_policy_attachment.lambda_secretsmanager,
    aws_iam_role_policy_attachment.lambda_xray,
    aws_iam_role_policy_attachment.lambda_insights,
    aws_iam_role_policy_attachment.lambda_sqs,
    # Removido: aws_db_instance.postgres - RDS compartilhado via remote state
    aws_secretsmanager_secret_version.db_current
  ]
}

# Lambda Function Alias
resource "aws_lambda_alias" "live" {
  name             = "live"
  description      = "Alias pointing to the live version"
  function_name    = aws_lambda_function.valida_pessoa.function_name
  function_version = "$LATEST"
}

