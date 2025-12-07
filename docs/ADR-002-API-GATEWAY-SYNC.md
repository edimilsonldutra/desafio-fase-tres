# ADR-002: Comunicação Síncrona vs Assíncrona entre APIs

**Status**: Aceito  
**Data**: 2025-12-05  
**Decisores**: Edimilson L. Dutra, Equipe de Arquitetura  

---

## 📋 Contexto

Precisamos decidir como as APIs do sistema se comunicarão entre si, especialmente nos seguintes cenários:

1. **Autenticação → Business APIs**: Validação de token JWT
2. **Order Service → Inventory Service**: Verificação de estoque
3. **Order Service → External API**: Aprovação de orçamento
4. **Notificações**: Email/SMS para clientes

### Requisitos
- **Latência**: Usuários esperam resposta em <2 segundos
- **Consistência**: Ordens de serviço devem ser transacionais
- **Resiliência**: Falhas em serviços externos não devem derrubar o sistema
- **Escalabilidade**: Suportar picos de 1000 req/s

---

## ⚖️ Decisão

**Escolhemos comunicação SÍNCRONA (HTTP REST)** como padrão, com exceções específicas para comunicação assíncrona:

### Comunicação Síncrona (HTTP REST)
- ✅ Autenticação (API Gateway → Lambda)
- ✅ Business APIs internas (Clientes, Veículos, Ordens)
- ✅ Consultas de estoque (para validação)
- ✅ Aprovação de orçamento (com circuit breaker)

### Comunicação Assíncrona (Event-Driven)
- ✅ Notificações (email/SMS)
- ✅ Auditoria (log de eventos)
- ✅ Relatórios (processamento batch)

---

## 🎯 Justificativa

### Por que Síncrona como Padrão?

#### ✅ Prós

1. **Simplicidade**
   - HTTP REST é padrão da indústria
   - Fácil debugar com ferramentas (curl, Postman, browser DevTools)
   - Menos componentes para gerenciar (sem message brokers)

2. **Consistência Forte**
   - Request-response garante ACID
   - Usuário recebe confirmação imediata
   - Não precisa de eventual consistency

3. **Developer Experience**
   - Equipe já conhece REST
   - Frameworks maduros (Spring Boot, Express)
   - Testável com mocks simples

4. **Latência Previsível**
   - Usuário espera e recebe resposta
   - Não precisa de polling ou webhooks
   - Error handling direto (HTTP status codes)

#### ❌ Contras

1. **Acoplamento Temporal**
   - Cliente bloqueia esperando resposta
   - Se serviço downstream cai, requisição falha

2. **Escalabilidade Limitada**
   - Picos podem sobrecarregar serviços downstream
   - Precisa de rate limiting e circuit breakers

3. **Latência Cumulativa**
   - Latência total = soma de todas as chamadas
   - Chamadas em cadeia podem demorar segundos

---

### Quando Usar Assíncrona?

#### ✅ Casos de Uso

1. **Notificações (Email/SMS)**
   - Usuário não precisa esperar envio
   - Pode falhar e retentar depois
   - SQS + Lambda consumer

2. **Auditoria**
   - Logs de eventos não bloqueiam transação
   - Pode ser processado depois
   - EventBridge + Lambda

3. **Relatórios**
   - Processamento demorado (minutos)
   - Usuário recebe resultado por email
   - S3 + Lambda + SES

#### ✅ Prós

1. **Desacoplamento**
   - Produtor e consumidor independentes
   - Falhas não propagam

2. **Escalabilidade**
   - Message broker absorve picos
   - Consumidores podem escalar independentemente

3. **Resiliência**
   - Retry automático
   - Dead Letter Queue para falhas

#### ❌ Contras

1. **Complexidade**
   - Precisa de message broker (SQS, SNS, Kafka)
   - Mais componentes para monitorar

2. **Eventual Consistency**
   - Usuário não sabe se processou
   - Precisa de mecanismo de consulta

3. **Debugging Difícil**
   - Rastreamento distribuído complexo
   - Ordem de processamento não garantida

---

## 🏗️ Arquitetura de Comunicação

```
┌─────────────────────────────────────────────────────────────┐
│                      SYNCHRONOUS FLOWS                       │
└─────────────────────────────────────────────────────────────┘

1. Authentication Flow (HTTP REST - Síncrono)
   Client → API Gateway → Lambda → RDS → Lambda → Client
   Latency: 50-100ms

2. Create Work Order (HTTP REST - Síncrono)
   Client → API Gateway → ALB → Pod → RDS → Pod → Client
   Latency: 150-300ms

3. External Budget Approval (HTTP REST - Síncrono com Circuit Breaker)
   Pod → External API → Pod
   Timeout: 5s
   Retry: 3x with exponential backoff
   Fallback: Manual approval queue

┌─────────────────────────────────────────────────────────────┐
│                     ASYNCHRONOUS FLOWS                       │
└─────────────────────────────────────────────────────────────┘

1. Send Notification (Event-Driven)
   Pod → SNS Topic → SQS Queue → Lambda → SES/SNS
   Processing Time: Seconds to minutes
   Retry: Automatic (3x)

2. Audit Log (Event-Driven)
   Pod → EventBridge → Lambda → CloudWatch Logs
   Processing Time: Near real-time
   Retention: 30 days

3. Generate Report (Event-Driven)
   Scheduled (CloudWatch Events) → Lambda → RDS → S3 → SES
   Processing Time: Minutes
   Frequency: Daily at 6 AM
```

---

## 📊 Comparação Detalhada

### Comunicação Síncrona (HTTP REST)

| Aspecto | Valor | Exemplo |
|---------|-------|---------|
| **Protocolo** | HTTP/1.1, HTTP/2 | REST, gRPC |
| **Latência** | 10-300ms | API Gateway → Lambda: 50ms |
| **Throughput** | 100-1000 req/s/pod | Spring Boot: 500 req/s |
| **Resiliência** | Média (precisa circuit breaker) | Resilience4j |
| **Complexidade** | Baixa | curl, Postman |
| **Custo** | $0 (sem broker) | Apenas compute |
| **Consistency** | Forte (ACID) | Transações síncronas |

#### Implementação (Spring Boot)

```java
@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderController {
    
    @Autowired
    private WorkOrderService workOrderService;
    
    @Autowired
    private ExternalBudgetClient externalBudgetClient;
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<WorkOrder> approveWorkOrder(@PathVariable Long id) {
        // 1. Get work order (Synchronous - DB)
        WorkOrder workOrder = workOrderService.findById(id);
        
        // 2. Call external API (Synchronous - HTTP with Circuit Breaker)
        try {
            BudgetApproval approval = externalBudgetClient.approve(workOrder);
            workOrder.setApprovalCode(approval.getCode());
        } catch (FeignException e) {
            // Fallback: Manual approval queue
            workOrder.setStatus(WorkOrderStatus.PENDING_MANUAL_APPROVAL);
        }
        
        // 3. Update order (Synchronous - DB)
        workOrder = workOrderService.update(workOrder);
        
        return ResponseEntity.ok(workOrder);
    }
}

// Circuit Breaker Configuration
@Configuration
public class CircuitBreakerConfig {
    
    @Bean
    public CircuitBreakerConfig externalBudgetCircuitBreaker() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // 50% failure rate
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .build();
    }
}
```

---

### Comunicação Assíncrona (Event-Driven)

| Aspecto | Valor | Exemplo |
|---------|-------|---------|
| **Protocolo** | Message Queue | SQS, SNS, Kafka |
| **Latência** | Seconds to minutes | SQS: 1-5s |
| **Throughput** | 1000+ msg/s | SQS: 3000 msg/s |
| **Resiliência** | Alta (retry automático) | DLQ |
| **Complexidade** | Média-Alta | Precisa broker |
| **Custo** | $5-20/mês | SQS: $0.40/1M msg |
| **Consistency** | Eventual | At-least-once delivery |

#### Implementação (SNS + SQS + Lambda)

```java
// Spring Boot - Publish Event
@Service
public class WorkOrderService {
    
    @Autowired
    private AmazonSNS snsClient;
    
    public void createWorkOrder(WorkOrder workOrder) {
        // 1. Save to database (Synchronous)
        workOrder = workOrderRepository.save(workOrder);
        
        // 2. Publish event (Asynchronous - Fire and Forget)
        publishWorkOrderCreatedEvent(workOrder);
    }
    
    private void publishWorkOrderCreatedEvent(WorkOrder workOrder) {
        String message = new ObjectMapper().writeValueAsString(Map.of(
            "eventType", "WorkOrderCreated",
            "orderId", workOrder.getId(),
            "customerId", workOrder.getCustomerId(),
            "amount", workOrder.getTotalPrice()
        ));
        
        snsClient.publish(new PublishRequest()
            .withTopicArn("arn:aws:sns:sa-east-1:123456789:work-order-events")
            .withMessage(message));
    }
}

// Lambda Consumer - Process Event
public class NotificationHandler implements RequestHandler<SQSEvent, Void> {
    
    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            try {
                // Parse event
                Map<String, Object> data = parseEvent(msg.getBody());
                
                // Send notification
                sendEmail(data.get("customerId"), data.get("orderId"));
                
            } catch (Exception e) {
                context.getLogger().log("Error processing event: " + e.getMessage());
                throw e; // Retry via SQS
            }
        }
        return null;
    }
    
    private void sendEmail(String customerId, String orderId) {
        // Get customer email
        String email = customerRepository.findById(customerId).getEmail();
        
        // Send via SES
        sesClient.sendEmail(new SendEmailRequest()
            .withDestination(new Destination().withToAddresses(email))
            .withMessage(new Message()
                .withSubject(new Content("Ordem de Serviço Criada"))
                .withBody(new Body().withText(new Content("Sua OS " + orderId + " foi criada com sucesso!"))))
            .withSource("noreply@oficina.com.br"));
    }
}
```

---

## 🔍 Alternativas Consideradas

### Alternativa 1: Event-Driven Full (Kafka/SNS)

#### ✅ Prós
- Desacoplamento total
- Escalabilidade ilimitada
- Resiliência alta

#### ❌ Contras
- Complexidade extrema
- Latência alta (eventual consistency)
- Difícil debugar
- Custo de Kafka (MSK): $150/mês

**Motivo da Rejeição**: Over-engineering para nosso caso de uso. Sistema não precisa de eventual consistency para operações core.

---

### Alternativa 2: GraphQL Federation

#### ✅ Prós
- API unificada
- Cliente escolhe campos
- Menos roundtrips

#### ❌ Contras
- Curva de aprendizado
- Overhead de parsing
- Cache complexo

**Motivo da Rejeição**: Equipe não tem experiência. REST atende bem.

---

### Alternativa 3: gRPC

#### ✅ Prós
- Performance (Protobuf binário)
- Streaming bidirecional
- Type-safe contracts

#### ❌ Contras
- Não suportado por browsers (precisa gRPC-Web)
- Menos tooling que REST
- Curva de aprendizado

**Motivo da Rejeição**: Overhead de aprendizado não justifica ganhos de performance.

---

## 📈 Consequências

### Positivas ✅
1. **Simplicidade**: REST é amplamente conhecido
2. **Consistência**: ACID garantido em operações críticas
3. **Debugging Fácil**: Logs e traces simples
4. **Latência Baixa**: <300ms para 95% das requisições
5. **Custo Baixo**: Sem message brokers para maioria dos fluxos

### Negativas ❌
1. **Acoplamento Temporal**: Requisição bloqueia até resposta
2. **Cascading Failures**: Falha em downstream afeta upstream (mitigado com circuit breaker)
3. **Escalabilidade Limitada**: Precisa rate limiting e throttling
4. **Picos de Tráfego**: Podem sobrecarregar serviços (mitigado com HPA)

### Riscos ⚠️
1. **External API Timeout**: Aprovação de orçamento pode demorar >5s
   - **Mitigação**: Circuit breaker + fallback para fila manual
2. **Database Connection Pool Exhaustion**: Muitas requests simultâneas
   - **Mitigação**: HikariCP com max 20 connections + HPA
3. **Latência Cumulativa**: Chamadas em cadeia podem ultrapassar 2s
   - **Mitigação**: Caching de dados frequentes + otimização de queries

---

## 🚀 Plano de Implementação

### Fase 1: REST APIs (Semana 1-3)
- [ ] Implementar endpoints REST (Spring Boot)
- [ ] Configurar Feign clients para chamadas síncronas
- [ ] Testes de integração

### Fase 2: Circuit Breakers (Semana 4)
- [ ] Implementar Resilience4j
- [ ] Configurar fallbacks para external APIs
- [ ] Testes de resiliência (chaos engineering)

### Fase 3: Async Flows (Semana 5-6)
- [ ] Configurar SNS topics para eventos
- [ ] Criar SQS queues + DLQ
- [ ] Lambda consumers para notificações

### Fase 4: Monitoramento (Semana 7)
- [ ] CloudWatch dashboards (latência, errors)
- [ ] X-Ray distributed tracing
- [ ] Alarmes para circuit breaker trips

---

## 🔄 Revisão

Esta decisão será **reavaliada em 6 meses** ou se:
- Latência P95 ultrapassar 500ms por 2 semanas consecutivas
- Circuit breaker trips >10% das requisições
- Picos de tráfego causarem >5% de errors

**Possíveis Evoluções**:
- Migrar notificações para SNS/SQS (Fase 3)
- Adicionar cache (Redis) para queries frequentes
- Implementar gRPC para comunicação interna (se performance for gargalo)

---

## 📚 Referências

- [Microservices Patterns - Chris Richardson](https://microservices.io/patterns/communication-style/messaging.html)
- [REST vs gRPC vs GraphQL](https://www.apollographql.com/blog/graphql-vs-rest)
- [Resilience4j Circuit Breaker](https://resilience4j.readme.io/docs/circuitbreaker)
- [AWS Messaging Services Comparison](https://aws.amazon.com/messaging/)

---

**Status**: Aceito  
**Data de Decisão**: 2025-12-05  
**Última Revisão**: 2025-12-05  
**Próxima Revisão**: 2026-06-05
