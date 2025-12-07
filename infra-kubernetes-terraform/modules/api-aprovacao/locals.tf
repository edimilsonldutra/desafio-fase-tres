# ================================
# Local Variables
# ================================

locals {
  common_labels = {
    "app.kubernetes.io/name"       = var.app_name
    "app.kubernetes.io/instance"   = "${var.app_name}-${var.environment}"
    "app.kubernetes.io/version"    = "1.0.0"
    "app.kubernetes.io/component"  = "api"
    "app.kubernetes.io/part-of"    = var.project_name
    "app.kubernetes.io/managed-by" = "terraform"
    "environment"                  = var.environment
  }

  selector_labels = {
    "app" = var.app_name
    "env" = var.environment
  }
}
