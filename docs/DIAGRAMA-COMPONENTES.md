# 🏗️ Diagrama de Componentes - Arquitetura Cloud-Native Completa

**Projeto**: Sistema de Gestão de Oficina Mecânica  
**Data**: 2025-12-07  
**Versão**: 2.0  
**Autor**: Edimilson L. Dutra

---

## 📋 Visão Geral

Este documento apresenta a arquitetura completa de componentes do sistema de gestão de oficina mecânica, incluindo **nuvem AWS**, **APIs**, **banco de dados** e **monitoramento com New Relic**.

---

## 🌐 Diagrama de Componentes - Arquitetura Completa

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                          AWS CLOUD (us-east-1)                                               │
│                                                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐   │
│  │                                    CAMADA DE ENTRADA (Edge Layer)                                     │   │
│  │  ┌────────────────────────────────────────────────────────────────────────────────────────────────┐  │   │
│  │  │  CloudFront CDN + AWS WAF                                                                       │  │   │
│  │  │  - SSL/TLS Termination                                                                          │  │   │
│  │  │  - DDoS Protection (AWS Shield)                                                                 │  │   │
│  │  │  - Rate Limiting, SQL Injection & XSS Protection                                                │  │   │
│  │  └────────────────────────────────┬───────────────────────────────────────────────────────────────┘  │   │
│  │                                   │                                                                   │   │
│  │  ┌────────────────────────────────▼───────────────────────────────────────────────────────────────┐  │   │
│  │  │  API Gateway (HTTP API v2)                                                                      │  │   │
│  │  │  Routes:                                                                                        │  │   │
│  │  │  POST   /auth/validate             → Lambda Auth Service                                      │  │   │
│  │  │  GET/POST /api/v1/*                → ALB → EKS Ingress                                         │  │   │
│  │  │  - Throttling: 10000 req/s | CORS | Custom Domain: api.oficina.com                            │  │   │
│  │  └────────────────┬─────────────────────────────────┬─────────────────────────────────────────────┘  │   │
│  └────────────────────┼─────────────────────────────────┼────────────────────────────────────────────────┘   │
│                       │                                 │                                                    │
│  ┌────────────────────▼────────────────┐   ┌───────────▼────────────────────────────────────────────────┐   │
│  │  CAMADA SERVERLESS                 │   │  CAMADA DE MICROSERVIÇOS (EKS)                             │   │
│  │  ┌──────────────────────────────┐  │   │  ┌──────────────────────────────────────────────────────┐  │   │
│  │  │  Lambda: Auth Service        │  │   │  │  Application Load Balancer (ALB)                     │  │   │
│  │  │  - Runtime: Java 21          │  │   │  │  - Health Checks | SSL | Target Groups               │  │   │
│  │  │  - Memory: 512 MB            │  │   │  └──────────────┬───────────────────────────────────────┘  │   │
│  │  │  - Timeout: 30s              │  │   │                 │                                           │   │
│  │  │  - Concurrency: 100          │  │   │  ┌──────────────▼───────────────────────────────────────┐  │   │
│  │  │  - New Relic Integration     │  │   │  │  EKS Cluster (Kubernetes 1.28)                       │  │   │
│  │  │  Functions:                  │  │   │  │  Namespace: oficina-service                          │  │   │
│  │  │  1. Validar CPF no RDS       │  │   │  │                                                      │  │   │
│  │  │  2. Gerar JWT Token          │  │   │  │  ┌────────────────────────────────────────────────┐  │  │   │
│  │  │  3. Retornar dados cliente   │  │   │  │  │ Deployment: oficina-service (HPA 2-10 replicas)│  │  │   │
│  │  └──────────┬───────────────────┘  │   │  │  │                                                │  │  │   │
│  │             │                      │   │  │  │ ┌────────────────────────────────────────────┐ │  │  │   │
│  │  ┌──────────▼───────────────────┐  │   │  │  │ │ Pod: oficina-service-xxx                   │ │  │  │   │
│  │  │  CloudWatch & X-Ray          │  │   │  │  │ │ ┌────────────────────────────────────────┐ │ │  │  │   │
│  │  │  - Lambda Logs & Traces      │  │   │  │  │ │ │ Container: Spring Boot App             │ │ │  │  │   │
│  │  └──────────────────────────────┘  │   │  │  │ │ │ - Java 21 + Spring Boot 3.3.13         │ │ │  │  │   │
│  └────────────────────────────────────┘   │  │  │ │ │ - New Relic Java Agent (javaagent)     │ │ │  │  │   │
│                                           │  │  │ │ │ - Resources: 500m-1000m CPU, 512Mi-1Gi │ │ │  │  │   │
│  ┌────────────────────────────────────┐   │  │  │ │ │ - Port: 8080                           │ │ │  │  │   │
│  │  CAMADA DE DADOS                   │   │  │  │ │ │ - Health: /actuator/health/liveness   │ │ │  │  │   │
│  │  ┌──────────────────────────────┐  │   │  │  │ │ └────────────────────────────────────────┘ │ │  │  │   │
│  │  │  AWS Secrets Manager         │  │   │  │  │ │ ┌────────────────────────────────────────┐ │ │  │  │   │
│  │  │  ┌────────────────────────┐  │  │   │  │  │ │ │ Init Container: db-migration           │ │ │  │  │   │
│  │  │  │ oficina-db-credentials │  │  │   │  │  │ │ │ - Flyway DB Schema Migrations          │ │ │  │  │   │
│  │  │  │ - username/password    │  │  │   │  │  │ │ └────────────────────────────────────────┘ │ │  │  │   │
│  │  │  │ - host/port            │  │  │   │  │  │ └────────────────────────────────────────────┘ │  │  │   │
│  │  │  │ Auto-rotation: 30 days │  │  │   │  │  │                                                │  │  │   │
│  │  │  └────────────────────────┘  │  │   │  │  │ Service: oficina-service (ClusterIP)            │  │  │   │
│  │  │  KMS Encrypted              │  │   │  │  │ Port: 80 → 8080                                  │  │  │   │
│  │  └──────────┬───────────────────┘  │   │  │  └──────────────────────────────────────────────────┘  │   │
│  │             │                      │   │  │                                                         │   │
│  │  ┌──────────▼───────────────────┐  │   │  │  ┌──────────────────────────────────────────────────┐  │   │
│  │  │  RDS PostgreSQL 15           │  │   │  │  │  DaemonSet: New Relic Infrastructure             │  │   │
│  │  │  ┌────────────────────────┐  │  │   │  │  │  - Namespace: newrelic                           │  │   │
│  │  │  │ Instance: db.t3.medium │  │  │   │  │  │  - Image: newrelic/infrastructure-k8s:3.0.0      │  │   │
│  │  │  │ Storage: 100 GB (GP3)  │  │  │   │  │  │  - Runs on EVERY node                            │  │   │
│  │  │  │ Multi-AZ: Enabled      │  │  │   │  │  │  - Collects: CPU, Memory, Network, Disk metrics  │  │   │
│  │  │  │ Backup: 30 days        │  │  │   │  │  │  - Sends to New Relic Platform                   │  │   │
│  │  │  │ Encryption: KMS        │  │  │   │  │  └──────────────────────────────────────────────────┘  │   │
│  │  │  └────────────────────────┘  │  │   │  │                                                         │   │
│  │  │  Databases:                  │  │   │  │  ┌──────────────────────────────────────────────────┐  │   │
│  │  │  - oficina_db                │  │   │  │  │  Deployment: Kube State Metrics                  │  │   │
│  │  │  Tables: customers,          │  │   │  │  │  - Exposes cluster state metrics                 │  │   │
│  │  │           vehicles,          │  │   │  │  │  - Port: 8080/metrics                            │  │   │
│  │  │           work_orders,       │  │   │  │  │  - Read by New Relic Infrastructure              │  │   │
│  │  │           services, parts    │  │   │  │  └──────────────────────────────────────────────────┘  │   │
│  │  └──────────────────────────────┘  │   │  │                                                         │   │
│  └────────────────────────────────────┘   │  │  ┌──────────────────────────────────────────────────┐  │   │
│                                           │  │  │  K8s Add-ons                                     │  │   │
│  ┌────────────────────────────────────┐   │  │  │  - Cluster Autoscaler                            │  │   │
│  │  CAMADA DE OBSERVABILIDADE         │   │  │  │  - External Secrets Operator                     │  │   │
│  │  ┌──────────────────────────────┐  │   │  │  │  - Metrics Server (HPA)                          │  │   │
│  │  │  🟢 New Relic One Platform   │  │   │  │  └──────────────────────────────────────────────────┘  │   │
│  │  │  ┌────────────────────────┐  │  │   │  │                                                         │   │
│  │  │  │ APM & Services         │  │  │   │  │  EKS Node Group: t3.medium (Min: 2 | Max: 10)           │   │
│  │  │  │ - Java Agent 8.8.0     │  │  │   │  └─────────────────────────────────────────────────────────┘   │
│  │  │  │ - Transaction Traces   │  │  │   └─────────────────────────────────────────────────────────────────┘
│  │  │  │ - Error Tracking       │  │  │
│  │  │  │ - Database Queries     │  │  │   ┌─────────────────────────────────────────────────────────────────┐
│  │  │  │ - JVM Metrics          │  │  │   │  CAMADA DE REDE (VPC)                                           │
│  │  │  │ - Distributed Tracing  │  │  │   │  ┌───────────────────────────────────────────────────────────┐  │
│  │  │  └────────────────────────┘  │  │   │  │  VPC: 10.0.0.0/16                                         │  │
│  │  │                              │  │   │  │                                                           │  │
│  │  │  ┌────────────────────────┐  │  │   │  │  Public Subnets (3 AZs) - NAT Gateways                    │  │
│  │  │  │ Infrastructure         │  │  │   │  │  - 10.0.1.0/24, 10.0.2.0/24, 10.0.3.0/24                  │  │
│  │  │  │ - Kubernetes Cluster   │  │  │   │  │                                                           │  │
│  │  │  │ - Node Metrics         │  │  │   │  │  Private Subnets (3 AZs) - EKS Nodes                      │  │
│  │  │  │ - Pod Metrics          │  │  │   │  │  - 10.0.11.0/24, 10.0.12.0/24, 10.0.13.0/24               │  │
│  │  │  │ - CPU, Memory, Disk    │  │  │   │  │                                                           │  │
│  │  │  │ - Network Traffic      │  │  │   │  │  Database Subnets (3 AZs) - RDS Multi-AZ                  │  │
│  │  │  └────────────────────────┘  │  │   │  │  - 10.0.21.0/24, 10.0.22.0/24, 10.0.23.0/24               │  │
│  │  │                              │  │   │  │                                                           │  │
│  │  │  ┌────────────────────────┐  │  │   │  │  Security Groups:                                         │  │
│  │  │  │ Logs                   │  │  │   │  │  - ALB SG: 80/443 from Internet                           │  │
│  │  │  │ - Structured JSON      │  │  │   │  │  - EKS Node SG: All from ALB SG                           │  │
│  │  │  │ - MDC Correlation:     │  │  │   │  │  - RDS SG: 5432 from Lambda + EKS SG                      │  │
│  │  │  │   * traceId            │  │  │   │  │  - Lambda SG: Outbound to RDS                             │  │
│  │  │  │   * spanId             │  │  │   │  └───────────────────────────────────────────────────────────┘  │
│  │  │  │   * requestId          │  │  │   └─────────────────────────────────────────────────────────────────┘
│  │  │  │   * ordemServicoId     │  │  │
│  │  │  │ - Application Logs     │  │  │   ┌─────────────────────────────────────────────────────────────────┐
│  │  │  │ - Error Logs           │  │  │   │  CI/CD Pipeline (GitHub Actions)                                │
│  │  │  └────────────────────────┘  │  │   │  ┌───────────────────────────────────────────────────────────┐  │
│  │  │                              │  │   │  │  Workflow: Deploy Lambda                                  │  │
│  │  │  ┌────────────────────────┐  │  │   │  │  - Build Java 21 | SAM Deploy | Multi-env                │  │
│  │  │  │ Custom Metrics         │  │  │   │  └───────────────────────────────────────────────────────────┘  │
│  │  │  │ - oficina.ordem_       │  │  │   │  ┌───────────────────────────────────────────────────────────┐  │
│  │  │  │   servico.criadas      │  │  │   │  │  Workflow: Deploy Infrastructure                          │  │
│  │  │  │ - oficina.ordem_       │  │  │   │  │  - Terraform Plan/Apply | Security Scan                   │  │
│  │  │  │   servico.concluidas   │  │  │   │  └───────────────────────────────────────────────────────────┘  │
│  │  │  │ - oficina.integracao.  │  │  │   │  ┌───────────────────────────────────────────────────────────┐  │
│  │  │  │   aprovacao_duration   │  │  │   │  │  Workflow: Deploy Application                             │  │
│  │  │  │ - Database queries     │  │  │   │  │  - Build Docker | Push ECR | K8s Rollout                  │  │
│  │  │  └────────────────────────┘  │  │   │  └───────────────────────────────────────────────────────────┘  │
│  │  │                              │  │   └─────────────────────────────────────────────────────────────────┘
│  │  │  ┌────────────────────────┐  │  │
│  │  │  │ Dashboards             │  │  │   ┌─────────────────────────────────────────────────────────────────┐
│  │  │  │ - Overview Page        │  │  │   │  EXTERNAL INTEGRATIONS                                          │
│  │  │  │   * Throughput         │  │  │   │  ┌───────────────────────────────────────────────────────────┐  │
│  │  │  │   * Latency P95/P99    │  │  │   │  │  GitHub (Source Control)                                  │  │
│  │  │  │   * Error Rate         │  │  │   │  │  - lambda-auth-service                                    │  │
│  │  │  │ - Ordens Serviço Page  │  │  │   │  │  - infra-database-terraform                               │  │
│  │  │  │   * Volume diário      │  │  │   │  │  - infra-kubernetes-terraform                             │  │
│  │  │  │   * Status distribution│  │  │   │  │  - oficina-service-k8s                                    │  │
│  │  │  │   * Tempo médio status │  │  │   │  └───────────────────────────────────────────────────────────┘  │
│  │  │  │ - Integrações Page     │  │  │   │  ┌───────────────────────────────────────────────────────────┐  │
│  │  │  │   * Status integrações │  │  │   │  │  Client Applications                                      │  │
│  │  │  │   * Erros timeline     │  │  │   │  │  - Web Frontend (React/Next.js)                           │  │
│  │  │  │ - Database Page        │  │  │   │  │  - Mobile App (iOS/Android)                               │  │
│  │  │  │   * Query performance  │  │  │   │  │  - Admin Dashboard                                        │  │
│  │  │  │   * Slow queries       │  │  │   │  └───────────────────────────────────────────────────────────┘  │
│  │  │  └────────────────────────┘  │  │   └─────────────────────────────────────────────────────────────────┘
│  │  │                              │  │
│  │  │  ┌────────────────────────┐  │  │
│  │  │  │ Alerts & AI            │  │  │
│  │  │  │ - High Latency Alert   │  │  │
│  │  │  │ - Error Rate Alert     │  │  │
│  │  │  │ - Resource Usage Alert │  │  │
│  │  │  │ - Integration Failures │  │  │
│  │  │  │ → Slack, Email, PagerD │  │  │
│  │  │  └────────────────────────┘  │  │
│  │  └──────────────────────────────┘  │
│  │                                    │
│  │  ┌──────────────────────────────┐  │
│  │  │  CloudWatch (Backup)         │  │
│  │  │  - Container Insights        │  │
│  │  │  - Lambda Logs               │  │
│  │  │  - RDS Logs                  │  │
│  │  │  - Retention: 30 days        │  │
│  │  └──────────────────────────────┘  │
│  └────────────────────────────────────┘
└──────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Componentes Principais

### 1. Camada de Entrada (Edge Layer)
**CloudFront + AWS WAF**
- **Função**: Ponto de entrada global para todas as requisições
- **Proteções**: DDoS (AWS Shield), SQL Injection, XSS, Rate Limiting
- **SSL/TLS**: Terminação de certificados
- **Cache**: Assets estáticos (se aplicável)

**API Gateway HTTP API v2**
- **Função**: Roteamento de requisições
- **Rotas**:
  - `POST /auth/validate` → Lambda Auth Service (autenticação)
  - `GET/POST /api/v1/*` → ALB → EKS (APIs de negócio)
- **Features**: Throttling (10K req/s), CORS, Custom Domain
- **Logs**: CloudWatch integration

---

### 2. Camada Serverless
**Lambda Auth Service**
- **Runtime**: Java 21
- **Função**: Validação de CPF e geração de JWT token
- **Recursos**: 512MB memory, 30s timeout, 100 concurrent executions
- **Integrações**:
  - RDS PostgreSQL (validação de cliente)
  - Secrets Manager (credenciais DB)
  - CloudWatch Logs (logging)
  - X-Ray (tracing)
  - **New Relic APM** (monitoramento)

---

### 3. Camada de Microserviços (EKS)
**EKS Cluster (Kubernetes 1.28)**
- **Namespace**: `oficina-service`
- **Deployment**: Spring Boot application
- **Replicas**: HPA (Horizontal Pod Autoscaler) - 2 a 10 pods
  - Trigger: CPU > 70%, Memory > 80%
- **Container Specs**:
  - **Image**: `oficina-service:latest` (ECR)
  - **Resources**: 500m-1000m CPU, 512Mi-1Gi Memory
  - **Port**: 8080
  - **Health**: `/actuator/health/liveness` e `/readiness`
  - **New Relic**: Java Agent integrado via `-javaagent`

**Init Container**: Flyway DB Migrations
- Executa antes do app container
- Aplica schema migrations no RDS

**Service**: ClusterIP
- Port mapping: 80 → 8080
- Exposto via Ingress para ALB

**New Relic Infrastructure DaemonSet**
- **Namespace**: `newrelic`
- **Função**: Coleta métricas de infraestrutura K8s
- **Deployment**: 1 pod por node (DaemonSet pattern)
- **Métricas**: CPU, Memory, Network, Disk, Pod States
- **Destino**: New Relic Platform

**Kube State Metrics**
- **Função**: Expõe métricas de estado do cluster
- **Port**: 8080/metrics (formato Prometheus)
- **Consumidor**: New Relic Infrastructure Agent

---

### 4. Camada de Dados
**RDS PostgreSQL 15**
- **Instance**: db.t3.medium
- **Storage**: 100 GB GP3 (SSD)
- **Multi-AZ**: Enabled (alta disponibilidade)
- **Backup**: Automático, retenção 30 dias
- **Encryption**: AWS KMS
- **Database**: `oficina_db`
- **Tables**: 
  - `customers` (clientes)
  - `vehicles` (veículos)
  - `work_orders` (ordens de serviço)
  - `services` (serviços)
  - `parts` (peças)
  - `work_order_items` (itens da OS)
  - `inventory` (estoque)
  - `payments` (pagamentos)

**AWS Secrets Manager**
- **Secret**: `oficina-db-credentials`
- **Conteúdo**: username, password, host, port
- **Rotation**: Automática a cada 30 dias
- **Encryption**: AWS KMS
- **Consumers**: Lambda Auth Service, EKS Pods (via External Secrets Operator)

**AWS KMS**
- **Função**: Criptografia de dados em repouso
- **Chaves**:
  - RDS encryption key
  - Secrets Manager encryption key
  - EBS volume encryption key

---

### 5. Camada de Observabilidade

#### 🟢 New Relic One Platform (Principal)

**APM & Services**
- **Agent**: New Relic Java Agent 8.8.0
- **Deployment**: Integrado via `-javaagent:/app/newrelic/newrelic.jar`
- **Capabilities**:
  - **Transaction Tracing**: Latência de cada endpoint (P50, P95, P99)
  - **Error Tracking**: Stack traces completos de exceções
  - **Database Queries**: Tempo de execução de queries SQL
  - **JVM Metrics**: Heap usage, GC pauses, thread pools
  - **Distributed Tracing**: Correlação entre Lambda e EKS

**Infrastructure Monitoring**
- **Agent**: New Relic Infrastructure K8s 3.0.0 (DaemonSet)
- **Métricas**:
  - **Node-level**: CPU, Memory, Disk I/O, Network
  - **Pod-level**: Resource usage, restart counts
  - **Cluster-level**: Node health, deployments, services
- **Integration**: Kube State Metrics para cluster state

**Logs**
- **Formato**: JSON estruturado (Logback + Logstash Encoder)
- **Correlation Fields (MDC)**:
  - `traceId`: Rastreamento distribuído
  - `spanId`: Segmento do trace
  - `requestId`: ID único da requisição
  - `ordemServicoId`: Contexto de negócio
  - `userId`, `clienteId`: Contexto do usuário
- **Collection**: Application Log Forwarding (New Relic Java Agent)
- **Searchable**: Full-text search, filtros por campos

**Custom Metrics**
- **Negócio**:
  - `oficina.ordem_servico.criadas.total`
  - `oficina.ordem_servico.concluidas.total`
  - `oficina.ordem_servico.status.{diagnostico|execucao|finalizacao}`
  - `oficina.integracao.aprovacao_orcamento.duration`
  - `oficina.integracao.aprovacao_orcamento.erros`
- **Técnicas**:
  - Database connection pool metrics
  - HTTP client metrics
  - JVM detailed metrics

**Dashboards** (4 páginas pré-configuradas)
1. **Overview**:
   - Throughput (requests/min)
   - Latência (P50, P95, P99)
   - Error rate
   - CPU/Memory por pod
   
2. **Ordens de Serviço**:
   - Volume diário de OS criadas
   - Distribuição por status
   - Tempo médio de execução por status
   - Taxa de sucesso vs falha
   
3. **Integrações**:
   - Status de integrações externas (API Aprovação)
   - Erros de integração (timeline)
   - Latência de chamadas externas
   
4. **Database**:
   - Query performance
   - Slow queries (> 1s)
   - Connection pool status
   - Transaction throughput

**Alerts & AI**
- **High Latency**: P95 > 2s por 5 min → Slack
- **Error Rate**: > 5% em 5 min → PagerDuty
- **Resource Usage**: CPU > 80% ou Memory > 85% → Email
- **Integration Failures**: Taxa de erro > 10% → Slack
- **Database**: Slow queries > 10/min → Email

#### CloudWatch (Backup/Complementar)
- **Container Insights**: Métricas agregadas de EKS
- **Lambda Logs**: Logs de execução do Lambda Auth
- **RDS Logs**: Slow query log, error log
- **Retention**: 30 dias

---

### 6. Camada de Rede (VPC)
**VPC**: `10.0.0.0/16`

**Public Subnets** (3 AZs):
- `10.0.1.0/24` (us-east-1a)
- `10.0.2.0/24` (us-east-1b)
- `10.0.3.0/24` (us-east-1c)
- **Componentes**: NAT Gateways (HA), ALB

**Private Subnets** (3 AZs):
- `10.0.11.0/24` (us-east-1a)
- `10.0.12.0/24` (us-east-1b)
- `10.0.13.0/24` (us-east-1c)
- **Componentes**: EKS worker nodes

**Database Subnets** (3 AZs):
- `10.0.21.0/24` (us-east-1a) - RDS Primary
- `10.0.22.0/24` (us-east-1b) - RDS Standby
- `10.0.23.0/24` (us-east-1c) - Reserved

**Security Groups**:
- **ALB SG**: Inbound 80/443 from Internet (0.0.0.0/0)
- **EKS Node SG**: Inbound all from ALB SG
- **RDS SG**: Inbound 5432 from Lambda SG + EKS Node SG
- **Lambda SG**: Outbound 5432 to RDS SG

---

### 7. CI/CD Pipeline (GitHub Actions)
**Workflow 1: Deploy Lambda**
- Trigger: Push to `main` branch (path: `lambda-auth-service/**`)
- Steps:
  1. Build Java 21 application
  2. SAM build & package
  3. SAM deploy to dev/staging/prod
  4. Run integration tests

**Workflow 2: Deploy Infrastructure**
- Trigger: Push to `main` branch (path: `infra-*-terraform/**`)
- Steps:
  1. Terraform fmt check
  2. Security scan (Checkov, TFSec)
  3. Terraform plan
  4. Manual approval (production)
  5. Terraform apply

**Workflow 3: Deploy Application**
- Trigger: Push to `main` branch (path: `oficina-service-k8s/**`)
- Steps:
  1. Build Docker image
  2. Security scan (Trivy)
  3. Push to ECR
  4. Update K8s deployment (kubectl set image)
  5. Rollout status verification
  6. Rollback on failure

---

## 🔄 Fluxos de Dados

### Fluxo 1: Autenticação
```
Cliente → CloudFront → WAF → API Gateway 
        → Lambda Auth Service 
        → Secrets Manager (credenciais) 
        → RDS (validação CPF) 
        → Lambda (geração JWT) 
        → API Gateway → Cliente
        
Monitoramento:
- New Relic APM: Trace da Lambda
- CloudWatch Logs: Log de autenticação
- X-Ray: Distributed trace
```

### Fluxo 2: Criação de Ordem de Serviço
```
Cliente → CloudFront → WAF → API Gateway 
        → ALB → Ingress NGINX → Service ClusterIP 
        → Pod oficina-service 
        → RDS (INSERT transaction) 
        → Pod → Service → Ingress → ALB 
        → API Gateway → Cliente
        
Monitoramento:
- New Relic APM: Transaction trace completo
- New Relic Logs: Logs estruturados com ordemServicoId
- New Relic Metrics: Incremento de oficina.ordem_servico.criadas.total
- New Relic Infrastructure: CPU/Memory usage do pod
```

### Fluxo 3: Monitoramento Contínuo
```
Pods EKS → New Relic Java Agent → New Relic Collector → New Relic One
        ↓                              ↓
    Logs JSON                   APM Transactions
    Custom Metrics              Error Traces
                                Database Queries

Nodes EKS → New Relic Infrastructure Agent → New Relic Collector
          ↓
      CPU, Memory, Network, Disk metrics
      Pod states, Container metrics

Kube State Metrics → New Relic Infrastructure → New Relic Platform
                   ↓
            Cluster state, Deployments, Services
```

---

## 🔐 Segurança

### Defense in Depth (7 Camadas)
1. **CloudFront + WAF**: Proteção contra DDoS, SQL Injection, XSS
2. **API Gateway**: Rate limiting (10K req/s), throttling, API keys (opcional)
3. **VPC**: Isolamento de rede, subnets privadas
4. **Security Groups**: Firewall de instâncias, least privilege
5. **IAM**: Roles com permissões mínimas (Lambda, EKS nodes)
6. **Secrets Manager**: Credenciais criptografadas, rotação automática
7. **KMS**: Criptografia de dados em repouso (RDS, Secrets, EBS)

### Compliance
- **Encryption at Rest**: KMS para RDS, EBS, Secrets
- **Encryption in Transit**: TLS 1.2+ em todos os endpoints
- **Audit**: CloudTrail para API calls, CloudWatch Logs
- **Network Isolation**: Private subnets, no direct internet access

---

## 📈 Escalabilidade

### Horizontal Pod Autoscaler (HPA)
- **Target Metrics**: CPU 70%, Memory 80%
- **Min Replicas**: 2 (para HA)
- **Max Replicas**: 10
- **Scale Up Policy**: Adiciona 1 pod a cada 30s (gradual)
- **Scale Down Policy**: Remove pods após 5 min de baixo uso (conservativo)
- **Cooldown**: 3 min entre scale events

### Cluster Autoscaler
- **Node Type**: t3.medium (2 vCPU, 4 GB RAM)
- **Min Nodes**: 2 (HA)
- **Max Nodes**: 10
- **Trigger**: Pods em estado Pending por falta de recursos
- **Scale Down**: Nodes com usage < 50% por 10 min

### RDS Read Replicas (Futuro)
- **Trigger**: Read throughput > 80% da instância primary
- **Replicas**: Até 5 read replicas
- **Endpoints**: Read-only endpoint para queries SELECT

---

## 💰 Estimativa de Custos (Mensal)

| Componente | Especificação | Custo Estimado |
|------------|---------------|----------------|
| **Lambda** | 1M invocations, 512MB, 500ms avg | $5 |
| **API Gateway** | 1M HTTP requests | $3.50 |
| **RDS PostgreSQL** | db.t3.medium Multi-AZ, 100GB GP3 | $120 |
| **EKS** | Cluster control plane | $73 |
| **EC2 (EKS Nodes)** | 3 × t3.medium (8760h/mês) | $147 |
| **ALB** | 1 load balancer + 10GB processed | $25 |
| **NAT Gateway** | 3 gateways (HA) + 10GB transfer | $100 |
| **CloudWatch** | 10GB logs + métricas + insights | $30 |
| **Secrets Manager** | 2 secrets + rotations | $2 |
| **KMS** | 3 keys + API calls | $5 |
| **Data Transfer** | Estimado (out to internet) | $20 |
| **New Relic** | APM + Infrastructure (assume plano Pro)** | $99 |
| **TOTAL** | | **~$629/mês** |

> **Nota**: Custo do New Relic varia conforme o plano (Standard $99, Pro $299). Pode usar plano gratuito (100GB/mês) para ambiente de desenvolvimento.

### Oportunidades de Otimização
1. **Lambda**: Usar ARM64 (Graviton2) para -20% de custo
2. **RDS**: Reserved Instances para -40% (compromisso 1 ano)
3. **EKS Nodes**: Spot Instances para -70% (workloads tolerantes a interrupção)
4. **NAT Gateway**: Usar 1 NAT Gateway (economia $66/mês, perde HA)
5. **CloudWatch**: Ajustar retenção de logs para 7 dias (dev/staging)

---

## 🎯 Benefícios da Arquitetura

✅ **Alta Disponibilidade**:
- Multi-AZ em RDS, NAT Gateways
- EKS nodes distribuídos em 3 AZs
- ALB com health checks automáticos
- Uptime SLA: 99.95%

✅ **Escalabilidade**:
- HPA para pods (2-10 replicas)
- Cluster Autoscaler para nodes (2-10 nodes)
- Lambda auto-scaling (0-1000 concurrent)
- RDS read replicas (futuro)

✅ **Segurança**:
- 7 camadas de defesa (WAF, VPC, SG, IAM, KMS, etc.)
- Criptografia end-to-end
- Rotação automática de credenciais
- Compliance com ISO 27001, SOC 2

✅ **Observabilidade Completa** (New Relic):
- APM: Latência, errors, throughput de cada endpoint
- Logs: Estruturados com correlação (traceId, spanId)
- Infraestrutura: CPU, memory, network por pod e node
- Custom Metrics: Métricas de negócio (ordens de serviço, integrações)
- Dashboards: 4 páginas com visões de negócio e técnica
- Alertas: Proativos via Slack, Email, PagerDuty

✅ **DevOps Ready**:
- CI/CD totalmente automatizado (GitHub Actions)
- Infraestrutura como código (Terraform)
- Deployments imutáveis (Docker)
- Rollback automático em falhas

✅ **Cost-Effective**:
- Serverless (Lambda) para autenticação: $5/mês
- Auto-scaling: Paga apenas pelo que usa
- Reserved Instances potencial: -40% de economia

✅ **Cloud-Native**:
- Serviços gerenciados (RDS, EKS, Lambda)
- Zero gerenciamento de servidores
- Patches automáticos de segurança
- Backup e disaster recovery automáticos

---

## 📚 Documentos Relacionados

- **[DIAGRAMA-SEQUENCIA.md](./DIAGRAMA-SEQUENCIA.md)**: Fluxos detalhados de autenticação e criação de OS
- **[RFC-001-ESCOLHA-CLOUD.md](./RFC-001-ESCOLHA-CLOUD.md)**: Decisão técnica de escolha da AWS
- **[RFC-002-ESCOLHA-DATABASE.md](./RFC-002-ESCOLHA-DATABASE.md)**: Decisão técnica de escolha do PostgreSQL
- **[ADR-001-SERVERLESS-ARCHITECTURE.md](./ADR-001-SERVERLESS-ARCHITECTURE.md)**: Arquitetura híbrida Serverless + Containers
- **[ADR-002-API-GATEWAY-SYNC.md](./ADR-002-API-GATEWAY-SYNC.md)**: Comunicação síncrona via API Gateway
- **[MONITORAMENTO-OBSERVABILIDADE.md](./MONITORAMENTO-OBSERVABILIDADE.md)**: Guia completo de New Relic

---

**Documento gerado em**: 2025-12-07  
**Última revisão**: 2025-12-07  
**Próxima revisão**: 2026-03-07
