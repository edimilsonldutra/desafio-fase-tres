variable "secret_name" {
  description = "Name of the secret"
  type        = string
}

variable "description" {
  description = "Description of the secret"
  type        = string
  default     = ""
}

variable "secret_string" {
  description = "Secret string value (JSON format recommended)"
  type        = string
  default     = null
  sensitive   = true
}

variable "secret_binary" {
  description = "Secret binary value (base64-encoded)"
  type        = string
  default     = null
  sensitive   = true
}

variable "kms_key_id" {
  description = "KMS key ID to encrypt the secret"
  type        = string
  default     = null
}

variable "recovery_window_in_days" {
  description = "Number of days to recover the secret after deletion"
  type        = number
  default     = 30
}

variable "enable_rotation" {
  description = "Enable automatic secret rotation"
  type        = bool
  default     = false
}

variable "rotation_lambda_arn" {
  description = "ARN of Lambda function for rotation"
  type        = string
  default     = ""
}

variable "rotation_days" {
  description = "Number of days between automatic rotations"
  type        = number
  default     = 30
}

variable "tags" {
  description = "A map of tags to add to all resources"
  type        = map(string)
  default     = {}
}
