# IAM Role for Lambda
resource "aws_iam_role" "lambda_role" {
  name               = local.lambda_role_name
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json

  tags = merge(
    local.common_tags,
    {
      Name = local.lambda_role_name
      Type = "IAM-Role"
    }
  )
}

# Lambda Assume Role Policy
data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

# Secrets Manager Access Policy for Lambda (com Least Privilege)
data "aws_iam_policy_document" "lambda_secretsmanager" {
  statement {
    sid    = "GetDatabaseSecret"
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret"
    ]
    resources = [
      aws_secretsmanager_secret.db.arn
    ]
    
    condition {
      test     = "StringEquals"
      variable = "aws:RequestedRegion"
      values   = [data.aws_region.current.name]
    }
  }

  # Permite descriptografia com KMS apenas se KMS estiver habilitado
  dynamic "statement" {
    for_each = var.enable_kms_encryption ? [1] : []
    content {
      sid    = "DecryptWithKMS"
      effect = "Allow"
      actions = [
        "kms:Decrypt",
        "kms:DescribeKey"
      ]
      resources = [
        aws_kms_key.main[0].arn
      ]
    }
  }
}

resource "aws_iam_policy" "lambda_secretsmanager_policy" {
  name        = "${local.lambda_role_name}-secretsmanager-policy"
  description = "Policy for Lambda to access Secrets Manager for DB credentials"
  policy      = data.aws_iam_policy_document.lambda_secretsmanager.json

  tags = merge(
    local.common_tags,
    {
      Name = "${local.lambda_role_name}-secretsmanager-policy"
      Type = "IAM-Policy"
    }
  )
}

# X-Ray Policy para Lambda
data "aws_iam_policy_document" "lambda_xray" {
  count = var.enable_xray_tracing ? 1 : 0

  statement {
    sid    = "XRayAccess"
    effect = "Allow"
    actions = [
      "xray:PutTraceSegments",
      "xray:PutTelemetryRecords"
    ]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "lambda_xray_policy" {
  count = var.enable_xray_tracing ? 1 : 0

  name        = "${local.lambda_role_name}-xray-policy"
  description = "Policy for Lambda to write to X-Ray"
  policy      = data.aws_iam_policy_document.lambda_xray[0].json

  tags = merge(
    local.common_tags,
    {
      Name = "${local.lambda_role_name}-xray-policy"
      Type = "IAM-Policy"
    }
  )
}

# Lambda Insights Policy
data "aws_iam_policy_document" "lambda_insights" {
  count = var.enable_lambda_insights ? 1 : 0

  statement {
    sid    = "CloudWatchInsights"
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = [
      "arn:${data.aws_partition.current.partition}:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda-insights:*"
    ]
  }
}

resource "aws_iam_policy" "lambda_insights_policy" {
  count = var.enable_lambda_insights ? 1 : 0

  name        = "${local.lambda_role_name}-insights-policy"
  description = "Policy for Lambda Insights"
  policy      = data.aws_iam_policy_document.lambda_insights[0].json

  tags = merge(
    local.common_tags,
    {
      Name = "${local.lambda_role_name}-insights-policy"
      Type = "IAM-Policy"
    }
  )
}

# SQS DLQ Policy para Lambda
data "aws_iam_policy_document" "lambda_sqs" {
  statement {
    sid    = "SendToDeadLetterQueue"
    effect = "Allow"
    actions = [
      "sqs:SendMessage"
    ]
    resources = [
      aws_sqs_queue.lambda_dlq.arn
    ]
  }
}

resource "aws_iam_policy" "lambda_sqs_policy" {
  name        = "${local.lambda_role_name}-sqs-policy"
  description = "Policy for Lambda to send messages to DLQ"
  policy      = data.aws_iam_policy_document.lambda_sqs.json

  tags = merge(
    local.common_tags,
    {
      Name = "${local.lambda_role_name}-sqs-policy"
      Type = "IAM-Policy"
    }
  )
}

# Attach AWS managed policy for Lambda basic execution
resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Attach AWS managed policy for Lambda VPC execution
resource "aws_iam_role_policy_attachment" "lambda_vpc_execution" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

# Attach Secrets Manager policy to Lambda role
resource "aws_iam_role_policy_attachment" "lambda_secretsmanager" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.lambda_secretsmanager_policy.arn
}

# Attach X-Ray policy to Lambda role (se habilitado)
resource "aws_iam_role_policy_attachment" "lambda_xray" {
  count = var.enable_xray_tracing ? 1 : 0

  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.lambda_xray_policy[0].arn
}

# Attach Lambda Insights policy to Lambda role (se habilitado)
resource "aws_iam_role_policy_attachment" "lambda_insights" {
  count = var.enable_lambda_insights ? 1 : 0

  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.lambda_insights_policy[0].arn
}

# Attach SQS DLQ policy to Lambda role
resource "aws_iam_role_policy_attachment" "lambda_sqs" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.lambda_sqs_policy.arn
}
