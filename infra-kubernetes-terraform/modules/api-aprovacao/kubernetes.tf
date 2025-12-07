# ================================
# Kubernetes Deployment and Service for API Aprovação Orçamento
# ================================

# Namespace (using default or create specific)
resource "kubernetes_namespace" "api_aprovacao" {
  count = var.create_namespace ? 1 : 0

  metadata {
    name = var.namespace

    labels = {
      name        = var.namespace
      environment = var.environment
      managed-by  = "terraform"
    }
  }
}

# ConfigMap for application configuration
resource "kubernetes_config_map" "api_aprovacao_config" {
  metadata {
    name      = "${var.app_name}-config"
    namespace = var.namespace

    labels = local.common_labels
  }

  data = {
    "application.properties" = <<-EOT
      server.port=${var.container_port}
      api.principal.url=${var.api_principal_url}
      
      # Logging
      logging.level.root=${var.log_level}
      logging.level.br.com.grupo99=${var.log_level}
      
      # Actuator
      management.endpoints.web.exposure.include=health,info,metrics,prometheus
      management.endpoint.health.show-details=always
      management.metrics.export.prometheus.enabled=true
    EOT
  }
}

# Deployment
resource "kubernetes_deployment" "api_aprovacao" {
  metadata {
    name      = var.app_name
    namespace = var.namespace

    labels = local.common_labels
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = local.selector_labels
    }

    template {
      metadata {
        labels = merge(local.common_labels, local.selector_labels)
        
        annotations = {
          "prometheus.io/scrape" = "true"
          "prometheus.io/port"   = tostring(var.container_port)
          "prometheus.io/path"   = "/actuator/prometheus"
        }
      }

      spec {
        container {
          name  = var.app_name
          image = var.image_name

          port {
            name           = "http"
            container_port = var.container_port
            protocol       = "TCP"
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = var.environment
          }

          env {
            name = "JAVA_OPTS"
            value = join(" ", [
              "-Xms${var.java_memory_min}",
              "-Xmx${var.java_memory_max}",
              "-XX:+UseG1GC",
              "-XX:MaxGCPauseMillis=200",
              "-Dfile.encoding=UTF-8"
            ])
          }

          # Mount ConfigMap
          volume_mount {
            name       = "config"
            mount_path = "/config"
            read_only  = true
          }

          # Health checks
          liveness_probe {
            http_get {
              path = "/actuator/health/liveness"
              port = var.container_port
            }
            initial_delay_seconds = 60
            period_seconds        = 10
            timeout_seconds       = 3
            failure_threshold     = 3
          }

          readiness_probe {
            http_get {
              path = "/actuator/health/readiness"
              port = var.container_port
            }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 3
            failure_threshold     = 3
          }

          # Resource limits
          resources {
            requests = {
              cpu    = var.resources_requests_cpu
              memory = var.resources_requests_memory
            }
            limits = {
              cpu    = var.resources_limits_cpu
              memory = var.resources_limits_memory
            }
          }
        }

        # Volume for ConfigMap
        volume {
          name = "config"
          config_map {
            name = kubernetes_config_map.api_aprovacao_config.metadata[0].name
          }
        }

        # Image pull secrets (if needed)
        dynamic "image_pull_secrets" {
          for_each = var.image_pull_secrets
          content {
            name = image_pull_secrets.value
          }
        }
      }
    }

    strategy {
      type = "RollingUpdate"
      rolling_update {
        max_surge       = "25%"
        max_unavailable = "25%"
      }
    }
  }
}

# Service
resource "kubernetes_service" "api_aprovacao" {
  metadata {
    name      = var.app_name
    namespace = var.namespace

    labels = local.common_labels

    annotations = var.service_annotations
  }

  spec {
    type = var.service_type

    selector = local.selector_labels

    port {
      name        = "http"
      port        = var.service_port
      target_port = var.container_port
      protocol    = "TCP"
    }

    session_affinity = "ClientIP"
  }
}

# Horizontal Pod Autoscaler
resource "kubernetes_horizontal_pod_autoscaler_v2" "api_aprovacao" {
  count = var.enable_hpa ? 1 : 0

  metadata {
    name      = var.app_name
    namespace = var.namespace

    labels = local.common_labels
  }

  spec {
    scale_target_ref {
      api_version = "apps/v1"
      kind        = "Deployment"
      name        = kubernetes_deployment.api_aprovacao.metadata[0].name
    }

    min_replicas = var.hpa_min_replicas
    max_replicas = var.hpa_max_replicas

    metric {
      type = "Resource"
      resource {
        name = "cpu"
        target {
          type                = "Utilization"
          average_utilization = var.hpa_cpu_target
        }
      }
    }

    metric {
      type = "Resource"
      resource {
        name = "memory"
        target {
          type                = "Utilization"
          average_utilization = var.hpa_memory_target
        }
      }
    }
  }
}

# Ingress (optional)
resource "kubernetes_ingress_v1" "api_aprovacao" {
  count = var.create_ingress ? 1 : 0

  metadata {
    name      = var.app_name
    namespace = var.namespace

    labels = local.common_labels

    annotations = merge(
      {
        "kubernetes.io/ingress.class"                = var.ingress_class
        "cert-manager.io/cluster-issuer"             = var.cert_manager_issuer
        "nginx.ingress.kubernetes.io/rewrite-target" = "/"
      },
      var.ingress_annotations
    )
  }

  spec {
    dynamic "tls" {
      for_each = var.ingress_tls_enabled ? [1] : []
      content {
        hosts       = [var.ingress_host]
        secret_name = "${var.app_name}-tls"
      }
    }

    rule {
      host = var.ingress_host

      http {
        path {
          path      = var.ingress_path
          path_type = "Prefix"

          backend {
            service {
              name = kubernetes_service.api_aprovacao.metadata[0].name
              port {
                number = var.service_port
              }
            }
          }
        }
      }
    }
  }
}
