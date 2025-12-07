# ================================
# Secrets Module Variables
# ================================

variable "secret_name" {
  description = "Name of the secret in Secrets Manager"
  type        = string
}

variable "description" {
  description = "Description of the secret"
  type        = string
  default     = ""
}

variable "secret_value" {
  description = "Secret value (JSON encoded)"
  type        = string
  sensitive   = true
}

variable "kms_key_id" {
  description = "KMS key ID for encryption"
  type        = string
  default     = null
}

variable "enable_rotation" {
  description = "Enable automatic secret rotation"
  type        = bool
  default     = false
}

variable "rotation_days" {
  description = "Number of days between automatic rotations"
  type        = number
  default     = 30
}

variable "rotation_lambda_arn" {
  description = "ARN of the Lambda function for rotation"
  type        = string
  default     = null
}

variable "recovery_window_in_days" {
  description = "Number of days to retain deleted secret"
  type        = number
  default     = 30
}

variable "tags" {
  description = "Tags to apply to the secret"
  type        = map(string)
  default     = {}
}
