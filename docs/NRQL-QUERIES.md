# Queries NRQL Úteis - New Relic

Este documento contém queries NRQL úteis para monitoramento do Oficina Service.

## Performance da Aplicação

### Throughput (Requisições por Minuto)
```sql
SELECT rate(count(*), 1 minute) AS 'Requisições/min'
FROM Transaction
WHERE appName = 'oficina-service'
TIMESERIES AUTO
```

### Latência - Percentis
```sql
SELECT 
  percentile(duration, 50) AS 'P50',
  percentile(duration, 95) AS 'P95',
  percentile(duration, 99) AS 'P99'
FROM Transaction
WHERE appName = 'oficina-service'
TIMESERIES AUTO
```

### Transações Mais Lentas
```sql
SELECT average(duration) AS 'Duração Média (s)', 
       percentile(duration, 95) AS 'P95',
       count(*) AS 'Quantidade'
FROM Transaction
WHERE appName = 'oficina-service'
FACET name
SINCE 1 hour ago
ORDER BY average(duration) DESC
LIMIT 10
```

### Apdex Score
```sql
SELECT apdex(duration, 0.5)
FROM Transaction
WHERE appName = 'oficina-service'
TIMESERIES AUTO
```

## Ordens de Serviço

### Volume Diário de Ordens Criadas
```sql
SELECT sum(oficina.ordem_servico.criadas.total) AS 'Ordens Criadas'
FROM Metric
FACET dateOf(timestamp)
SINCE 30 days ago
```

### Ordens por Status (Atual)
```sql
SELECT 
  latest(oficina.ordem_servico.status.diagnostico.quantidade) AS 'Diagnóstico',
  latest(oficina.ordem_servico.status.execucao.quantidade) AS 'Execução',
  latest(oficina.ordem_servico.status.finalizacao.quantidade) AS 'Finalização',
  latest(oficina.ordem_servico.status.concluida.quantidade) AS 'Concluída'
FROM Metric
```

### Taxa de Sucesso na Criação
```sql
SELECT 
  (sum(oficina.ordem_servico.criadas.total) - sum(oficina.ordem_servico.erros.criacao)) / 
  sum(oficina.ordem_servico.criadas.total) * 100 AS 'Taxa de Sucesso (%)'
FROM Metric
SINCE 1 hour ago
TIMESERIES 5 minutes
```

### Tempo Médio por Status
```sql
SELECT 
  average(oficina.ordem_servico.status.diagnostico.tempo) AS 'Diagnóstico (ms)',
  average(oficina.ordem_servico.status.execucao.tempo) AS 'Execução (ms)',
  average(oficina.ordem_servico.status.finalizacao.tempo) AS 'Finalização (ms)'
FROM Metric
SINCE 24 hours ago
```

### Volume Horário de Ordens (Hoje)
```sql
SELECT count(*) AS 'Ordens Criadas'
FROM Log
WHERE message LIKE '%ordem_servico_criada%'
  AND service = 'oficina-service'
FACET hourOf(timestamp)
SINCE today
```

### Comparação Semanal
```sql
SELECT sum(oficina.ordem_servico.criadas.total)
FROM Metric
SINCE 1 week ago
COMPARE WITH 1 week ago
TIMESERIES 1 day
```

## Erros e Falhas

### Taxa de Erro Geral
```sql
SELECT percentage(count(*), WHERE error IS true) AS 'Taxa de Erro (%)'
FROM Transaction
WHERE appName = 'oficina-service'
TIMESERIES AUTO
```

### Top 10 Erros por Tipo
```sql
SELECT count(*) AS 'Ocorrências', 
       latest(error.message) AS 'Última Mensagem'
FROM TransactionError
WHERE appName = 'oficina-service'
FACET error.class
SINCE 24 hours ago
LIMIT 10
```

### Erros por Endpoint
```sql
SELECT count(*) AS 'Erros'
FROM TransactionError
WHERE appName = 'oficina-service'
FACET request.uri
SINCE 1 hour ago
LIMIT 20
```

### Timeline de Erros
```sql
SELECT count(*)
FROM TransactionError
WHERE appName = 'oficina-service'
FACET error.class
TIMESERIES 10 minutes
SINCE 6 hours ago
```

### Erros de Integração
```sql
SELECT count(*) AS 'Falhas de Integração'
FROM Metric
WHERE metricName = 'oficina.ordem_servico.erros.integracao'
TIMESERIES AUTO
```

### Stack Trace de Erros Recentes
```sql
SELECT timestamp, error.class, error.message, stack_trace
FROM TransactionError
WHERE appName = 'oficina-service'
SINCE 1 hour ago
LIMIT 10
```

## Integrações Externas

### Performance de Integrações
```sql
SELECT 
  count(*) AS 'Total',
  average(duration) AS 'Latência Média',
  percentile(duration, 95) AS 'P95',
  percentage(count(*), WHERE httpResponseCode < 400) AS 'Taxa Sucesso (%)'
FROM Transaction
WHERE appName = 'oficina-service' 
  AND name LIKE '%integration%'
FACET name
SINCE 1 hour ago
```

### Latência de Integrações
```sql
SELECT average(duration)
FROM Transaction
WHERE appName = 'oficina-service' 
  AND name LIKE '%integration%'
FACET name
TIMESERIES AUTO
```

### Falhas em Chamadas Externas
```sql
SELECT count(*)
FROM Transaction
WHERE appName = 'oficina-service'
  AND name LIKE '%integration%'
  AND httpResponseCode >= 400
FACET name, httpResponseCode
SINCE 6 hours ago
```

## Database Performance

### Tempo de Queries
```sql
SELECT 
  average(databaseDuration) AS 'Média',
  percentile(databaseDuration, 95) AS 'P95',
  percentile(databaseDuration, 99) AS 'P99'
FROM Transaction
WHERE appName = 'oficina-service'
TIMESERIES AUTO
```

### Slow Queries (> 1 segundo)
```sql
SELECT count(*) AS 'Quantidade',
       average(databaseDuration) AS 'Duração Média'
FROM Transaction
WHERE appName = 'oficina-service' 
  AND databaseDuration > 1
FACET name
SINCE 1 hour ago
LIMIT 20
```

### Database Calls por Transação
```sql
SELECT average(databaseCallCount) AS 'Média de Queries',
       max(databaseCallCount) AS 'Máximo'
FROM Transaction
WHERE appName = 'oficina-service'
TIMESERIES AUTO
```

### Problemas de Connection Pool
```sql
SELECT count(*) AS 'Connection Errors'
FROM TransactionError
WHERE appName = 'oficina-service'
  AND error.message LIKE '%connection%pool%'
TIMESERIES 5 minutes
```

## Monitoramento de Kubernetes

### CPU Usage por Pod
```sql
SELECT average(cpuUsedCores/cpuLimitCores*100) AS 'CPU Usage (%)'
FROM K8sContainerSample
WHERE containerName = 'oficina-service'
FACET podName
TIMESERIES AUTO
```

### Memória Usage por Pod
```sql
SELECT average(memoryUsedBytes/memoryLimitBytes*100) AS 'Memory Usage (%)'
FROM K8sContainerSample
WHERE containerName = 'oficina-service'
FACET podName
TIMESERIES AUTO
```

### Pod Restarts
```sql
SELECT max(restartCount) AS 'Restarts'
FROM K8sContainerSample
WHERE containerName = 'oficina-service'
FACET podName
SINCE 24 hours ago
```

### Network Traffic por Pod
```sql
SELECT 
  sum(net.rxBytesPerSecond)/1024/1024 AS 'RX (MB/s)',
  sum(net.txBytesPerSecond)/1024/1024 AS 'TX (MB/s)'
FROM K8sContainerSample
WHERE containerName = 'oficina-service'
FACET podName
TIMESERIES AUTO
```

### Status dos Pods
```sql
SELECT uniqueCount(podName) AS 'Pods Ativos'
FROM K8sContainerSample
WHERE containerName = 'oficina-service'
  AND status = 'Running'
TIMESERIES AUTO
```

### Resource Limits vs Usage
```sql
SELECT 
  average(cpuUsedCores) AS 'CPU Usado',
  average(cpuLimitCores) AS 'CPU Limit',
  average(memoryUsedBytes)/1024/1024/1024 AS 'Memory Usado (GB)',
  average(memoryLimitBytes)/1024/1024/1024 AS 'Memory Limit (GB)'
FROM K8sContainerSample
WHERE containerName = 'oficina-service'
TIMESERIES AUTO
```

## Health Checks

### Health Check Status
```sql
SELECT count(*)
FROM Transaction
WHERE appName = 'oficina-service'
  AND name LIKE '%/actuator/health%'
FACET httpResponseCode
TIMESERIES 1 minute
```

### Liveness Probe Failures
```sql
SELECT count(*) AS 'Failures'
FROM Transaction
WHERE appName = 'oficina-service'
  AND name LIKE '%/actuator/health/liveness%'
  AND httpResponseCode != 200
TIMESERIES 5 minutes
```

### Readiness Probe Failures
```sql
SELECT count(*) AS 'Failures'
FROM Transaction
WHERE appName = 'oficina-service'
  AND name LIKE '%/actuator/health/readiness%'
  AND httpResponseCode != 200
TIMESERIES 5 minutes
```

## Logs Estruturados

### Logs de Criação de Ordens
```sql
SELECT timestamp, message, traceId, ordemServicoId, userId
FROM Log
WHERE service = 'oficina-service'
  AND message LIKE '%ordem_servico_criada%'
SINCE 1 hour ago
LIMIT 100
```

### Logs de Erro
```sql
SELECT timestamp, level, logger, message, traceId, stackTrace
FROM Log
WHERE service = 'oficina-service'
  AND level = 'ERROR'
SINCE 1 hour ago
LIMIT 50
```

### Rastreamento de Requisição Específica
```sql
SELECT timestamp, level, logger, message, spanId
FROM Log
WHERE service = 'oficina-service'
  AND traceId = 'TRACE_ID_AQUI'
ORDER BY timestamp
```

### Volume de Logs por Nível
```sql
SELECT count(*)
FROM Log
WHERE service = 'oficina-service'
FACET level
TIMESERIES AUTO
```

## Análise de Usuários

### Requisições por Usuário
```sql
SELECT count(*) AS 'Requisições'
FROM Transaction
WHERE appName = 'oficina-service'
FACET userId
SINCE 1 hour ago
LIMIT 20
```

### Latência por Usuário
```sql
SELECT average(duration) AS 'Latência Média'
FROM Transaction
WHERE appName = 'oficina-service'
FACET userId
SINCE 1 hour ago
LIMIT 20
```

## Alertas e SLA

### Uptime da Aplicação
```sql
SELECT percentage(count(*), WHERE httpResponseCode < 500)
FROM Transaction
WHERE appName = 'oficina-service'
SINCE 1 day ago
```

### SLA Target (99.9%)
```sql
SELECT 
  percentage(count(*), WHERE duration <= 1 AND error IS false) AS 'SLA Compliance (%)'
FROM Transaction
WHERE appName = 'oficina-service'
SINCE 1 month ago
TIMESERIES 1 day
```

### Disponibilidade por Hora
```sql
SELECT percentage(count(*), WHERE httpResponseCode < 500) AS 'Disponibilidade (%)'
FROM Transaction
WHERE appName = 'oficina-service'
FACET hourOf(timestamp)
SINCE 7 days ago
```

## Queries Customizadas para Business Metrics

### Ordens Criadas vs Concluídas
```sql
SELECT 
  sum(oficina.ordem_servico.criadas.total) AS 'Criadas',
  sum(oficina.ordem_servico.concluidas.total) AS 'Concluídas'
FROM Metric
TIMESERIES 1 hour
SINCE 1 week ago
```

### Tempo Médio de Conclusão
```sql
SELECT average(oficina.ordem_servico.status.finalizacao.tempo) AS 'Tempo Conclusão (ms)'
FROM Metric
TIMESERIES 1 day
SINCE 1 month ago
```

### Taxa de Cancelamento
```sql
SELECT 
  sum(oficina.ordem_servico.canceladas.total) / 
  sum(oficina.ordem_servico.criadas.total) * 100 AS 'Taxa Cancelamento (%)'
FROM Metric
SINCE 1 week ago
TIMESERIES 1 day
```

## Dicas de Uso

### Usar COMPARE WITH
```sql
SELECT count(*) FROM Transaction 
WHERE appName = 'oficina-service'
SINCE 1 day ago 
COMPARE WITH 1 day ago
```

### Filtrar por Hora do Dia
```sql
SELECT count(*) FROM Transaction
WHERE appName = 'oficina-service'
  AND hourOf(timestamp) BETWEEN 8 AND 18
SINCE 1 week ago
```

### Agrupar por Dia da Semana
```sql
SELECT count(*) AS 'Requisições'
FROM Transaction
WHERE appName = 'oficina-service'
FACET weekdayOf(timestamp)
SINCE 4 weeks ago
```

### Usar Funções de Agregação
```sql
SELECT 
  count(*) AS 'Total',
  average(duration) AS 'Média',
  min(duration) AS 'Mínimo',
  max(duration) AS 'Máximo',
  stddev(duration) AS 'Desvio Padrão'
FROM Transaction
WHERE appName = 'oficina-service'
```
