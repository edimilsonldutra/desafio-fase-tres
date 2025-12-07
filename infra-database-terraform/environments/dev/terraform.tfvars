# ============================================================================
# Terraform Variables Configuration
# Copy this file to terraform.tfvars and update with your values
# ============================================================================

# ============================================================================
# AWS Configuration
# ============================================================================
aws_region  = "us-east-1"
environment = "dev" # Options: dev, staging, prod

# ============================================================================
# Project Configuration
# ============================================================================
project_name = "valida-pessoa"

# ============================================================================
# JWT Configuration (IMPORTANT: Change in production!)
# ============================================================================
jwt_secret        = "change-this-secret-key-to-a-strong-random-32-character-minimum-string"
jwt_expiration_ms = 3600000 # 1 hour in milliseconds

# ============================================================================
# Lambda Configuration
# ============================================================================
lambda_timeout                        = 30
lambda_memory_size                    = 512
lambda_jar_path                       = "../../LambdaValidaPessoa/target/HelloWorld-1.0.jar"
lambda_reserved_concurrent_executions = -1 # -1 for unreserved, or set a specific number


# ============================================================================
# CloudWatch Configuration
# ============================================================================
log_retention_days     = 14    # Valid: 1,3,5,7,14,30,60,90,120,150,180,365,400,545,731,1827,3653
enable_lambda_insights = false # Enhanced monitoring (additional cost)

# ============================================================================
# API Gateway Configuration
# ============================================================================
api_throttle_burst_limit = 5000
api_throttle_rate_limit  = 10000
enable_api_cache         = false # Enable for production if needed
api_cache_ttl            = 300   # Cache TTL in seconds

# ============================================================================
# Alarm Configuration
# ============================================================================
enable_alarms                              = true
lambda_error_threshold                     = 5  # Number of errors before alarm
lambda_duration_alarm_threshold_percentage = 80 # % of timeout before alarm
alarm_evaluation_periods                   = 2  # Periods before alarm triggers

# ============================================================================
# Network Configuration - VPC is always created by vpc.tf
# ============================================================================
# Note: VPC, subnets, and security groups are automatically created
# No manual configuration needed here

# ============================================================================
# Feature Flags
# ============================================================================
enable_xray_tracing = false # Enable X-Ray for detailed tracing
create_sample_data  = true  # Create sample customers (dev only)
enable_cors         = true  # Enable CORS for API Gateway

# ============================================================================
# Additional Tags (optional)
# ============================================================================
additional_tags = {
  Team       = "DevOps"
  CostCenter = "Engineering"
  Contact    = "devops@example.com"
  # Add more tags as needed (max 50)
}

