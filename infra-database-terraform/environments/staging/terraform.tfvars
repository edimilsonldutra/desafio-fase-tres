# ================================
# General Configuration - STAGING
# ================================
aws_region   = "us-east-1"
environment  = "staging"
project_name = "oficina-mecanica"

# ================================
# Database Configuration
# ================================
db_name               = "oficina_db_staging"
db_username           = "dbadmin"
db_engine_version     = "15.5"
db_instance_class     = "db.t3.small"
db_allocated_storage  = 30
db_max_allocated_storage = 100
db_storage_type       = "gp3"
multi_az              = true

# ================================
# Backup Configuration
# ================================
backup_retention_period = 14
backup_window          = "03:00-04:00"
maintenance_window     = "Mon:04:00-Mon:05:00"

# ================================
# Monitoring Configuration
# ================================
enable_enhanced_monitoring  = true
monitoring_interval         = 60
enable_performance_insights = true
enable_iam_auth            = false

# ================================
# Security Configuration
# ================================
ecs_security_group_id    = ""
lambda_security_group_id = ""
eks_security_group_id    = ""

# ================================
# Secrets Configuration
# ================================
enable_secret_rotation = false

# ================================
# Lambda Configuration
# ================================
jwt_secret                                = "staging-jwt-secret-change-in-production-minimum-32-characters"
jwt_expiration_ms                         = 3600000
lambda_memory_size                        = 512
lambda_jar_path                           = "../../lambda-auth-service/target/ValidaPessoa-1.0.jar"
lambda_reserved_concurrent_executions     = -1
lambda_timeout                            = 30
lambda_error_threshold                    = 5
lambda_duration_alarm_threshold_percentage = 80

# ================================
# API Gateway Configuration
# ================================
api_throttle_burst_limit = 5000
api_throttle_rate_limit  = 10000
enable_api_cache         = false
api_cache_ttl            = 300

# ================================
# Monitoring & Alarms
# ================================
enable_alarms            = true
alarm_evaluation_periods = 2
enable_lambda_insights   = false
log_retention_days       = 14

# ================================
# Tracing & Debug
# ================================
enable_xray_tracing = false
enable_cors         = true
create_sample_data  = false

# ================================
# Additional Tags
# ================================
additional_tags = {
  Team       = "DevOps"
  CostCenter = "Engineering"
  Contact    = "devops@example.com"
}
