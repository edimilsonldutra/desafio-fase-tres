# ================================
# Outputs - API Aprovação Orçamento Module
# ================================

output "deployment_name" {
  description = "Name of the Kubernetes deployment"
  value       = kubernetes_deployment.api_aprovacao.metadata[0].name
}

output "service_name" {
  description = "Name of the Kubernetes service"
  value       = kubernetes_service.api_aprovacao.metadata[0].name
}

output "service_cluster_ip" {
  description = "Cluster IP of the service"
  value       = kubernetes_service.api_aprovacao.spec[0].cluster_ip
}

output "namespace" {
  description = "Kubernetes namespace"
  value       = var.namespace
}

output "replicas" {
  description = "Number of pod replicas"
  value       = var.replicas
}

output "image_name" {
  description = "Docker image being used"
  value       = var.image_name
}

output "service_url" {
  description = "Internal service URL"
  value       = "http://${kubernetes_service.api_aprovacao.metadata[0].name}.${var.namespace}.svc.cluster.local:${var.service_port}"
}

output "ingress_host" {
  description = "Ingress hostname (if created)"
  value       = var.create_ingress ? var.ingress_host : null
}

output "hpa_enabled" {
  description = "Whether HPA is enabled"
  value       = var.enable_hpa
}
