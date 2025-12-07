# 🔍 Exemplos Práticos - Monitoramento New Relic

Este guia apresenta exemplos práticos de uso da solução de observabilidade.

## 📊 Cenários Comuns

### Cenário 1: Investigar Latência Alta

**Situação:** Usuários reportam lentidão na criação de ordens de serviço.

**Passo 1 - Verificar latência geral:**
```sql
SELECT percentile(duration, 95) 
FROM Transaction 
WHERE appName = 'oficina-service' 
  AND name LIKE '%OrdemServico%' 
SINCE 1 hour ago 
TIMESERIES
```

**Passo 2 - Identificar endpoint lento:**
```sql
SELECT average(duration), percentile(duration, 95) 
FROM Transaction 
WHERE appName = 'oficina-service' 
FACET name 
SINCE 1 hour ago 
ORDER BY percentile(duration, 95) DESC
```

**Passo 3 - Verificar queries lentas:**
```sql
SELECT count(*), average(databaseDuration) 
FROM Transaction 
WHERE appName = 'oficina-service' 
  AND databaseDuration > 1 
FACET name 
SINCE 1 hour ago
```

**Passo 4 - Analisar trace específico:**
```
APM → oficina-service → Transactions → 
Select slow transaction → View distributed trace
```

---

### Cenário 2: Investigar Erro em Ordem de Serviço

**Situação:** Alerta de falhas na criação de ordens de serviço.

**Passo 1 - Verificar métrica de erro:**
```sql
SELECT sum(oficina.ordem_servico.erros.criacao) 
FROM Metric 
TIMESERIES 5 minutes 
SINCE 1 hour ago
```

**Passo 2 - Buscar erros nos logs:**
```sql
SELECT timestamp, message, traceId, ordemServicoId, stackTrace 
FROM Log 
WHERE service = 'oficina-service' 
  AND level = 'ERROR' 
  AND message LIKE '%ordem_servico_erro%' 
SINCE 30 minutes ago
```

**Passo 3 - Ver stack trace completo:**
```sql
SELECT timestamp, error.class, error.message, stack_trace 
FROM TransactionError 
WHERE appName = 'oficina-service' 
  AND error.message LIKE '%OrdemServico%' 
SINCE 1 hour ago 
LIMIT 10
```

**Passo 4 - Rastrear requisição completa:**
```sql
-- Use o traceId do log de erro
SELECT * 
FROM Log 
WHERE traceId = 'TRACE_ID_AQUI' 
ORDER BY timestamp
```

---

### Cenário 3: Monitorar Deploy em Produção

**Situação:** Deploy recente em produção, verificar saúde.

**Passo 1 - Verificar pods rodando:**
```sql
SELECT uniqueCount(podName) AS 'Pods Ativos' 
FROM K8sContainerSample 
WHERE containerName = 'oficina-service' 
  AND status = 'Running' 
SINCE 10 minutes ago 
TIMESERIES
```

**Passo 2 - Verificar restarts:**
```sql
SELECT max(restartCount) 
FROM K8sContainerSample 
WHERE containerName = 'oficina-service' 
FACET podName 
SINCE 30 minutes ago
```

**Passo 3 - Comparar performance antes/depois:**
```sql
SELECT percentile(duration, 95) 
FROM Transaction 
WHERE appName = 'oficina-service' 
SINCE 1 hour ago 
COMPARE WITH 1 hour ago 
TIMESERIES
```

**Passo 4 - Verificar taxa de erro:**
```sql
SELECT percentage(count(*), WHERE error IS true) 
FROM Transaction 
WHERE appName = 'oficina-service' 
SINCE 30 minutes ago 
COMPARE WITH 30 minutes ago 
TIMESERIES
```

---

### Cenário 4: Analisar Performance de Integração Externa

**Situação:** Integração com API de Aprovação de Orçamento está lenta.

**Passo 1 - Latência da integração:**
```sql
SELECT average(duration), percentile(duration, 95, 99) 
FROM Transaction 
WHERE appName = 'oficina-service' 
  AND name LIKE '%aprovacao%' 
TIMESERIES
```

**Passo 2 - Taxa de sucesso:**
```sql
SELECT 
  count(*) AS 'Total',
  percentage(count(*), WHERE httpResponseCode < 400) AS 'Sucesso %' 
FROM Transaction 
WHERE appName = 'oficina-service' 
  AND name LIKE '%aprovacao%' 
SINCE 1 hour ago
```

**Passo 3 - Distribuição de status codes:**
```sql
SELECT count(*) 
FROM Transaction 
WHERE appName = 'oficina-service' 
  AND name LIKE '%aprovacao%' 
FACET httpResponseCode 
SINCE 1 hour ago
```

**Passo 4 - Logs de falhas:**
```sql
SELECT timestamp, message, traceId 
FROM Log 
WHERE service = 'oficina-service' 
  AND message LIKE '%integracao_externa_erro%' 
SINCE 1 hour ago
```

---

### Cenário 5: Análise de Volume de Negócio

**Situação:** Produto quer entender volume de ordens por período.

**Passo 1 - Volume diário (última semana):**
```sql
SELECT sum(oficina.ordem_servico.criadas.total) AS 'Ordens Criadas' 
FROM Metric 
FACET dateOf(timestamp) 
SINCE 7 days ago
```

**Passo 2 - Volume por hora (hoje):**
```sql
SELECT sum(oficina.ordem_servico.criadas.total) 
FROM Metric 
FACET hourOf(timestamp) 
SINCE today
```

**Passo 3 - Distribuição por status:**
```sql
SELECT 
  latest(oficina.ordem_servico.status.diagnostico.quantidade) AS 'Diagnóstico',
  latest(oficina.ordem_servico.status.execucao.quantidade) AS 'Execução',
  latest(oficina.ordem_servico.status.finalizacao.quantidade) AS 'Finalização',
  latest(oficina.ordem_servico.status.concluida.quantidade) AS 'Concluída' 
FROM Metric
```

**Passo 4 - Taxa de conclusão:**
```sql
SELECT 
  sum(oficina.ordem_servico.concluidas.total) / 
  sum(oficina.ordem_servico.criadas.total) * 100 AS 'Taxa Conclusão %' 
FROM Metric 
SINCE 1 day ago 
TIMESERIES 1 hour
```

---

### Cenário 6: Otimizar Consumo de Recursos

**Situação:** Pods consumindo muita memória.

**Passo 1 - Identificar pod com maior uso:**
```sql
SELECT average(memoryUsedBytes/memoryLimitBytes*100) AS 'Memory %' 
FROM K8sContainerSample 
WHERE containerName = 'oficina-service' 
FACET podName 
SINCE 1 hour ago
```

**Passo 2 - Tendência de uso de memória:**
```sql
SELECT average(memoryUsedBytes)/1024/1024/1024 AS 'Memory GB' 
FROM K8sContainerSample 
WHERE containerName = 'oficina-service' 
TIMESERIES AUTO 
SINCE 24 hours ago
```

**Passo 3 - Correlação com volume de requests:**
```sql
SELECT 
  rate(count(*), 1 minute) AS 'Requests/min',
  average(memoryUsedBytes)/1024/1024/1024 AS 'Memory GB' 
FROM Transaction, K8sContainerSample 
WHERE appName = 'oficina-service' 
  AND containerName = 'oficina-service' 
TIMESERIES
```

**Passo 4 - Verificar connection pool:**
```sql
SELECT count(*) AS 'Connection Errors' 
FROM TransactionError 
WHERE appName = 'oficina-service' 
  AND error.message LIKE '%connection%pool%' 
TIMESERIES
```

---

### Cenário 7: Troubleshoot Health Check Failure

**Situação:** Liveness probe falhando intermitentemente.

**Passo 1 - Verificar falhas:**
```sql
SELECT count(*) 
FROM Transaction 
WHERE appName = 'oficina-service' 
  AND name LIKE '%/actuator/health/liveness%' 
  AND httpResponseCode != 200 
TIMESERIES 1 minute
```

**Passo 2 - Correlação com CPU:**
```sql
SELECT 
  count(*) AS 'Health Failures',
  average(cpuUsedCores/cpuLimitCores*100) AS 'CPU %' 
FROM Transaction, K8sContainerSample 
WHERE appName = 'oficina-service' 
  AND name LIKE '%liveness%' 
  AND httpResponseCode != 200 
  AND containerName = 'oficina-service'
```

**Passo 3 - Logs durante falha:**
```sql
SELECT timestamp, level, message, traceId 
FROM Log 
WHERE service = 'oficina-service' 
  AND timestamp BETWEEN '2025-12-07 14:00:00' AND '2025-12-07 14:05:00' 
ORDER BY timestamp
```

**Passo 4 - Verificar database health:**
```sql
SELECT timestamp, message 
FROM Log 
WHERE service = 'oficina-service' 
  AND logger LIKE '%DatabaseHealthIndicator%' 
SINCE 1 hour ago
```

---

## 🎯 Workflows Prontos

### Workflow 1: Morning Health Check (Verificação Diária)

```sql
-- 1. Uptime das últimas 24h
SELECT percentage(count(*), WHERE httpResponseCode < 500) AS 'Uptime %' 
FROM Transaction 
WHERE appName = 'oficina-service' 
SINCE 24 hours ago

-- 2. Volume de ordens (ontem vs hoje)
SELECT sum(oficina.ordem_servico.criadas.total) 
FROM Metric 
SINCE today 
COMPARE WITH 1 day ago

-- 3. Top 5 erros
SELECT count(*), error.class, latest(error.message) 
FROM TransactionError 
WHERE appName = 'oficina-service' 
FACET error.class 
SINCE 24 hours ago 
LIMIT 5

-- 4. Latência média
SELECT percentile(duration, 50, 95, 99) 
FROM Transaction 
WHERE appName = 'oficina-service' 
SINCE 24 hours ago

-- 5. Resource usage
SELECT 
  average(cpuUsedCores/cpuLimitCores*100) AS 'CPU %',
  average(memoryUsedBytes/memoryLimitBytes*100) AS 'Memory %' 
FROM K8sContainerSample 
WHERE containerName = 'oficina-service' 
SINCE 24 hours ago
```

---

### Workflow 2: Incident Investigation (Investigação de Incidente)

```sql
-- Defina o período do incidente
-- INCIDENT_START = '2025-12-07 14:00:00'
-- INCIDENT_END = '2025-12-07 14:30:00'

-- 1. Timeline de erros
SELECT count(*) 
FROM TransactionError 
WHERE appName = 'oficina-service' 
TIMESERIES 1 minute 
BETWEEN INCIDENT_START AND INCIDENT_END

-- 2. Tipos de erro
SELECT count(*), error.class, error.message 
FROM TransactionError 
WHERE appName = 'oficina-service' 
FACET error.class 
BETWEEN INCIDENT_START AND INCIDENT_END

-- 3. Pods afetados
SELECT uniqueCount(podName) 
FROM K8sContainerSample 
WHERE containerName = 'oficina-service' 
  AND status != 'Running' 
BETWEEN INCIDENT_START AND INCIDENT_END

-- 4. Logs de erro
SELECT timestamp, level, logger, message, traceId, stackTrace 
FROM Log 
WHERE service = 'oficina-service' 
  AND level IN ('ERROR', 'FATAL') 
BETWEEN INCIDENT_START AND INCIDENT_END 
LIMIT 100

-- 5. Resource spike
SELECT 
  max(cpuUsedCores/cpuLimitCores*100) AS 'Max CPU %',
  max(memoryUsedBytes/memoryLimitBytes*100) AS 'Max Memory %' 
FROM K8sContainerSample 
WHERE containerName = 'oficina-service' 
FACET podName 
BETWEEN INCIDENT_START AND INCIDENT_END
```

---

### Workflow 3: Performance Baseline (Estabelecer Baseline)

```sql
-- Execute semanalmente para estabelecer baseline

-- 1. Latência P95 por hora do dia
SELECT percentile(duration, 95) 
FROM Transaction 
WHERE appName = 'oficina-service' 
FACET hourOf(timestamp) 
SINCE 7 days ago

-- 2. Throughput médio por dia da semana
SELECT rate(count(*), 1 minute) 
FROM Transaction 
WHERE appName = 'oficina-service' 
FACET weekdayOf(timestamp) 
SINCE 4 weeks ago

-- 3. Volume de ordens por dia da semana
SELECT sum(oficina.ordem_servico.criadas.total) 
FROM Metric 
FACET weekdayOf(timestamp) 
SINCE 4 weeks ago

-- 4. Taxa de erro média
SELECT percentage(count(*), WHERE error IS true) 
FROM Transaction 
WHERE appName = 'oficina-service' 
SINCE 30 days ago 
TIMESERIES 1 day

-- 5. Consumo médio de recursos
SELECT 
  average(cpuUsedCores/cpuLimitCores*100) AS 'Avg CPU %',
  average(memoryUsedBytes/memoryLimitBytes*100) AS 'Avg Memory %' 
FROM K8sContainerSample 
WHERE containerName = 'oficina-service' 
SINCE 30 days ago 
TIMESERIES 1 day
```

---

## 📱 Uso no Terminal

### Verificar Status Geral

```bash
# Health check
curl http://localhost:8080/actuator/health | jq

# Métricas específicas
curl http://localhost:8080/actuator/metrics/oficina.ordem_servico.criadas.total | jq

# Todas as métricas disponíveis
curl http://localhost:8080/actuator/metrics | jq '.names[]' | grep oficina

# Prometheus format
curl http://localhost:8080/actuator/prometheus | grep oficina_ordem_servico
```

### Verificar Logs

```bash
# Logs em tempo real (JSON)
kubectl logs -f deployment/oficina-deployment -n oficina

# Filtrar logs de erro
kubectl logs deployment/oficina-deployment -n oficina | jq 'select(.level=="ERROR")'

# Buscar por traceId
kubectl logs deployment/oficina-deployment -n oficina | jq 'select(.traceId=="TRACE_ID_AQUI")'

# Logs de ordens de serviço
kubectl logs deployment/oficina-deployment -n oficina | jq 'select(.message | contains("ordem_servico"))'
```

### Verificar New Relic Agent

```bash
# Verificar se o agente está rodando
kubectl exec deployment/oficina-deployment -n oficina -- ps aux | grep newrelic

# Ver configuração
kubectl exec deployment/oficina-deployment -n oficina -- cat /app/newrelic/newrelic.yml

# Logs do New Relic (dentro do pod)
kubectl exec deployment/oficina-deployment -n oficina -- cat /app/newrelic/logs/newrelic_agent.log
```

---

## 🔔 Exemplos de Alertas

### Criar Alerta via API

```bash
# Usando New Relic API
curl -X POST 'https://api.newrelic.com/v2/alerts_nrql_conditions.json' \
  -H 'Api-Key:YOUR_API_KEY' \
  -H 'Content-Type: application/json' \
  -d '{
    "nrql_condition": {
      "name": "Alta latência em Ordens de Serviço",
      "enabled": true,
      "terms": [{
        "duration": "5",
        "operator": "above",
        "priority": "critical",
        "threshold": "1",
        "time_function": "all"
      }],
      "nrql": {
        "query": "SELECT percentile(duration, 95) FROM Transaction WHERE appName = '\''oficina-service'\'' AND name LIKE '\''%OrdemServico%'\''"
      }
    }
  }'
```

---

## 💡 Dicas Práticas

### 1. Otimizar Queries NRQL

❌ **Evite:**
```sql
-- Muito genérico, demora para executar
SELECT * FROM Transaction SINCE 30 days ago
```

✅ **Prefira:**
```sql
-- Específico, com filtros e limite de tempo
SELECT duration, name FROM Transaction 
WHERE appName = 'oficina-service' 
SINCE 1 hour ago 
LIMIT 100
```

### 2. Usar Variáveis em Dashboards

```sql
-- Defina variável {{appName}}
SELECT count(*) FROM Transaction 
WHERE appName = {{appName}} 
TIMESERIES
```

### 3. Combinar Métricas

```sql
-- Correlacionar performance com volume
SELECT 
  rate(count(*), 1 minute) AS 'Throughput',
  percentile(duration, 95) AS 'Latência P95' 
FROM Transaction 
WHERE appName = 'oficina-service' 
TIMESERIES
```

### 4. Usar FACET CASES

```sql
-- Agrupar latências em buckets
SELECT count(*) 
FROM Transaction 
WHERE appName = 'oficina-service' 
FACET CASES (
  WHERE duration < 0.1 AS 'Fast (< 100ms)',
  WHERE duration >= 0.1 AND duration < 0.5 AS 'Normal (100-500ms)',
  WHERE duration >= 0.5 AND duration < 1 AS 'Slow (500ms-1s)',
  WHERE duration >= 1 AS 'Very Slow (> 1s)'
)
```

---

**Última atualização**: 07/12/2025  
**Versão**: 1.0
