# ✅ Checklist de Verificação - Monitoramento New Relic

Use este checklist após a instalação para verificar se tudo está funcionando corretamente.

## 📋 Pré-requisitos

- [ ] New Relic Account criada
- [ ] License Key obtida
- [ ] Kubernetes cluster acessível
- [ ] kubectl configurado
- [ ] Docker instalado

---

## 🔧 1. Instalação do New Relic Infrastructure

### 1.1 Namespace e Secrets

```bash
# Verificar namespace
kubectl get namespace newrelic
```
**Esperado**: `newrelic   Active   Xm`

```bash
# Verificar secret da license key
kubectl get secret -n newrelic newrelic-license-key
```
**Esperado**: Secret existe

```bash
# Verificar conteúdo do secret (opcional)
kubectl get secret -n newrelic newrelic-license-key -o jsonpath='{.data.license-key}' | base64 -d
```
**Esperado**: Sua license key

**Checklist:**
- [ ] Namespace `newrelic` criado
- [ ] Secret `newrelic-license-key` existe
- [ ] License key está correta

---

### 1.2 DaemonSet New Relic Infrastructure

```bash
# Verificar DaemonSet
kubectl get daemonset -n newrelic
```
**Esperado**:
```
NAME                      DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR   AGE
newrelic-infrastructure   3         3         3       3            3           <none>          Xm
```

```bash
# Verificar pods
kubectl get pods -n newrelic -l app=newrelic-infrastructure
```
**Esperado**: Um pod por node, todos `Running`

```bash
# Verificar logs
kubectl logs -n newrelic -l app=newrelic-infrastructure --tail=50
```
**Esperado**: Logs mostrando conexão com New Relic, sem erros críticos

**Checklist:**
- [ ] DaemonSet criado
- [ ] Pod em cada node do cluster
- [ ] Todos os pods em estado `Running`
- [ ] Logs sem erros de conexão
- [ ] Dados aparecendo no New Relic Infrastructure

---

### 1.3 Kube State Metrics

```bash
# Verificar deployment
kubectl get deployment -n newrelic kube-state-metrics
```
**Esperado**: `READY 1/1`

```bash
# Verificar pod
kubectl get pods -n newrelic -l app=kube-state-metrics
```
**Esperado**: Pod `Running`

```bash
# Testar endpoint de métricas
kubectl port-forward -n newrelic svc/kube-state-metrics 8080:8080
curl http://localhost:8080/metrics | head -20
```
**Esperado**: Métricas em formato Prometheus

**Checklist:**
- [ ] Deployment `kube-state-metrics` criado
- [ ] Pod `Running`
- [ ] Service expondo porta 8080
- [ ] Endpoint `/metrics` respondendo

---

## 🏗️ 2. Aplicação (oficina-service)

### 2.1 Build e Deploy

```bash
# Build da imagem
cd oficina-service-k8s
docker build -t oficina-service:latest .
```
**Esperado**: Build bem-sucedido, New Relic Agent baixado

```bash
# Verificar arquivo newrelic.jar na imagem
docker run --rm oficina-service:latest ls -la /app/newrelic/
```
**Esperado**: Arquivo `newrelic.jar` presente

**Checklist:**
- [ ] Docker build completado sem erros
- [ ] New Relic Agent presente na imagem
- [ ] Arquivo `newrelic.yml` copiado

---

### 2.2 Secrets e ConfigMaps

```bash
# Criar secret no namespace oficina
kubectl create namespace oficina
kubectl create secret generic newrelic-secret \
  --from-literal=license-key=YOUR_LICENSE_KEY \
  -n oficina
```

```bash
# Verificar secret
kubectl get secret -n oficina newrelic-secret
```
**Esperado**: Secret existe

**Checklist:**
- [ ] Namespace `oficina` criado
- [ ] Secret `newrelic-secret` criado
- [ ] ConfigMaps existentes (se necessário)

---

### 2.3 Deployment

```bash
# Deploy
kubectl apply -f k8s/base/

# Verificar deployment
kubectl get deployment -n oficina oficina-deployment
```
**Esperado**: `READY 2/2`

```bash
# Verificar pods
kubectl get pods -n oficina
```
**Esperado**: 2 pods `Running`

```bash
# Verificar variáveis de ambiente
kubectl get pod -n oficina -l app=oficina -o jsonpath='{.items[0].spec.containers[0].env}' | jq
```
**Esperado**: `NEW_RELIC_LICENSE_KEY`, `NEW_RELIC_ENVIRONMENT`, `NEW_RELIC_APP_NAME` presentes

**Checklist:**
- [ ] Deployment criado
- [ ] Pods rodando (2/2)
- [ ] Variáveis de ambiente configuradas
- [ ] Resources (CPU/Memory) definidos

---

### 2.4 New Relic Agent na Aplicação

```bash
# Verificar logs do pod
kubectl logs -n oficina -l app=oficina --tail=100 | grep -i "newrelic"
```
**Esperado**: Mensagens como:
```
New Relic Agent v8.x.x has successfully connected to New Relic
Agent is reporting data to: oficina-service-dev
```

```bash
# Verificar processo Java com agent
kubectl exec -n oficina deployment/oficina-deployment -- ps aux | grep java
```
**Esperado**: Comando Java com `-javaagent:/app/newrelic/newrelic.jar`

```bash
# Verificar logs do New Relic Agent
kubectl exec -n oficina deployment/oficina-deployment -- cat /app/newrelic/logs/newrelic_agent.log
```
**Esperado**: Logs mostrando inicialização bem-sucedida

**Checklist:**
- [ ] Agent carregado no startup
- [ ] Conexão com New Relic estabelecida
- [ ] App name correto sendo reportado
- [ ] Sem erros nos logs do agent

---

## 🔍 3. Logs Estruturados

### 3.1 Formato JSON

```bash
# Verificar logs
kubectl logs -n oficina deployment/oficina-deployment --tail=10
```
**Esperado**: Logs em formato JSON

```bash
# Verificar campos do JSON
kubectl logs -n oficina deployment/oficina-deployment --tail=1 | jq
```
**Esperado**: Campos incluindo:
- `timestamp`
- `level`
- `logger`
- `message`
- `service`
- `environment`

**Checklist:**
- [ ] Logs em formato JSON
- [ ] Campos obrigatórios presentes
- [ ] Timestamp no formato ISO 8601

---

### 3.2 Correlação de Requisições

```bash
# Fazer requisição
curl -v http://localhost:8080/api/clientes

# Verificar headers de resposta
```
**Esperado**: Headers `X-Trace-Id`, `X-Span-Id`, `X-Request-Id`

```bash
# Verificar logs da requisição
kubectl logs -n oficina deployment/oficina-deployment --tail=50 | jq 'select(.requestUri=="/api/clientes")'
```
**Esperado**: Logs com:
- `traceId`
- `spanId`
- `requestId`
- `requestUri`
- `requestMethod`

**Checklist:**
- [ ] Headers de correlação retornados
- [ ] MDC fields presentes nos logs
- [ ] Mesmo `traceId` em todos os logs da requisição

---

## 📊 4. Métricas Customizadas

### 4.1 Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health | jq
```
**Esperado**:
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "status": "Connection OK"
      }
    }
  }
}
```

```bash
# Liveness probe
curl http://localhost:8080/actuator/health/liveness
```
**Esperado**: `{"status":"UP"}`

```bash
# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```
**Esperado**: `{"status":"UP"}`

**Checklist:**
- [ ] `/actuator/health` respondendo
- [ ] `/actuator/health/liveness` UP
- [ ] `/actuator/health/readiness` UP
- [ ] Database health indicator funcionando

---

### 4.2 Métricas Prometheus

```bash
# Listar métricas disponíveis
curl http://localhost:8080/actuator/metrics | jq '.names[]' | grep oficina
```
**Esperado**: Métricas customizadas:
```
oficina.ordem_servico.criadas.total
oficina.ordem_servico.concluidas.total
oficina.ordem_servico.status.diagnostico
...
```

```bash
# Verificar métrica específica
curl http://localhost:8080/actuator/metrics/oficina.ordem_servico.criadas.total | jq
```
**Esperado**: Valor numérico

```bash
# Endpoint Prometheus
curl http://localhost:8080/actuator/prometheus | grep oficina_ordem_servico
```
**Esperado**: Métricas em formato Prometheus

**Checklist:**
- [ ] Endpoint `/actuator/metrics` respondendo
- [ ] Métricas customizadas presentes
- [ ] Endpoint `/actuator/prometheus` exposto
- [ ] Formato Prometheus correto

---

### 4.3 Teste de Métricas em Ação

```bash
# Criar uma ordem de serviço
curl -X POST http://localhost:8080/api/ordens-servico \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "clienteId": "...",
    "veiculoId": "...",
    "descricao": "Teste de métricas"
  }'
```

```bash
# Verificar logs
kubectl logs -n oficina deployment/oficina-deployment --tail=20 | grep "ordem_servico_criada"
```
**Esperado**: Log estruturado do evento

```bash
# Verificar incremento da métrica
curl http://localhost:8080/actuator/metrics/oficina.ordem_servico.criadas.total | jq '.measurements[0].value'
```
**Esperado**: Valor incrementado

**Checklist:**
- [ ] Logs estruturados gerados
- [ ] Contadores incrementados
- [ ] Timers registrados
- [ ] Eventos enviados para New Relic

---

## 🎯 5. New Relic Platform

### 5.1 APM

Acesse: https://one.newrelic.com/ → APM & Services

**Verificar:**
- [ ] Aplicação `oficina-service-dev` aparece
- [ ] Throughput mostrando dados
- [ ] Latência (P95, P99) disponível
- [ ] Transações listadas
- [ ] Error rate calculado

**Drill down:**
- [ ] Clicar em uma transação e ver detalhes
- [ ] Distributed trace funcionando
- [ ] Database queries aparecendo
- [ ] Stack traces de erros disponíveis

---

### 5.2 Logs

Acesse: Logs → oficina-service

**Verificar:**
- [ ] Logs aparecendo
- [ ] Formato JSON preservado
- [ ] Campos de correlação presentes
- [ ] Busca por `traceId` funciona
- [ ] Busca por `level:ERROR` funciona

**Testar queries:**
```
service:oficina-service AND level:ERROR
service:oficina-service AND message:*ordem_servico_criada*
traceId:SPECIFIC_TRACE_ID
```

---

### 5.3 Infrastructure

Acesse: Infrastructure → Kubernetes

**Verificar:**
- [ ] Cluster aparece
- [ ] Nodes listados
- [ ] Pods do `oficina-service` aparecem
- [ ] Métricas de CPU/Memory visíveis
- [ ] Eventos do Kubernetes capturados

**Métricas importantes:**
- [ ] CPU usage por pod
- [ ] Memory usage por pod
- [ ] Network traffic
- [ ] Pod restarts

---

### 5.4 Métricas Customizadas

Query Builder (NRQL):

```sql
SELECT * FROM Metric 
WHERE metricName LIKE 'oficina.%' 
SINCE 1 hour ago
```

**Esperado**: Métricas customizadas listadas

```sql
SELECT sum(oficina.ordem_servico.criadas.total) 
FROM Metric 
SINCE today
```

**Esperado**: Valor numérico

**Checklist:**
- [ ] Métricas customizadas aparecendo
- [ ] Queries NRQL funcionando
- [ ] Dados em tempo real

---

## 🔔 6. Alertas

### 6.1 Importar Políticas

1. Acesse: Alerts & AI → Alert Policies
2. Importe `docs/newrelic-alerts-config.yml`

**Checklist:**
- [ ] Políticas importadas
- [ ] Condições configuradas
- [ ] Thresholds corretos

---

### 6.2 Canais de Notificação

**Configurar:**
- [ ] Email
- [ ] Slack (opcional)
- [ ] PagerDuty (opcional)

**Testar:**
- [ ] Enviar teste de notificação
- [ ] Verificar recebimento

---

### 6.3 Testar Alerta

```bash
# Gerar carga para disparar alerta de latência
for i in {1..1000}; do
  curl http://localhost:8080/api/clientes &
done
```

**Verificar:**
- [ ] Alerta disparou no New Relic
- [ ] Notificação recebida
- [ ] Incident criado

---

## 📈 7. Dashboards

### 7.1 Importar Dashboard

1. Acesse: Dashboards
2. Import dashboard
3. Carregue `docs/newrelic-dashboard.json`

**Checklist:**
- [ ] Dashboard importado
- [ ] Todas as páginas carregadas (4 páginas)
- [ ] Widgets mostrando dados

---

### 7.2 Verificar Widgets

**Overview Page:**
- [ ] Throughput mostrando dados
- [ ] Latência (P50, P95, P99) visível
- [ ] Taxa de erro calculada
- [ ] CPU/Memory por pod

**Ordens de Serviço Page:**
- [ ] Volume diário funcionando
- [ ] Distribuição por status
- [ ] Tempo médio por status
- [ ] Taxa de sucesso

**Integrações Page:**
- [ ] Status de integrações
- [ ] Erros listados
- [ ] Timeline de erros

**Database Page:**
- [ ] Query time
- [ ] Slow queries
- [ ] Connection pool status

---

## 🧪 8. Testes E2E

### 8.1 Fluxo Completo

```bash
# 1. Autenticar
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"cpf":"12345678900"}' | jq -r '.token')

# 2. Criar ordem de serviço
OS_ID=$(curl -X POST http://localhost:8080/api/ordens-servico \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...}' | jq -r '.id')

# 3. Atualizar status
curl -X PUT http://localhost:8080/api/ordens-servico/$OS_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"EXECUCAO"}'
```

**Verificar no New Relic:**
- [ ] 3 transações apareceram
- [ ] Distributed trace mostra as 3 chamadas
- [ ] Logs correlacionados pelo `traceId`
- [ ] Métricas incrementadas:
  - [ ] `oficina.ordem_servico.criadas.total`
  - [ ] `oficina.ordem_servico.atualizadas.total`

---

### 8.2 Teste de Erro

```bash
# Gerar erro proposital
curl -X POST http://localhost:8080/api/ordens-servico \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"invalid":"data"}'
```

**Verificar:**
- [ ] Erro aparece nos Logs (level: ERROR)
- [ ] TransactionError registrado no APM
- [ ] Stack trace disponível
- [ ] Métrica `oficina.ordem_servico.erros.criacao` incrementada

---

## ✅ Checklist Final

### Infraestrutura
- [ ] New Relic Infrastructure DaemonSet rodando
- [ ] Kube State Metrics rodando
- [ ] Dados aparecendo no New Relic Infrastructure

### Aplicação
- [ ] Pods rodando com New Relic Agent
- [ ] Agent conectado ao New Relic
- [ ] Logs em formato JSON
- [ ] Correlação de requisições funcionando

### Métricas
- [ ] Actuator endpoints respondendo
- [ ] Health checks funcionando
- [ ] Métricas customizadas expostas
- [ ] Dados no formato Prometheus

### New Relic Platform
- [ ] APM mostrando dados
- [ ] Logs sendo coletados
- [ ] Infrastructure com métricas K8s
- [ ] Métricas customizadas aparecendo

### Alertas e Dashboards
- [ ] Políticas de alertas importadas
- [ ] Canais de notificação configurados
- [ ] Dashboards importados
- [ ] Todos os widgets funcionando

### Testes
- [ ] Fluxo E2E executado com sucesso
- [ ] Traces distribuídos funcionando
- [ ] Erros sendo capturados
- [ ] Métricas incrementando corretamente

---

## 🐛 Troubleshooting Rápido

### Problema: Agent não conecta

```bash
# Verificar license key
kubectl get secret -n oficina newrelic-secret -o jsonpath='{.data.license-key}' | base64 -d

# Verificar conectividade
kubectl exec -n oficina deployment/oficina-deployment -- ping -c 3 collector.newrelic.com

# Ver logs do agent
kubectl exec -n oficina deployment/oficina-deployment -- cat /app/newrelic/logs/newrelic_agent.log
```

### Problema: Métricas não aparecem

```bash
# Verificar se MetricsService está funcionando
kubectl logs -n oficina deployment/oficina-deployment | grep MetricsService

# Testar endpoint
kubectl port-forward -n oficina svc/oficina-service 8080:8080
curl http://localhost:8080/actuator/prometheus | grep oficina
```

### Problema: Logs não são JSON

```bash
# Verificar se logback-spring.xml está sendo carregado
kubectl exec -n oficina deployment/oficina-deployment -- cat /app/classes/logback-spring.xml

# Verificar dependência
kubectl exec -n oficina deployment/oficina-deployment -- ls -la /app/libs/ | grep logstash
```

---

**Checklist Completo**: ✅  
**Data**: __________  
**Verificado por**: __________  
**Ambiente**: [ ] Dev [ ] Staging [ ] Production
