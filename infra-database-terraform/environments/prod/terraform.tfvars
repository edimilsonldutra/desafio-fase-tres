# ================================
# General Configuration - PRODUCTION
# ================================
aws_region   = "us-east-1"
environment  = "prod"
project_name = "oficina-mecanica"

# ================================
# Database Configuration
# ================================
db_name               = "oficina_db_prod"
db_username           = "dbadmin"
db_engine_version     = "15.5"
db_instance_class     = "db.r6g.large"
db_allocated_storage  = 100
db_max_allocated_storage = 500
db_storage_type       = "gp3"
multi_az              = true

# ================================
# Backup Configuration
# ================================
backup_retention_period = 30
backup_window          = "03:00-04:00"
maintenance_window     = "Sun:04:00-Sun:05:00"

# ================================
# Monitoring Configuration
# ================================
enable_enhanced_monitoring  = true
monitoring_interval         = 30
enable_performance_insights = true
enable_iam_auth            = true

# ================================
# Security Configuration
# ================================
ecs_security_group_id    = ""
lambda_security_group_id = ""
eks_security_group_id    = ""

# ================================
# Secrets Configuration
# ================================
enable_secret_rotation = true

# ================================
# Lambda Configuration
# ================================
jwt_secret                                = "production-jwt-secret-MUST-CHANGE-minimum-32-characters-required"
jwt_expiration_ms                         = 3600000
lambda_memory_size                        = 1024
lambda_jar_path                           = "../../lambda-auth-service/target/ValidaPessoa-1.0.jar"
lambda_reserved_concurrent_executions     = 100
lambda_timeout                            = 30
lambda_error_threshold                    = 3
lambda_duration_alarm_threshold_percentage = 70

# ================================
# API Gateway Configuration
# ================================
api_throttle_burst_limit = 10000
api_throttle_rate_limit  = 20000
enable_api_cache         = true
api_cache_ttl            = 300

# ================================
# Monitoring & Alarms
# ================================
enable_alarms            = true
alarm_evaluation_periods = 2
enable_lambda_insights   = true
log_retention_days       = 30

# ================================
# Tracing & Debug
# ================================
enable_xray_tracing = true
enable_cors         = true
create_sample_data  = false

# ================================
# Additional Tags
# ================================
additional_tags = {
  Team       = "DevOps"
  CostCenter = "Engineering"
  Contact    = "devops@example.com"
  Critical   = "true"
}
