# CloudWatch Alarms Centralizados

# ========================================
# Lambda Alarms
# ========================================

# Lambda Errors
resource "aws_cloudwatch_metric_alarm" "lambda_errors" {
  count = var.enable_alarms ? 1 : 0

  alarm_name          = "${local.lambda_function_name}-errors"
  alarm_description   = "Lambda function errors exceed threshold"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = var.alarm_evaluation_periods
  metric_name         = "Errors"
  namespace           = "AWS/Lambda"
  period              = 60
  statistic           = "Sum"
  threshold           = var.lambda_error_threshold
  treat_missing_data  = "notBreaching"

  dimensions = {
    FunctionName = aws_lambda_function.valida_pessoa.function_name
  }

  alarm_actions = [aws_sns_topic.alarms[0].arn]
  ok_actions    = [aws_sns_topic.alarms[0].arn]

  tags = local.common_tags
}

# Lambda Duration
resource "aws_cloudwatch_metric_alarm" "lambda_duration" {
  count = var.enable_alarms ? 1 : 0

  alarm_name          = "${local.lambda_function_name}-duration"
  alarm_description   = "Lambda function duration exceeds ${var.lambda_duration_alarm_threshold_percentage}% of timeout"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = var.alarm_evaluation_periods
  metric_name         = "Duration"
  namespace           = "AWS/Lambda"
  period              = 60
  statistic           = "Average"
  threshold           = var.lambda_timeout * 1000 * (var.lambda_duration_alarm_threshold_percentage / 100)
  treat_missing_data  = "notBreaching"

  dimensions = {
    FunctionName = aws_lambda_function.valida_pessoa.function_name
  }

  alarm_actions = [aws_sns_topic.alarms[0].arn]
  ok_actions    = [aws_sns_topic.alarms[0].arn]

  tags = local.common_tags
}

# Lambda Throttles
resource "aws_cloudwatch_metric_alarm" "lambda_throttles" {
  count = var.enable_alarms ? 1 : 0

  alarm_name          = "${local.lambda_function_name}-throttles"
  alarm_description   = "Lambda function is being throttled"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "Throttles"
  namespace           = "AWS/Lambda"
  period              = 60
  statistic           = "Sum"
  threshold           = 5
  treat_missing_data  = "notBreaching"

  dimensions = {
    FunctionName = aws_lambda_function.valida_pessoa.function_name
  }

  alarm_actions = [aws_sns_topic.alarms[0].arn]
  ok_actions    = [aws_sns_topic.alarms[0].arn]

  tags = local.common_tags
}

# Lambda Concurrent Executions
resource "aws_cloudwatch_metric_alarm" "lambda_concurrent_executions" {
  count = var.enable_alarms ? 1 : 0

  alarm_name          = "${local.lambda_function_name}-concurrent-executions"
  alarm_description   = "Lambda concurrent executions exceed 80% of reserved capacity"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "ConcurrentExecutions"
  namespace           = "AWS/Lambda"
  period              = 60
  statistic           = "Maximum"
  threshold           = var.lambda_reserved_concurrent_executions != null ? var.lambda_reserved_concurrent_executions * 0.8 : 800
  treat_missing_data  = "notBreaching"

  dimensions = {
    FunctionName = aws_lambda_function.valida_pessoa.function_name
  }

  alarm_actions = [aws_sns_topic.alarms[0].arn]
  ok_actions    = [aws_sns_topic.alarms[0].arn]

  tags = local.common_tags
}

# ========================================
# RDS Alarms
# ========================================
# Removido: Este módulo usa RDS compartilhado via remote state
# RDS alarms devem ser gerenciados no módulo infra-database-terraform

# # RDS CPU Utilization
# resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
#   count = var.enable_alarms ? 1 : 0
#   alarm_name          = "${local.db_identifier}-cpu-utilization"
#   alarm_description   = "RDS CPU utilization is too high"
#   comparison_operator = "GreaterThanThreshold"
#   evaluation_periods  = var.alarm_evaluation_periods
#   metric_name         = "CPUUtilization"
#   namespace           = "AWS/RDS"
#   period              = 300
#   statistic           = "Average"
#   threshold           = 80
#   treat_missing_data  = "notBreaching"
#   dimensions = {
#     DBInstanceIdentifier = aws_db_instance.postgres.id
#   }
#   alarm_actions = [aws_sns_topic.alarms[0].arn]
#   ok_actions    = [aws_sns_topic.alarms[0].arn]
#   tags = local.common_tags
# }

# # RDS Free Storage Space
# resource "aws_cloudwatch_metric_alarm" "rds_storage" {
#   count = var.enable_alarms ? 1 : 0
#   alarm_name          = "${local.db_identifier}-low-storage"
#   alarm_description   = "RDS free storage space is running low"
#   comparison_operator = "LessThanThreshold"
#   evaluation_periods  = 2
#   metric_name         = "FreeStorageSpace"
#   namespace           = "AWS/RDS"
#   period              = 300
#   statistic           = "Average"
#   threshold           = 5368709120 # 5 GB in bytes
#   treat_missing_data  = "notBreaching"
#   dimensions = {
#     DBInstanceIdentifier = aws_db_instance.postgres.id
#   }
#   alarm_actions = [aws_sns_topic.alarms[0].arn]
#   ok_actions    = [aws_sns_topic.alarms[0].arn]
#   tags = local.common_tags
# }

# # RDS Database Connections
# resource "aws_cloudwatch_metric_alarm" "rds_connections" {
#   count = var.enable_alarms ? 1 : 0
#   alarm_name          = "${local.db_identifier}-high-connections"
#   alarm_description   = "RDS database connections are high"
#   comparison_operator = "GreaterThanThreshold"
#   evaluation_periods  = 2
#   metric_name         = "DatabaseConnections"
#   namespace           = "AWS/RDS"
#   period              = 300
#   statistic           = "Average"
#   threshold           = 80 # Adjust based on instance class
#   treat_missing_data  = "notBreaching"
#   dimensions = {
#     DBInstanceIdentifier = aws_db_instance.postgres.id
#   }
#   alarm_actions = [aws_sns_topic.alarms[0].arn]
#   ok_actions    = [aws_sns_topic.alarms[0].arn]
#   tags = local.common_tags
# }

# # RDS Read Latency
# resource "aws_cloudwatch_metric_alarm" "rds_read_latency" {
#   count = var.enable_alarms ? 1 : 0
#   alarm_name          = "${local.db_identifier}-read-latency"
#   alarm_description   = "RDS read latency is high"
#   comparison_operator = "GreaterThanThreshold"
#   evaluation_periods  = 2
#   metric_name         = "ReadLatency"
#   namespace           = "AWS/RDS"
#   period              = 300
#   statistic           = "Average"
#   threshold           = 0.1 # 100ms
#   treat_missing_data  = "notBreaching"
#   dimensions = {
#     DBInstanceIdentifier = aws_db_instance.postgres.id
#   }
#   alarm_actions = [aws_sns_topic.alarms[0].arn]
#   ok_actions    = [aws_sns_topic.alarms[0].arn]
#   tags = local.common_tags
# }

# # RDS Write Latency
# resource "aws_cloudwatch_metric_alarm" "rds_write_latency" {
#   count = var.enable_alarms ? 1 : 0
#   alarm_name          = "${local.db_identifier}-write-latency"
#   alarm_description   = "RDS write latency is high"
#   comparison_operator = "GreaterThanThreshold"
#   evaluation_periods  = 2
#   metric_name         = "WriteLatency"
#   namespace           = "AWS/RDS"
#   period              = 300
#   statistic           = "Average"
#   threshold           = 0.1 # 100ms
#   treat_missing_data  = "notBreaching"
#   dimensions = {
#     DBInstanceIdentifier = aws_db_instance.postgres.id
#   }
#   alarm_actions = [aws_sns_topic.alarms[0].arn]
#   ok_actions    = [aws_sns_topic.alarms[0].arn]
#   tags = local.common_tags
# }

# ========================================
# API Gateway Alarms
# ========================================

# API Gateway 4xx Errors
resource "aws_cloudwatch_metric_alarm" "api_4xx_errors" {
  count = var.enable_alarms ? 1 : 0

  alarm_name          = "${local.api_gateway_name}-4xx-errors"
  alarm_description   = "API Gateway 4xx errors exceed threshold"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "4XXError"
  namespace           = "AWS/ApiGateway"
  period              = 300
  statistic           = "Sum"
  threshold           = 50
  treat_missing_data  = "notBreaching"

  dimensions = {
    ApiName = aws_api_gateway_rest_api.api.name
    Stage   = aws_api_gateway_stage.stage.stage_name
  }

  alarm_actions = [aws_sns_topic.alarms[0].arn]
  ok_actions    = [aws_sns_topic.alarms[0].arn]

  tags = local.common_tags
}

# API Gateway 5xx Errors
resource "aws_cloudwatch_metric_alarm" "api_5xx_errors" {
  count = var.enable_alarms ? 1 : 0

  alarm_name          = "${local.api_gateway_name}-5xx-errors"
  alarm_description   = "API Gateway 5xx errors detected"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "5XXError"
  namespace           = "AWS/ApiGateway"
  period              = 300
  statistic           = "Sum"
  threshold           = 10
  treat_missing_data  = "notBreaching"

  dimensions = {
    ApiName = aws_api_gateway_rest_api.api.name
    Stage   = aws_api_gateway_stage.stage.stage_name
  }

  alarm_actions = [aws_sns_topic.alarms[0].arn]
  ok_actions    = [aws_sns_topic.alarms[0].arn]

  tags = local.common_tags
}

# API Gateway Latency
resource "aws_cloudwatch_metric_alarm" "api_latency" {
  count = var.enable_alarms ? 1 : 0

  alarm_name          = "${local.api_gateway_name}-latency"
  alarm_description   = "API Gateway latency is high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "Latency"
  namespace           = "AWS/ApiGateway"
  period              = 300
  statistic           = "Average"
  threshold           = 3000 # 3 seconds
  treat_missing_data  = "notBreaching"

  dimensions = {
    ApiName = aws_api_gateway_rest_api.api.name
    Stage   = aws_api_gateway_stage.stage.stage_name
  }

  alarm_actions = [aws_sns_topic.alarms[0].arn]
  ok_actions    = [aws_sns_topic.alarms[0].arn]

  tags = local.common_tags
}
