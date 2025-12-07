# SNS Topic para CloudWatch Alarms

resource "aws_sns_topic" "alarms" {
  count = var.enable_alarms ? 1 : 0

  name              = "${local.name_prefix}-alarms"
  display_name      = "CloudWatch Alarms for ${local.name_prefix}"
  kms_master_key_id = var.enable_kms_encryption ? aws_kms_key.main[0].id : null

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-alarms"
      Type = "SNS-Topic"
    }
  )
}

# SNS Topic Policy
resource "aws_sns_topic_policy" "alarms" {
  count = var.enable_alarms ? 1 : 0

  arn = aws_sns_topic.alarms[0].arn

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudWatchPublish"
        Effect = "Allow"
        Principal = {
          Service = "cloudwatch.amazonaws.com"
        }
        Action = "SNS:Publish"
        Resource = aws_sns_topic.alarms[0].arn
      }
    ]
  })
}

# SNS Email Subscriptions
resource "aws_sns_topic_subscription" "alarm_email" {
  count = var.enable_alarms ? length(var.alarm_email_endpoints) : 0

  topic_arn = aws_sns_topic.alarms[0].arn
  protocol  = "email"
  endpoint  = var.alarm_email_endpoints[count.index]
}
