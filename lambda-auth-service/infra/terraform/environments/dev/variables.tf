# ================================
# Variables - Development Environment
# ================================

variable "jwt_secret" {
  description = "Secret key for JWT generation (minimum 32 characters)"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.jwt_secret) >= 32
    error_message = "JWT secret must be at least 32 characters long."
  }
}

variable "db_password" {
  description = "Database password for RDS instance"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.db_password) >= 8
    error_message = "Database password must be at least 8 characters long."
  }
}
