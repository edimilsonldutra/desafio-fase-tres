# ================================
# Variables - API Aprovação Orçamento Module
# ================================

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "project_name" {
  description = "Project name for resource naming"
  type        = string
  default     = "fiap-oficina"
}

variable "app_name" {
  description = "Application name"
  type        = string
  default     = "api-aprovacao-orcamento"
}

variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
  default     = "default"
}

variable "create_namespace" {
  description = "Whether to create the namespace"
  type        = bool
  default     = false
}

# ================================
# Image Configuration
# ================================

variable "image_name" {
  description = "Docker image name with tag"
  type        = string
}

variable "image_pull_secrets" {
  description = "List of image pull secrets"
  type        = list(string)
  default     = []
}

# ================================
# Deployment Configuration
# ================================

variable "replicas" {
  description = "Number of pod replicas"
  type        = number
  default     = 2

  validation {
    condition     = var.replicas >= 1 && var.replicas <= 10
    error_message = "Replicas must be between 1 and 10."
  }
}

variable "container_port" {
  description = "Container port"
  type        = number
  default     = 8081
}

variable "service_port" {
  description = "Service port"
  type        = number
  default     = 80
}

variable "service_type" {
  description = "Kubernetes service type"
  type        = string
  default     = "ClusterIP"

  validation {
    condition     = contains(["ClusterIP", "NodePort", "LoadBalancer"], var.service_type)
    error_message = "Service type must be ClusterIP, NodePort, or LoadBalancer."
  }
}

variable "service_annotations" {
  description = "Annotations for the service"
  type        = map(string)
  default     = {}
}

# ================================
# Application Configuration
# ================================

variable "api_principal_url" {
  description = "URL of the main application API"
  type        = string
  default     = "http://oficina-service.default.svc.cluster.local:8080"
}

variable "log_level" {
  description = "Application log level"
  type        = string
  default     = "INFO"

  validation {
    condition     = contains(["TRACE", "DEBUG", "INFO", "WARN", "ERROR"], var.log_level)
    error_message = "Log level must be TRACE, DEBUG, INFO, WARN, or ERROR."
  }
}

# ================================
# Java Configuration
# ================================

variable "java_memory_min" {
  description = "Minimum Java heap size"
  type        = string
  default     = "256m"
}

variable "java_memory_max" {
  description = "Maximum Java heap size"
  type        = string
  default     = "512m"
}

# ================================
# Resource Limits
# ================================

variable "resources_requests_cpu" {
  description = "CPU request"
  type        = string
  default     = "100m"
}

variable "resources_requests_memory" {
  description = "Memory request"
  type        = string
  default     = "256Mi"
}

variable "resources_limits_cpu" {
  description = "CPU limit"
  type        = string
  default     = "500m"
}

variable "resources_limits_memory" {
  description = "Memory limit"
  type        = string
  default     = "512Mi"
}

# ================================
# Horizontal Pod Autoscaler
# ================================

variable "enable_hpa" {
  description = "Enable Horizontal Pod Autoscaler"
  type        = bool
  default     = true
}

variable "hpa_min_replicas" {
  description = "Minimum number of replicas for HPA"
  type        = number
  default     = 2
}

variable "hpa_max_replicas" {
  description = "Maximum number of replicas for HPA"
  type        = number
  default     = 5
}

variable "hpa_cpu_target" {
  description = "Target CPU utilization percentage for HPA"
  type        = number
  default     = 70
}

variable "hpa_memory_target" {
  description = "Target memory utilization percentage for HPA"
  type        = number
  default     = 80
}

# ================================
# Ingress Configuration
# ================================

variable "create_ingress" {
  description = "Whether to create an Ingress resource"
  type        = bool
  default     = false
}

variable "ingress_class" {
  description = "Ingress class"
  type        = string
  default     = "nginx"
}

variable "ingress_host" {
  description = "Ingress hostname"
  type        = string
  default     = ""
}

variable "ingress_path" {
  description = "Ingress path"
  type        = string
  default     = "/"
}

variable "ingress_tls_enabled" {
  description = "Enable TLS for ingress"
  type        = bool
  default     = false
}

variable "cert_manager_issuer" {
  description = "Cert-manager cluster issuer"
  type        = string
  default     = "letsencrypt-prod"
}

variable "ingress_annotations" {
  description = "Additional annotations for ingress"
  type        = map(string)
  default     = {}
}

# ================================
# Tags
# ================================

variable "additional_tags" {
  description = "Additional tags for all resources"
  type        = map(string)
  default     = {}
}
