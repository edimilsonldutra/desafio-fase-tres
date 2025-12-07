# ========================================
# CI/CD Resources - GitHub Actions
# ========================================
# This file creates all necessary resources for GitHub Actions CI/CD:
# - IAM Users for GitHub Actions (dev and prod)
# - S3 Buckets for deployment artifacts
# - IAM Policies with minimal required permissions
# - Access Keys (outputs are sensitive)
# ========================================

# ---------------------------------------
# S3 Buckets for Deployment
# ---------------------------------------

# Development Deployment Bucket
resource "aws_s3_bucket" "deployment_dev" {
  bucket = "${var.project_name}-deploy-dev-${data.aws_caller_identity.current.account_id}"

  tags = merge(
    local.common_tags,
    {
      Name        = "Lambda Deployment Bucket - Dev"
      Environment = "dev"
      Purpose     = "CI/CD Deployment Artifacts"
    }
  )
}

# Production Deployment Bucket
resource "aws_s3_bucket" "deployment_prod" {
  bucket = "${var.project_name}-deploy-prod-${data.aws_caller_identity.current.account_id}"

  tags = merge(
    local.common_tags,
    {
      Name        = "Lambda Deployment Bucket - Prod"
      Environment = "prod"
      Purpose     = "CI/CD Deployment Artifacts"
    }
  )
}

# Enable versioning on dev bucket
resource "aws_s3_bucket_versioning" "deployment_dev" {
  bucket = aws_s3_bucket.deployment_dev.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Enable versioning on prod bucket
resource "aws_s3_bucket_versioning" "deployment_prod" {
  bucket = aws_s3_bucket.deployment_prod.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Server-side encryption for dev bucket
resource "aws_s3_bucket_server_side_encryption_configuration" "deployment_dev" {
  bucket = aws_s3_bucket.deployment_dev.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Server-side encryption for prod bucket
resource "aws_s3_bucket_server_side_encryption_configuration" "deployment_prod" {
  bucket = aws_s3_bucket.deployment_prod.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Block public access for dev bucket
resource "aws_s3_bucket_public_access_block" "deployment_dev" {
  bucket = aws_s3_bucket.deployment_dev.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Block public access for prod bucket
resource "aws_s3_bucket_public_access_block" "deployment_prod" {
  bucket = aws_s3_bucket.deployment_prod.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Lifecycle rule for dev bucket (cleanup old artifacts after 30 days)
resource "aws_s3_bucket_lifecycle_configuration" "deployment_dev" {
  bucket = aws_s3_bucket.deployment_dev.id

  rule {
    id     = "cleanup-old-artifacts"
    status = "Enabled"

    filter {}

    expiration {
      days = 30
    }

    noncurrent_version_expiration {
      noncurrent_days = 7
    }
  }
}

# Lifecycle rule for prod bucket (keep artifacts for 90 days)
resource "aws_s3_bucket_lifecycle_configuration" "deployment_prod" {
  bucket = aws_s3_bucket.deployment_prod.id

  rule {
    id     = "cleanup-old-artifacts"
    status = "Enabled"

    filter {}

    expiration {
      days = 90
    }

    noncurrent_version_expiration {
      noncurrent_days = 30
    }
  }
}

# ---------------------------------------
# IAM User for GitHub Actions - Development
# ---------------------------------------

resource "aws_iam_user" "github_actions_dev" {
  name = "github-actions-${var.project_name}-dev"
  path = "/cicd/"

  tags = merge(
    local.common_tags,
    {
      Name        = "GitHub Actions User - Dev"
      Environment = "dev"
      Purpose     = "CI/CD Automation"
    }
  )
}

# IAM Policy for GitHub Actions - Development
data "aws_iam_policy_document" "github_actions_dev" {
  # Lambda permissions
  statement {
    sid    = "LambdaManagement"
    effect = "Allow"
    actions = [
      "lambda:CreateFunction",
      "lambda:UpdateFunctionCode",
      "lambda:UpdateFunctionConfiguration",
      "lambda:GetFunction",
      "lambda:GetFunctionConfiguration",
      "lambda:ListFunctions",
      "lambda:PublishVersion",
      "lambda:CreateAlias",
      "lambda:UpdateAlias",
      "lambda:GetAlias",
      "lambda:AddPermission",
      "lambda:RemovePermission",
      "lambda:InvokeFunction",
      "lambda:TagResource",
      "lambda:UntagResource",
      "lambda:ListTags"
    ]
    resources = [
      "arn:aws:lambda:${var.aws_region}:${data.aws_caller_identity.current.account_id}:function:${var.project_name}-*-dev"
    ]
  }

  # S3 deployment bucket permissions
  statement {
    sid    = "S3DeploymentBucket"
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:DeleteObject",
      "s3:ListBucket",
      "s3:GetBucketVersioning",
      "s3:GetObjectVersion"
    ]
    resources = [
      aws_s3_bucket.deployment_dev.arn,
      "${aws_s3_bucket.deployment_dev.arn}/*"
    ]
  }

  # IAM permissions (for Lambda role)
  statement {
    sid    = "IAMPassRole"
    effect = "Allow"
    actions = [
      "iam:PassRole",
      "iam:GetRole"
    ]
    resources = [
      aws_iam_role.lambda_role.arn
    ]
  }

  # API Gateway permissions
  statement {
    sid    = "APIGatewayManagement"
    effect = "Allow"
    actions = [
      "apigateway:GET",
      "apigateway:POST",
      "apigateway:PUT",
      "apigateway:PATCH",
      "apigateway:DELETE"
    ]
    resources = [
      "arn:aws:apigateway:${var.aws_region}::/restapis/*",
      "arn:aws:apigateway:${var.aws_region}::/restapis"
    ]
  }

  # CloudWatch Logs permissions
  statement {
    sid    = "CloudWatchLogs"
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:DescribeLogGroups",
      "logs:PutRetentionPolicy"
    ]
    resources = [
      "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${var.project_name}-*-dev*"
    ]
  }
}

resource "aws_iam_user_policy" "github_actions_dev" {
  name   = "github-actions-${var.project_name}-dev-policy"
  user   = aws_iam_user.github_actions_dev.name
  policy = data.aws_iam_policy_document.github_actions_dev.json
}

# Access Key for GitHub Actions - Development
resource "aws_iam_access_key" "github_actions_dev" {
  user = aws_iam_user.github_actions_dev.name
}

# ---------------------------------------
# IAM User for GitHub Actions - Production
# ---------------------------------------

resource "aws_iam_user" "github_actions_prod" {
  name = "github-actions-${var.project_name}-prod"
  path = "/cicd/"

  tags = merge(
    local.common_tags,
    {
      Name        = "GitHub Actions User - Prod"
      Environment = "prod"
      Purpose     = "CI/CD Automation"
    }
  )
}

# IAM Policy for GitHub Actions - Production
data "aws_iam_policy_document" "github_actions_prod" {
  # Lambda permissions
  statement {
    sid    = "LambdaManagement"
    effect = "Allow"
    actions = [
      "lambda:CreateFunction",
      "lambda:UpdateFunctionCode",
      "lambda:UpdateFunctionConfiguration",
      "lambda:GetFunction",
      "lambda:GetFunctionConfiguration",
      "lambda:ListFunctions",
      "lambda:PublishVersion",
      "lambda:CreateAlias",
      "lambda:UpdateAlias",
      "lambda:GetAlias",
      "lambda:AddPermission",
      "lambda:RemovePermission",
      "lambda:InvokeFunction",
      "lambda:TagResource",
      "lambda:UntagResource",
      "lambda:ListTags"
    ]
    resources = [
      "arn:aws:lambda:${var.aws_region}:${data.aws_caller_identity.current.account_id}:function:${var.project_name}-*-prod"
    ]
  }

  # S3 deployment bucket permissions
  statement {
    sid    = "S3DeploymentBucket"
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:DeleteObject",
      "s3:ListBucket",
      "s3:GetBucketVersioning",
      "s3:GetObjectVersion"
    ]
    resources = [
      aws_s3_bucket.deployment_prod.arn,
      "${aws_s3_bucket.deployment_prod.arn}/*"
    ]
  }

  # IAM permissions (for Lambda role)
  statement {
    sid    = "IAMPassRole"
    effect = "Allow"
    actions = [
      "iam:PassRole",
      "iam:GetRole"
    ]
    resources = [
      aws_iam_role.lambda_role.arn
    ]
  }

  # API Gateway permissions
  statement {
    sid    = "APIGatewayManagement"
    effect = "Allow"
    actions = [
      "apigateway:GET",
      "apigateway:POST",
      "apigateway:PUT",
      "apigateway:PATCH",
      "apigateway:DELETE"
    ]
    resources = [
      "arn:aws:apigateway:${var.aws_region}::/restapis/*",
      "arn:aws:apigateway:${var.aws_region}::/restapis"
    ]
  }

  # CloudWatch Logs permissions
  statement {
    sid    = "CloudWatchLogs"
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:DescribeLogGroups",
      "logs:PutRetentionPolicy"
    ]
    resources = [
      "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${var.project_name}-*-prod*"
    ]
  }
}

resource "aws_iam_user_policy" "github_actions_prod" {
  name   = "github-actions-${var.project_name}-prod-policy"
  user   = aws_iam_user.github_actions_prod.name
  policy = data.aws_iam_policy_document.github_actions_prod.json
}

# Access Key for GitHub Actions - Production
resource "aws_iam_access_key" "github_actions_prod" {
  user = aws_iam_user.github_actions_prod.name
}

# ---------------------------------------
# Data Sources
# ---------------------------------------
# Removido: data source já declarado em data.tf
