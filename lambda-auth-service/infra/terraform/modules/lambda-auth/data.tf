# Data sources for infrastructure configuration

# Get current AWS account information
data "aws_caller_identity" "current" {}

# Get current AWS region
data "aws_region" "current" {}

# Get available Availability Zones
data "aws_availability_zones" "available" {
  state = "available"
  
  # Exclude Local Zones and Wavelength Zones
  filter {
    name   = "opt-in-status"
    values = ["opt-in-not-required"]
  }
}

# Get AWS partition (aws, aws-cn, aws-us-gov)
data "aws_partition" "current" {}

# Get latest Amazon Linux 2 AMI (for potential EC2 bastion host)
data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# Lambda Insights extension layer ARN
# https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/Lambda-Insights-extension-versionsx86-64.html
data "aws_lambda_layer_version" "lambda_insights" {
  count = var.enable_lambda_insights ? 1 : 0

  layer_name = "LambdaInsightsExtension"
  
  # Get the latest version
  compatible_runtime = var.lambda_runtime
}

# IAM policy document for VPC Flow Logs
data "aws_iam_policy_document" "vpc_flow_logs_assume_role" {
  count = var.enable_vpc_flow_logs ? 1 : 0

  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["vpc-flow-logs.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "vpc_flow_logs" {
  count = var.enable_vpc_flow_logs ? 1 : 0

  statement {
    effect = "Allow"

    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:DescribeLogGroups",
      "logs:DescribeLogStreams",
    ]

    resources = ["*"]
  }
}
