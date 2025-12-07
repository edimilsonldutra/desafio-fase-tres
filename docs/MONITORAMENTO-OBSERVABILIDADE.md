# Monitoramento e Observabilidade - New Relic

## Visão Geral

Este documento descreve a implementação completa de **Monitoramento e Observabilidade** para o projeto Oficina Service utilizando **New Relic** como plataforma principal.

## Índice

1. [Componentes Implementados](#componentes-implementados)
2. [Configuração do New Relic APM](#configuração-do-new-relic-apm)
3. [Logs Estruturados](#logs-estruturados)
4. [Métricas Customizadas](#métricas-customizadas)
5. [Monitoramento de Kubernetes](#monitoramento-de-kubernetes)
6. [Alertas Configurados](#alertas-configurados)
7. [Dashboards](#dashboards)
8. [Guia de Instalação](#guia-de-instalação)
9. [Troubleshooting](#troubleshooting)

---

## Componentes Implementados

### ✅ 1. New Relic APM (Application Performance Monitoring)
- Integração com Java Agent
- Distributed Tracing habilitado
- Monitoramento de transações HTTP
- Rastreamento de queries de banco de dados

### ✅ 2. Logs Estruturados JSON
- Formato JSON com Logback e Logstash Encoder
- Correlação de requisições com `traceId`, `spanId`, `requestId`
- MDC (Mapped Diagnostic Context) para contexto de logs
- Suporte a New Relic Log Forwarding

### ✅ 3. Métricas Customizadas
- Contadores de Ordens de Serviço (criadas, concluídas, canceladas)
- Timers para latência de operações
- Métricas por status (Diagnóstico, Execução, Finalização)
- Exposição via Prometheus e New Relic

### ✅ 4. New Relic Infrastructure (Kubernetes)
- DaemonSet para monitoramento de nodes
- Kube State Metrics
- Monitoramento de CPU, memória e recursos
- Eventos de Kubernetes

### ✅ 5. Health Checks
- Liveness e Readiness Probes
- Health Indicators customizados (Database)
- Exposição via Spring Boot Actuator

### ✅ 6. Alertas Configurados
- Latência elevada de APIs
- Consumo excessivo de CPU/memória
- Falhas no processamento de ordens de serviço
- Erros em integrações externas
- Health check failures

### ✅ 7. Dashboards Customizados
- Overview geral da aplicação
- Métricas de Ordens de Serviço
- Monitoramento de integrações
- Performance de banco de dados

---

## Configuração do New Relic APM

### Arquivos de Configuração

#### 1. `newrelic.yml`
Localizado em `src/main/resources/newrelic.yml`

```yaml
common: &default_settings
  license_key: '${NEW_RELIC_LICENSE_KEY}'
  app_name: Oficina Service - ${NEW_RELIC_ENVIRONMENT:dev}
  distributed_tracing:
    enabled: true
  application_logging:
    enabled: true
    forwarding:
      enabled: true
```

#### 2. `pom.xml`
Dependências adicionadas:

```xml
<dependency>
    <groupId>com.newrelic.agent.java</groupId>
    <artifactId>newrelic-api</artifactId>
    <version>8.8.0</version>
</dependency>
```

#### 3. Dockerfile
Integração do Java Agent:

```dockerfile
RUN curl -L -o newrelic-java.zip https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip
    
CMD ["java", "-javaagent:/app/newrelic/newrelic.jar", "-jar", "app.jar"]
```

### Variáveis de Ambiente

No Kubernetes (`deployment.yaml`):

```yaml
env:
  - name: NEW_RELIC_LICENSE_KEY
    valueFrom:
      secretKeyRef:
        name: newrelic-secret
        key: license-key
  - name: NEW_RELIC_ENVIRONMENT
    value: "dev"
  - name: NEW_RELIC_APP_NAME
    value: "oficina-service"
```

---

## Logs Estruturados

### Configuração do Logback

Arquivo `logback-spring.xml` configurado com:

#### 1. Formato JSON
```xml
<appender name="CONSOLE_JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <includeMdcKeyName>traceId</includeMdcKeyName>
        <includeMdcKeyName>spanId</includeMdcKeyName>
        <includeMdcKeyName>requestId</includeMdcKeyName>
        <includeMdcKeyName>ordemServicoId</includeMdcKeyName>
    </encoder>
</appender>
```

#### 2. Correlação de Requisições

**RequestCorrelationFilter** adiciona automaticamente:
- `traceId`: ID único para rastreamento distribuído
- `spanId`: ID do span atual
- `requestId`: ID único da requisição
- `userId`: ID do usuário autenticado
- `ordemServicoId`: ID da ordem de serviço

#### 3. Exemplo de Log

```json
{
  "timestamp": "2025-12-07T14:30:45.123Z",
  "level": "INFO",
  "logger": "br.com.grupo99.oficinaservice.application.service.OrdemServicoApplicationService",
  "message": "Ordem de serviço criada com sucesso",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "spanId": "1234567890abcdef",
  "requestId": "req-abc123",
  "ordemServicoId": "os-12345",
  "userId": "user-789",
  "service": "oficina-service",
  "environment": "dev"
}
```

---

## Métricas Customizadas

### MetricsService

Implementado em `infrastructure/observability/MetricsService.java`

#### Contadores Registrados

| Métrica | Descrição |
|---------|-----------|
| `oficina.ordem_servico.criadas.total` | Total de ordens criadas |
| `oficina.ordem_servico.concluidas.total` | Total de ordens concluídas |
| `oficina.ordem_servico.canceladas.total` | Total de ordens canceladas |
| `oficina.ordem_servico.status.diagnostico` | Ordens em Diagnóstico |
| `oficina.ordem_servico.status.execucao` | Ordens em Execução |
| `oficina.ordem_servico.status.finalizacao` | Ordens em Finalização |
| `oficina.ordem_servico.erros.criacao` | Erros na criação |
| `oficina.ordem_servico.erros.integracao` | Erros de integração |

#### Timers (Latência)

| Métrica | Descrição |
|---------|-----------|
| `oficina.ordem_servico.criacao.tempo` | Tempo de criação |
| `oficina.ordem_servico.atualizacao.tempo` | Tempo de atualização |
| `oficina.ordem_servico.status.diagnostico.tempo` | Tempo no status Diagnóstico |
| `oficina.ordem_servico.status.execucao.tempo` | Tempo no status Execução |

### OrdemServicoMonitoringAspect

Aspect AOP que intercepta automaticamente:

1. **Criação de Ordens de Serviço**
   - Registra duração
   - Incrementa contadores
   - Envia eventos para New Relic

2. **Atualização de Status**
   - Rastreia mudanças de status
   - Calcula tempo em cada status
   - Registra métricas por status

3. **Integrações Externas**
   - Monitora chamadas a APIs externas
   - Rastreia latência
   - Detecta falhas

---

## Monitoramento de Kubernetes

### New Relic Infrastructure

#### DaemonSet (`newrelic-infrastructure.yaml`)

Implantado em cada node do cluster para coletar:
- Métricas de CPU e memória por node
- Métricas de pods e containers
- Eventos de Kubernetes
- Logs de sistema

#### Kube State Metrics

Fornece métricas do cluster:
- Status de Deployments
- Status de Pods
- Recursos alocados vs usados
- ConfigMaps e Secrets

### Métricas Coletadas

| Recurso | Métricas |
|---------|----------|
| **Pods** | CPU, Memória, Network, Restarts |
| **Nodes** | Capacidade, Utilização, Status |
| **Deployments** | Replicas, Available, Unavailable |
| **Services** | Endpoints, Connections |

---

## Alertas Configurados

### 1. Latência das APIs

**Alerta Crítico:**
- P95 > 1 segundo por 5 minutos
- Média > 500ms por 5 minutos

**Alerta Warning:**
- P95 > 500ms por 5 minutos

### 2. Consumo de Recursos

**CPU:**
- Critical: > 80% por 5 minutos
- Warning: > 70% por 5 minutos

**Memória:**
- Critical: > 85% por 5 minutos
- Warning: > 75% por 5 minutos

**Pod Restarts:**
- Critical: > 3 restarts em 10 minutos

### 3. Health Checks

**Taxa de Erro HTTP:**
- Critical: > 5% por 5 minutos
- Warning: > 2% por 5 minutos

**Health Check Failure:**
- Critical: > 3 falhas em 5 minutos

**Aplicação Offline:**
- Critical: 0 transações por 3 minutos

### 4. Ordens de Serviço

**Falhas na Criação:**
- Critical: > 5 erros em 5 minutos
- Warning: > 2 erros em 5 minutos

**Falhas em Integrações:**
- Critical: > 3 erros em 5 minutos

**Taxa de Sucesso:**
- Critical: < 95% em 10 minutos
- Warning: < 98% em 10 minutos

### 5. Database Performance

**Slow Queries:**
- Warning: > 10 queries com duração > 1s em 5 minutos

**Connection Pool:**
- Critical: Qualquer erro de connection pool

---

## Dashboards

### 1. Overview Dashboard

**Widgets principais:**
- Throughput (requisições/minuto)
- Latência (P50, P95, P99)
- Taxa de erro
- CPU Usage por pod
- Memória Usage por pod

### 2. Ordens de Serviço Dashboard

**Widgets:**
- Volume diário de ordens
- Ordens criadas hoje
- Ordens concluídas hoje
- Distribuição por status (pizza)
- Tempo médio por status
- Taxa de sucesso de criação
- Timeline de erros

### 3. Integrações e Erros Dashboard

**Widgets:**
- Status de integrações externas
- Latência de integrações
- Erros por tipo
- Top 10 erros recentes
- Timeline de erros

### 4. Database Performance Dashboard

**Widgets:**
- Database query time (avg, P95, P99)
- Slow queries (> 1s)
- Database calls por transação
- Connection pool status

---

## Guia de Instalação

### Passo 1: Configurar New Relic License Key

1. Obtenha sua license key no [New Relic](https://one.newrelic.com/)

2. Crie o secret no Kubernetes:

```bash
kubectl create secret generic newrelic-secret \
  --from-literal=license-key=YOUR_LICENSE_KEY_HERE \
  -n oficina
```

### Passo 2: Deploy New Relic Infrastructure

```bash
# Criar namespace
kubectl apply -f infra-kubernetes-terraform/modules/newrelic-infrastructure.yaml

# Verificar status
kubectl get pods -n newrelic
kubectl logs -n newrelic -l app=newrelic-infrastructure
```

### Passo 3: Deploy Kube State Metrics

```bash
kubectl apply -f infra-kubernetes-terraform/modules/kube-state-metrics.yaml

# Verificar
kubectl get pods -n newrelic -l app=kube-state-metrics
```

### Passo 4: Build e Deploy da Aplicação

```bash
cd oficina-service-k8s

# Build com New Relic Agent
docker build -t oficina-service:latest .

# Deploy no Kubernetes
kubectl apply -f k8s/base/

# Verificar logs
kubectl logs -f deployment/oficina-deployment -n oficina
```

### Passo 5: Configurar Alertas

1. Acesse New Relic One
2. Navegue para Alerts & AI > Alert Policies
3. Importe o arquivo `docs/newrelic-alerts-config.yml`
4. Configure os canais de notificação (Email, Slack, PagerDuty)

### Passo 6: Importar Dashboards

1. Acesse New Relic One
2. Navegue para Dashboards
3. Clique em "Import dashboard"
4. Carregue o arquivo `docs/newrelic-dashboard.json`

---

## Endpoints Expostos

### Spring Boot Actuator

| Endpoint | Descrição |
|----------|-----------|
| `/actuator/health` | Health check geral |
| `/actuator/health/liveness` | Liveness probe |
| `/actuator/health/readiness` | Readiness probe |
| `/actuator/metrics` | Todas as métricas |
| `/actuator/prometheus` | Métricas formato Prometheus |
| `/actuator/info` | Informações da aplicação |
| `/actuator/loggers` | Configuração de logs |

### Exemplos de Consulta

```bash
# Health check
curl http://localhost:8080/actuator/health

# Métricas específicas
curl http://localhost:8080/actuator/metrics/oficina.ordem_servico.criadas.total

# Prometheus format
curl http://localhost:8080/actuator/prometheus
```

---

## Troubleshooting

### 1. New Relic Agent não está enviando dados

**Verificar:**
```bash
# Logs do container
kubectl logs -f deployment/oficina-deployment -n oficina | grep -i "newrelic"

# Verificar se o agente está ativo
kubectl exec -it deployment/oficina-deployment -n oficina -- ls -la /app/newrelic/
```

**Solução:**
- Verificar se `NEW_RELIC_LICENSE_KEY` está configurada
- Verificar conectividade com `collector.newrelic.com`
- Verificar se o arquivo `newrelic.yml` está presente

### 2. Métricas não aparecem no New Relic

**Verificar:**
```bash
# Testar endpoint Prometheus
curl http://localhost:8080/actuator/prometheus | grep oficina

# Verificar logs de métricas
kubectl logs -f deployment/oficina-deployment -n oficina | grep -i "metric"
```

**Solução:**
- Verificar se `MetricsService` está sendo injetado corretamente
- Verificar se os Aspects estão funcionando (logs devem mostrar execução)

### 3. Logs não estão em formato JSON

**Verificar:**
```bash
# Ver logs do pod
kubectl logs deployment/oficina-deployment -n oficina

# Deve retornar JSON
```

**Solução:**
- Verificar se `logback-spring.xml` está sendo carregado
- Verificar dependência `logstash-logback-encoder` no pom.xml

### 4. Infrastructure não está coletando dados

**Verificar:**
```bash
# Status do DaemonSet
kubectl get daemonset -n newrelic

# Logs
kubectl logs -n newrelic -l name=newrelic-infrastructure
```

**Solução:**
- Verificar RBAC permissions
- Verificar se a license key está correta
- Verificar conectividade com New Relic

---

## Queries NRQL Úteis

### Performance

```sql
-- Transações mais lentas
SELECT average(duration), percentile(duration, 95, 99)
FROM Transaction
WHERE appName = 'oficina-service'
FACET name
SINCE 1 hour ago

-- Throughput
SELECT rate(count(*), 1 minute)
FROM Transaction
WHERE appName = 'oficina-service'
TIMESERIES AUTO
```

### Ordens de Serviço

```sql
-- Volume diário
SELECT sum(oficina.ordem_servico.criadas.total)
FROM Metric
FACET dateOf(timestamp)
SINCE 7 days ago

-- Taxa de sucesso
SELECT (sum(oficina.ordem_servico.criadas.total) - sum(oficina.ordem_servico.erros.criacao)) / sum(oficina.ordem_servico.criadas.total) * 100
FROM Metric
SINCE 1 hour ago
```

### Erros

```sql
-- Top erros
SELECT count(*)
FROM TransactionError
WHERE appName = 'oficina-service'
FACET error.class, error.message
SINCE 24 hours ago
LIMIT 10

-- Timeline de erros
SELECT count(*)
FROM TransactionError
WHERE appName = 'oficina-service'
FACET error.class
TIMESERIES AUTO
```

### Kubernetes

```sql
-- CPU por pod
SELECT average(cpuUsedCores/cpuLimitCores*100)
FROM K8sContainerSample
WHERE containerName = 'oficina-service'
FACET podName
TIMESERIES AUTO

-- Memória por pod
SELECT average(memoryUsedBytes/memoryLimitBytes*100)
FROM K8sContainerSample
WHERE containerName = 'oficina-service'
FACET podName
TIMESERIES AUTO
```

---

## Boas Práticas

### 1. Correlação de Logs
- Sempre use `MDC.put()` para adicionar contexto aos logs
- Inclua IDs de entidades importantes (ordemServicoId, clienteId, etc.)
- Mantenha o `traceId` em todas as operações de uma requisição

### 2. Métricas Customizadas
- Use nomes descritivos e hierárquicos
- Adicione tags relevantes (`service`, `environment`, etc.)
- Registre tanto sucessos quanto falhas

### 3. Alertas
- Configure thresholds realistas baseados em dados históricos
- Use múltiplos canais de notificação
- Documente runbooks para cada alerta

### 4. Dashboards
- Organize por domínio (Overview, Ordens de Serviço, etc.)
- Use cores consistentes
- Inclua contexto temporal (comparar com períodos anteriores)

---

## Referências

- [New Relic Java Agent Documentation](https://docs.newrelic.com/docs/agents/java-agent/)
- [New Relic Kubernetes Integration](https://docs.newrelic.com/docs/kubernetes-pixie/kubernetes-integration/)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)

---

## Contato e Suporte

Para questões sobre monitoramento e observabilidade:
- **Equipe DevOps**: devops@grupo99.com.br
- **Slack**: #oficina-observability
- **PagerDuty**: Escalation Policy - Oficina Service
