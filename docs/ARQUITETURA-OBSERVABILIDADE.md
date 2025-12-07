# Arquitetura de Observabilidade - Diagrama

## Visão Geral da Solução

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         NEW RELIC ONE PLATFORM                              │
│                         (Cloud Observability)                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐  ┌──────────────┐    │
│  │   APM       │  │   Logs       │  │ Kubernetes  │  │   Alerts     │    │
│  │ Monitoring  │  │ Management   │  │ Monitoring  │  │  & Dashb.    │    │
│  └─────────────┘  └──────────────┘  └─────────────┘  └──────────────┘    │
│                                                                             │
└──────────────────────────────┬──────────────────────────────────────────────┘
                               │
                    ┌──────────┴──────────┐
                    │   Data Collection   │
                    └──────────┬──────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
┌───────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ New Relic     │    │  Application     │    │  Kubernetes     │
│ Java Agent    │    │  Logs (JSON)     │    │  Infrastructure │
└───────┬───────┘    └────────┬─────────┘    └────────┬────────┘
        │                     │                       │
        │                     │                       │
        ▼                     ▼                       ▼
┌───────────────────────────────────────────────────────────────┐
│              KUBERNETES CLUSTER (EKS/Local)                   │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│  Namespace: oficina                                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Deployment: oficina-deployment                      │    │
│  │  ┌───────────────────────────────────────────────┐  │    │
│  │  │  Pod: oficina-service-xxxx                    │  │    │
│  │  │  ┌─────────────────────────────────────────┐  │  │    │
│  │  │  │  Container: oficina-service             │  │  │    │
│  │  │  │                                         │  │  │    │
│  │  │  │  ┌──────────────────────────────────┐  │  │  │    │
│  │  │  │  │  Spring Boot Application         │  │  │  │    │
│  │  │  │  │  ┌────────────────────────────┐  │  │  │  │    │
│  │  │  │  │  │  Controllers               │  │  │  │  │    │
│  │  │  │  │  │  - OrdemServicoRest...     │  │  │  │  │    │
│  │  │  │  │  │  - ClienteRestController   │  │  │  │  │    │
│  │  │  │  │  └────────────────────────────┘  │  │  │  │    │
│  │  │  │  │                                  │  │  │  │    │
│  │  │  │  │  ┌────────────────────────────┐  │  │  │  │    │
│  │  │  │  │  │  Observability Layer       │  │  │  │  │    │
│  │  │  │  │  │  ────────────────────────  │  │  │  │  │    │
│  │  │  │  │  │  1. RequestCorrelation     │  │  │  │  │    │
│  │  │  │  │  │     Filter                 │  │  │  │  │    │
│  │  │  │  │  │     - Adds traceId         │  │  │  │  │    │
│  │  │  │  │  │     - Adds spanId          │  │  │  │  │    │
│  │  │  │  │  │     - Adds requestId       │  │  │  │  │    │
│  │  │  │  │  │                            │  │  │  │  │    │
│  │  │  │  │  │  2. OrdemServicoMonitoring │  │  │  │  │    │
│  │  │  │  │  │     Aspect (AOP)           │  │  │  │  │    │
│  │  │  │  │  │     - Records metrics      │  │  │  │  │    │
│  │  │  │  │  │     - Sends to New Relic   │  │  │  │  │    │
│  │  │  │  │  │     - Logs events          │  │  │  │  │    │
│  │  │  │  │  │                            │  │  │  │  │    │
│  │  │  │  │  │  3. MetricsService         │  │  │  │  │    │
│  │  │  │  │  │     - Custom counters      │  │  │  │  │    │
│  │  │  │  │  │     - Custom timers        │  │  │  │  │    │
│  │  │  │  │  │     - Gauges               │  │  │  │  │    │
│  │  │  │  │  │                            │  │  │  │  │    │
│  │  │  │  │  │  4. HealthIndicators       │  │  │  │  │    │
│  │  │  │  │  │     - Database health      │  │  │  │  │    │
│  │  │  │  │  └────────────────────────────┘  │  │  │  │    │
│  │  │  │  │                                  │  │  │  │    │
│  │  │  │  │  ┌────────────────────────────┐  │  │  │  │    │
│  │  │  │  │  │  Spring Boot Actuator      │  │  │  │  │    │
│  │  │  │  │  │  ────────────────────────  │  │  │  │  │    │
│  │  │  │  │  │  /actuator/health          │  │  │  │  │    │
│  │  │  │  │  │  /actuator/metrics         │  │  │  │  │    │
│  │  │  │  │  │  /actuator/prometheus      │  │  │  │  │    │
│  │  │  │  │  └────────────────────────────┘  │  │  │  │    │
│  │  │  │  │                                  │  │  │  │    │
│  │  │  │  │  ┌────────────────────────────┐  │  │  │  │    │
│  │  │  │  │  │  New Relic Java Agent      │  │  │  │  │    │
│  │  │  │  │  │  ────────────────────────  │  │  │  │  │    │
│  │  │  │  │  │  - APM data collection     │  │  │  │  │    │
│  │  │  │  │  │  - Distributed tracing     │  │  │  │  │    │
│  │  │  │  │  │  - Transaction monitoring  │  │  │  │  │    │
│  │  │  │  │  │  - DB query monitoring     │  │  │  │  │    │
│  │  │  │  │  └────────────────────────────┘  │  │  │  │    │
│  │  │  │  │                                  │  │  │  │    │
│  │  │  │  │  ┌────────────────────────────┐  │  │  │  │    │
│  │  │  │  │  │  Logback + JSON Encoder    │  │  │  │  │    │
│  │  │  │  │  │  ────────────────────────  │  │  │  │  │    │
│  │  │  │  │  │  - Structured JSON logs    │  │  │  │  │    │
│  │  │  │  │  │  - MDC correlation         │  │  │  │  │    │
│  │  │  │  │  │  - Stdout → K8s logs       │  │  │  │  │    │
│  │  │  │  │  └────────────────────────────┘  │  │  │  │    │
│  │  │  │  └──────────────────────────────────┘  │  │  │    │
│  │  │  │                                         │  │  │    │
│  │  │  │  Environment Variables:                 │  │  │    │
│  │  │  │  - NEW_RELIC_LICENSE_KEY (secret)       │  │  │    │
│  │  │  │  - NEW_RELIC_ENVIRONMENT=dev            │  │  │    │
│  │  │  │  - NEW_RELIC_APP_NAME=oficina-service   │  │  │    │
│  │  │  │                                         │  │  │    │
│  │  │  │  Resources:                              │  │  │    │
│  │  │  │  - CPU: 100m → 1000m                    │  │  │    │
│  │  │  │  - Memory: 512Mi → 1Gi                  │  │  │    │
│  │  │  │                                         │  │  │    │
│  │  │  │  Probes:                                 │  │  │    │
│  │  │  │  - Liveness: /actuator/health/liveness  │  │  │    │
│  │  │  │  - Readiness: /actuator/health/readiness│  │  │    │
│  │  │  └─────────────────────────────────────────┘  │  │    │
│  │  │                                                │  │    │
│  │  │  Replicas: 2                                   │  │    │
│  │  └────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                               │
│  Namespace: newrelic                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  DaemonSet: newrelic-infrastructure                  │    │
│  │  ┌───────────────────────────────────────────────┐  │    │
│  │  │  Pod (on each node)                           │  │    │
│  │  │  - Collects node metrics (CPU, memory)        │  │    │
│  │  │  - Collects pod/container metrics             │  │    │
│  │  │  - Collects K8s events                        │  │    │
│  │  │  - Sends to New Relic Infrastructure         │  │    │
│  │  └───────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Deployment: kube-state-metrics                      │    │
│  │  - Collects cluster state metrics                    │    │
│  │  - Deployment/Pod/Service status                     │    │
│  │  - Resource quotas and limits                        │    │
│  │  └─────────────────────────────────────────────────┘    │
│                                                               │
└───────────────────────────────────────────────────────────────┘
                               │
                               │  Sends data to
                               ▼
┌───────────────────────────────────────────────────────────────┐
│              NEW RELIC COLLECTOR                              │
│              (collector.newrelic.com)                         │
└───────────────────────────────────────────────────────────────┘
```

## Fluxo de Dados

### 1. Application Performance Monitoring (APM)

```
User Request
     │
     ▼
RequestCorrelationFilter
     │ (adds traceId, spanId, requestId to MDC)
     ▼
Controller
     │
     ▼
OrdemServicoMonitoringAspect (AOP)
     │ (before execution)
     │ - Records start time
     │ - Logs operation start
     │
     ▼
Use Case / Service
     │
     ▼
Repository / Database
     │
     ▼
OrdemServicoMonitoringAspect (AOP)
     │ (after execution)
     │ - Calculates duration
     │ - Increments counters
     │ - Records timing
     │ - NewRelic.recordMetric()
     │ - NewRelic.incrementCounter()
     │
     ▼
MetricsService
     │ - Updates Micrometer metrics
     │ - Exposes via /actuator/prometheus
     │
     ▼
New Relic Java Agent
     │ - Captures transaction data
     │ - Captures DB queries
     │ - Sends to New Relic Collector
     │
     ▼
Response to User
```

### 2. Logs Estruturados

```
Application Log Event
     │
     ▼
MDC Context
     │ - traceId
     │ - spanId
     │ - requestId
     │ - userId
     │ - ordemServicoId
     │
     ▼
Logback Encoder
     │ - Converts to JSON
     │ - Includes MDC fields
     │ - Adds timestamp, level, logger
     │
     ▼
Console Appender (STDOUT)
     │
     ▼
Kubernetes Logs
     │
     ▼
New Relic Log Forwarder
     │ (configured in Java Agent)
     │
     ▼
New Relic Logs
```

### 3. Métricas Customizadas

```
Business Event
(e.g., Ordem de Serviço criada)
     │
     ▼
OrdemServicoMonitoringAspect
     │
     ▼
MetricsService.incrementOrdemServicoCriada()
     │
     ├─▶ Micrometer Counter
     │   │ - oficina.ordem_servico.criadas.total
     │   │
     │   ▼
     │   Prometheus Endpoint
     │   /actuator/prometheus
     │
     └─▶ NewRelic.incrementCounter()
         │ - Custom/OrdemServico/Criacao/Count
         │
         ▼
         New Relic Custom Events
```

### 4. Kubernetes Monitoring

```
K8s Cluster
     │
     ├─▶ Node Metrics
     │   │ - CPU usage
     │   │ - Memory usage
     │   │ - Network traffic
     │   │
     │   ▼
     │   New Relic Infrastructure DaemonSet
     │   │ (running on each node)
     │   │
     │   ▼
     │   New Relic Infrastructure
     │
     └─▶ Cluster State
         │ - Pod status
         │ - Deployment status
         │ - Service endpoints
         │
         ▼
         Kube State Metrics
         │
         ▼
         New Relic Infrastructure
```

## Componentes Principais

### 1. Aplicação (oficina-service)

| Componente | Responsabilidade | Localização |
|------------|------------------|-------------|
| **RequestCorrelationFilter** | Adiciona IDs de correlação (traceId, spanId, requestId) ao MDC | `infrastructure/observability/` |
| **OrdemServicoMonitoringAspect** | Intercepta operações de OS e registra métricas | `infrastructure/observability/` |
| **MetricsService** | Gerencia métricas customizadas via Micrometer | `infrastructure/observability/` |
| **DatabaseHealthIndicator** | Health check customizado para database | `infrastructure/observability/` |

### 2. New Relic Java Agent

| Recurso | Descrição |
|---------|-----------|
| **Transaction Monitoring** | Captura automática de todas as transações HTTP |
| **Database Monitoring** | Rastreia queries SQL com duração |
| **Distributed Tracing** | Rastreia requisições entre microserviços |
| **Error Analytics** | Captura e analisa erros/exceptions |
| **Log Forwarding** | Envia logs da aplicação para New Relic |

### 3. Kubernetes Infrastructure

| Componente | Função |
|------------|--------|
| **New Relic Infrastructure DaemonSet** | Coleta métricas de nodes, pods, containers |
| **Kube State Metrics** | Coleta estado do cluster (deployments, services, etc) |
| **RBAC** | Permissões para acessar API do Kubernetes |

### 4. New Relic Platform

| Recurso | Uso |
|---------|-----|
| **APM** | Visualização de performance da aplicação |
| **Logs** | Busca e análise de logs estruturados |
| **Infrastructure** | Monitoramento de Kubernetes |
| **Dashboards** | Visualizações customizadas |
| **Alerts** | Notificações de incidentes |

## Métricas Coletadas

### Application Metrics

- **Throughput**: Requisições por minuto
- **Latency**: P50, P95, P99
- **Error Rate**: Porcentagem de erros
- **Apdex**: Application Performance Index

### Business Metrics

- **Ordens de Serviço**: Criadas, Concluídas, Canceladas
- **Status**: Quantidade em cada status
- **Tempo de Processamento**: Por status e operação
- **Taxa de Sucesso**: Operações bem-sucedidas

### Infrastructure Metrics

- **CPU Usage**: Por pod e node
- **Memory Usage**: Por pod e node
- **Network**: RX/TX bytes
- **Pod Status**: Running, Pending, Failed
- **Restarts**: Contagem de restarts

### Database Metrics

- **Query Duration**: Tempo de execução
- **Query Count**: Número de queries
- **Connection Pool**: Uso e disponibilidade
- **Slow Queries**: Queries > 1 segundo

---

**Criado em**: 07/12/2025  
**Versão**: 1.0
