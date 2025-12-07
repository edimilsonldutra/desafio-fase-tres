# ADR-004: Padrão de Comunicação REST Síncrona

**Status**: Aceito  
**Data**: 2025-12-07  
**Decisores**: Edimilson L. Dutra, Equipe de Arquitetura  
**Relacionado**: ADR-002 (Comunicação Síncrona vs Assíncrona)

---

## 📋 Contexto

Tendo decidido usar **comunicação síncrona** como padrão (ADR-002), precisamos definir:

1. **Protocolo**: HTTP/1.1 vs HTTP/2 vs gRPC
2. **Estilo de API**: REST vs GraphQL vs RPC
3. **Formato de Dados**: JSON vs Protocol Buffers vs XML
4. **Versionamento**: URL path vs Headers vs Query params
5. **Autenticação**: OAuth 2.0 vs JWT vs API Keys
6. **Error Handling**: Códigos HTTP vs Códigos customizados
7. **Paginação**: Offset-based vs Cursor-based
8. **Idempotência**: Como garantir operações idempotentes

### Requisitos
- **Simplicidade**: Equipe precisa adotar rapidamente
- **Interoperabilidade**: Clientes Web, Mobile e externos
- **Performance**: Latência <500ms para 95% das requisições
- **Manutenibilidade**: Fácil adicionar novos endpoints sem breaking changes

---

## ⚖️ Decisão

**Adotar HTTP/1.1 REST com JSON**, seguindo princípios RESTful e melhores práticas da indústria.

### Especificação Completa

| Aspecto | Decisão | Justificativa |
|---------|---------|---------------|
| **Protocolo** | HTTP/1.1 | Ampla compatibilidade, debugging simples |
| **Estilo** | REST (Richardson Maturity Level 2) | Padrão da indústria, intuitivo |
| **Formato** | JSON (camelCase) | Universalmente suportado, legível |
| **Versionamento** | URL Path (`/api/v1/`) | Explícito, fácil rotear |
| **Autenticação** | JWT Bearer Token | Stateless, escala horizontalmente |
| **Status Codes** | HTTP Standard (200, 201, 400, 404, 500) | Convenção universal |
| **Paginação** | Offset-based (`?page=1&size=20`) | Simples, suficiente para casos de uso |
| **Idempotência** | Idempotency-Key header | Previne duplicação (POST, PUT, PATCH) |
| **CORS** | Habilitado para domínios específicos | Suporta Web clients |
| **Rate Limiting** | 10000 req/s (API Gateway) | Proteção contra abuso |

---

## 🎯 Justificativa Técnica

### 1. HTTP/1.1 vs HTTP/2 vs gRPC

#### ✅ HTTP/1.1 (Escolhido)

**Prós**:
- **Universalmente suportado**: Navegadores, mobile SDKs, curl, Postman
- **Debugging fácil**: Logs legíveis, ferramentas dev maduras
- **Proxies compatíveis**: ALB, CloudFront, NGINX todos suportam
- **Caching padrão**: Headers `Cache-Control`, `ETag` funcionam out-of-the-box

**Contras**:
- **Sem multiplexing**: 1 requisição por conexão (mitigado com HTTP pipelining)
- **Overhead de headers**: Headers repetidos em cada request (≤1KB, aceitável)

**Quando reconsiderar HTTP/2**:
- Quando tivermos muitas requisições pequenas (ex: real-time updates)
- Quando multiplexing for crítico (ex: chat application)

---

#### ❌ HTTP/2 (Rejeitado para agora)

**Prós**:
- **Multiplexing**: Múltiplas requisições na mesma conexão
- **Header compression**: HPACK reduz overhead
- **Server push**: Servidor pode enviar recursos antes de serem pedidos

**Contras**:
- **Complexidade**: Debugging mais difícil (binário)
- **Overhead inicial**: Handshake mais complexo
- **Incompatibilidade**: Alguns proxies antigos não suportam
- **Não necessário**: Nosso throughput (500 req/min) não justifica

**Decisão**: Manter HTTP/1.1, migrar para HTTP/2 se throughput > 5000 req/min.

---

#### ❌ gRPC (Rejeitado)

**Prós**:
- **Performance**: Protocol Buffers são compactos e rápidos
- **Streaming**: Suporta bi-directional streaming
- **Geração de código**: Clients gerados automaticamente

**Contras**:
- **Incompatibilidade com browsers**: Precisa de proxy (gRPC-Web)
- **Debugging difícil**: Binário, precisa de ferramentas especiais
- **Overhead de setup**: Proto files, code generation
- **Equipe não familiar**: Curva de aprendizado

**Quando reconsiderar**:
- Para comunicação **interna** entre microserviços (Pod ↔ Pod)
- Se adotarmos arquitetura de microsserviços distribuídos (10+ services)

---

### 2. REST vs GraphQL

#### ✅ REST (Escolhido)

**Prós**:
- **Simplicidade**: 1 endpoint = 1 recurso = 1 responsabilidade
- **Caching**: HTTP caching funciona naturalmente (`GET /customers/123`)
- **Ferramentas maduras**: Swagger/OpenAPI, Postman, REST clients
- **Baixa curva de aprendizado**: Equipe já conhece

**Contras**:
- **Over-fetching**: Cliente recebe campos não utilizados
- **Under-fetching**: Precisa de múltiplas requisições (N+1 problem)
- **Versionamento**: Mudanças podem quebrar clientes

**Mitigações**:
- **Sparse Fieldsets**: `GET /customers?fields=id,name` (se necessário)
- **Composite endpoints**: `/work-orders/{id}?include=customer,vehicle` (casos específicos)

---

#### ❌ GraphQL (Rejeitado)

**Prós**:
- **Flexibilidade**: Cliente escolhe exatamente os campos desejados
- **1 request**: Busca dados relacionados em uma chamada
- **Schema-driven**: Type safety, auto-documentation

**Contras**:
- **Complexidade**: Precisa de GraphQL server (Apollo, GraphQL Java)
- **Caching difícil**: Não usa HTTP caching padrão
- **Vulnerabilidade**: Queries complexas podem sobrecarregar DB (N+1, deep nesting)
- **Overhead de parsing**: Query parsing adiciona latência

**Quando reconsiderar**:
- Se front-end precisar de alta flexibilidade (ex: admin dashboard complexo)
- Se tivermos muitos relacionamentos complexos (grafo de dados)

**Decisão**: Manter REST, considerar GraphQL apenas para admin dashboard (futuro).

---

### 3. JSON vs Protocol Buffers vs XML

#### ✅ JSON (Escolhido)

**Prós**:
- **Human-readable**: Fácil debugar, logs legíveis
- **Universalmente suportado**: Navegadores nativamente suportam
- **Schema flexível**: Adicionar campos sem quebrar clientes
- **Ferramentas**: JQ, JSON Schema, JSON Patch

**Formato**:
```json
{
  "id": "uuid-1234",
  "customer": {
    "id": "uuid-customer",
    "name": "João Silva",
    "cpf": "12345678901"
  },
  "status": "DIAGNOSTICO",
  "total": 450.00,
  "createdAt": "2025-12-07T10:30:00Z"
}
```

**Convenções**:
- **camelCase** para propriedades (`createdAt`, não `created_at`)
- **ISO 8601** para timestamps (`2025-12-07T10:30:00Z`)
- **Decimal strings** para dinheiro (`"450.00"`, não `450`)
- **Null explícito**: `"email": null` (não omitir campo)

**Contras**:
- **Tamanho**: ~30% maior que Protobuf (mas compressão Gzip reduz)
- **Parsing**: Mais lento que binário (mas diferença < 10ms)

---

#### ❌ Protocol Buffers (Rejeitado)

**Prós**:
- **Compacto**: ~70% menor que JSON
- **Rápido**: Parsing 5-10x mais rápido
- **Schema enforced**: Type safety

**Contras**:
- **Binário**: Não legível, difícil debugar
- **Schema required**: `.proto` files, compilação
- **Incompatibilidade**: Navegadores não suportam nativamente

**Decisão**: Considerar apenas para comunicação **interna** de alta performance.

---

#### ❌ XML (Rejeitado)

**Prós**:
- **Schema rígido**: XSD validation
- **Namespaces**: Evita conflitos

**Contras**:
- **Verboso**: 2-3x maior que JSON
- **Parsing lento**: DOM parsing é pesado
- **Legado**: Tecnologia em declínio

**Decisão**: Não usar, exceto para integrações legadas obrigatórias.

---

### 4. Versionamento de API

#### ✅ URL Path Versioning (Escolhido)

**Formato**: `/api/v1/customers`, `/api/v2/customers`

**Prós**:
- **Explícito**: Versão visível na URL
- **Fácil rotear**: ALB/API Gateway podem rotear por path
- **Cacheable**: Diferentes versões têm URLs distintas
- **Testável**: Fácil testar v1 e v2 lado a lado

**Exemplo**:
```
GET /api/v1/customers/123           → v1 response (deprecated)
GET /api/v2/customers/123           → v2 response (current)
```

**Política de Deprecação**:
1. Anunciar deprecation 6 meses antes
2. Adicionar header `X-API-Deprecation-Date: 2026-06-01`
3. Manter v1 por 12 meses após v2
4. Retornar HTTP 410 Gone após EOL

---

#### ❌ Header Versioning (Rejeitado)

**Formato**: `Accept: application/vnd.oficina.v1+json`

**Prós**:
- **RESTful "correto"**: Usa content negotiation
- **URL limpa**: Sem `/v1/` no path

**Contras**:
- **Difícil debugar**: Versão não visível na URL
- **Caching complexo**: Precisa de `Vary: Accept` header
- **Routing difícil**: API Gateway não pode rotear por header facilmente

**Decisão**: Não usar, complexidade não justifica.

---

### 5. Autenticação e Autorização

#### ✅ JWT Bearer Token (Escolhido)

**Formato**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**JWT Payload**:
```json
{
  "sub": "uuid-customer-123",
  "cpf": "12345678901",
  "name": "João Silva",
  "email": "joao@email.com",
  "roles": ["CUSTOMER"],
  "iat": 1701950400,
  "exp": 1701954000
}
```

**Prós**:
- **Stateless**: Não precisa consultar DB em cada request
- **Escala horizontalmente**: Não depende de sessão no servidor
- **Descentralizado**: Qualquer serviço pode validar
- **Padrão**: RFC 7519, amplamente suportado

**Implementação**:
- **Algorithm**: HMAC-SHA256 (HS256)
- **Secret**: Armazenado em Secrets Manager
- **Expiration**: 1 hora (3600s)
- **Refresh Token**: 7 dias (implementar em v2)

**Validação**:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) {
        String token = extractToken(request);
        if (token != null && jwtService.validateToken(token)) {
            Claims claims = jwtService.parseToken(token);
            Authentication auth = new JwtAuthentication(claims);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
```

---

#### ❌ OAuth 2.0 (Rejeitado para agora)

**Prós**:
- **Granular**: Scopes, delegação de acesso
- **Third-party**: Permite login social (Google, Facebook)

**Contras**:
- **Complexidade**: Authorization Server, refresh tokens, scopes
- **Overhead**: Múltiplos endpoints (authorize, token, revoke)
- **Não necessário**: Nosso caso de uso é simples (autenticação direta)

**Quando reconsiderar**: Se precisarmos de login social ou API para terceiros.

---

### 6. Códigos de Status HTTP

**Convenção padrão** (RFC 7231):

| Código | Uso | Exemplo |
|--------|-----|---------|
| **200 OK** | Requisição bem-sucedida | `GET /customers/123` |
| **201 Created** | Recurso criado | `POST /work-orders` |
| **204 No Content** | Sucesso sem corpo | `DELETE /customers/123` |
| **400 Bad Request** | Validação falhou | `POST /work-orders` (campos inválidos) |
| **401 Unauthorized** | Token inválido/ausente | Qualquer endpoint protegido |
| **403 Forbidden** | Sem permissão | `DELETE /work-orders/123` (não é dono) |
| **404 Not Found** | Recurso não existe | `GET /customers/999` |
| **409 Conflict** | Conflito de estado | `POST /work-orders` (estoque insuficiente) |
| **422 Unprocessable Entity** | Regra de negócio violada | `POST /work-orders` (cliente bloqueado) |
| **429 Too Many Requests** | Rate limit excedido | Qualquer endpoint (>10000 req/s) |
| **500 Internal Server Error** | Erro inesperado | Exceção não tratada |
| **503 Service Unavailable** | Serviço temporariamente indisponível | Deploy em andamento |

**Formato de Erro Padronizado**:
```json
{
  "timestamp": "2025-12-07T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Campo 'description' é obrigatório",
  "path": "/api/v1/work-orders",
  "traceId": "abc123",
  "requestId": "req-789",
  "details": [
    {
      "field": "description",
      "message": "não deve estar vazio",
      "rejectedValue": null
    }
  ]
}
```

---

### 7. Paginação

#### ✅ Offset-based (Escolhido)

**Formato**: `GET /customers?page=1&size=20`

**Resposta**:
```json
{
  "content": [
    {"id": "uuid-1", "name": "João"},
    {"id": "uuid-2", "name": "Maria"}
  ],
  "page": {
    "number": 1,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "links": {
    "first": "/api/v1/customers?page=0&size=20",
    "prev": "/api/v1/customers?page=0&size=20",
    "self": "/api/v1/customers?page=1&size=20",
    "next": "/api/v1/customers?page=2&size=20",
    "last": "/api/v1/customers?page=7&size=20"
  }
}
```

**Prós**:
- **Simples**: Fácil implementar (`LIMIT, OFFSET`)
- **Jump to page**: Usuário pode ir direto para página 5
- **Total count**: Útil para UIs (`Página 1 de 8`)

**Contras**:
- **Inconsistência**: Dados podem mudar entre páginas
- **Performance**: `OFFSET 10000` é lento em tabelas grandes

**Mitigação**:
- Limitar `size` máximo a 100
- Adicionar índice em colunas de ordenação
- Recomendar cursor-based para exports grandes

---

#### ❌ Cursor-based (Considerado para futuro)

**Formato**: `GET /customers?cursor=eyJpZCI6InV1aWQtMTIzIn0=&size=20`

**Prós**:
- **Consistente**: Não pula/duplica itens
- **Performance**: `WHERE id > ?` é rápido com índice

**Contras**:
- **Não pode pular**: Não tem "ir para página 5"
- **Complexidade**: Cursor precisa ser codificado (Base64)

**Decisão**: Implementar apenas se performance de offset for problema.

---

### 8. Idempotência

**Problema**: Requisições duplicadas (ex: usuário clica "Criar" 2x).

#### ✅ Idempotency-Key Header (Escolhido)

**Cliente envia**:
```
POST /api/v1/work-orders
Idempotency-Key: uuid-client-generated-123
Content-Type: application/json

{...}
```

**Servidor armazena**:
```sql
CREATE TABLE idempotency_keys (
  key VARCHAR(255) PRIMARY KEY,
  response_status INT,
  response_body JSONB,
  created_at TIMESTAMP,
  expires_at TIMESTAMP
);

-- TTL: 24 horas
```

**Lógica**:
```java
@PostMapping("/work-orders")
public ResponseEntity<?> createWorkOrder(
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody WorkOrderDTO dto) {
    
    // Check cache
    IdempotencyRecord record = idempotencyRepo.findByKey(idempotencyKey);
    if (record != null) {
        return ResponseEntity
            .status(record.getStatus())
            .body(record.getBody());
    }
    
    // Process request
    WorkOrder order = service.create(dto);
    ResponseEntity response = ResponseEntity
        .status(201)
        .body(order);
    
    // Store result
    idempotencyRepo.save(new IdempotencyRecord(
        idempotencyKey,
        201,
        order,
        LocalDateTime.now().plusHours(24)
    ));
    
    return response;
}
```

**Vantagens**:
- **Prevenção de duplicação**: Múltiplos cliques não criam ordens duplicadas
- **Network retry**: Cliente pode retentar com segurança
- **Idempotente**: `POST` se comporta como `PUT`

**Quando usar**:
- `POST /work-orders` ✅
- `PUT /work-orders/{id}` ❌ (já é idempotente)
- `DELETE /work-orders/{id}` ❌ (já é idempotente)

---

## 📐 Design Principles

### 1. Richardson Maturity Model - Level 2

**Level 0**: Single endpoint, single method (SOAP-like)  
**Level 1**: Multiple endpoints, single method  
**Level 2**: HTTP verbs + status codes ✅ **Nosso nível**  
**Level 3**: HATEOAS (hypermedia links)

**Decisão**: Level 2 é suficiente. HATEOAS adiciona complexidade sem benefícios claros.

---

### 2. Resource-Oriented Design

**Recursos (Substantivos)**:
- `/customers` (não `/getCustomers`)
- `/work-orders` (não `/createOrder`)
- `/vehicles` (não `/vehicleList`)

**Verbos HTTP**:
```
GET    /customers           → List all
GET    /customers/123       → Get one
POST   /customers           → Create
PUT    /customers/123       → Replace (full update)
PATCH  /customers/123       → Partial update
DELETE /customers/123       → Delete
```

**Sub-recursos**:
```
GET /customers/123/vehicles              → List customer's vehicles
GET /customers/123/work-orders           → List customer's work orders
POST /work-orders/123/approve            → Action on resource (OK, not RESTful purist)
```

---

### 3. Naming Conventions

| Tipo | Convenção | Exemplo |
|------|-----------|---------|
| **URLs** | kebab-case | `/work-orders`, `/approval-requests` |
| **JSON fields** | camelCase | `customerId`, `createdAt` |
| **Query params** | camelCase | `?sortBy=name&includeDeleted=false` |
| **Headers** | Kebab-Case | `Idempotency-Key`, `X-Trace-Id` |
| **Enums** | UPPER_SNAKE_CASE | `DIAGNOSTICO`, `EM_EXECUCAO` |

---

### 4. CORS Configuration

**Allowed Origins** (produção):
```yaml
cors:
  allowed-origins:
    - https://oficina.com
    - https://admin.oficina.com
    - https://mobile-app.oficina.com  # Deep links
  allowed-methods:
    - GET
    - POST
    - PUT
    - PATCH
    - DELETE
  allowed-headers:
    - Authorization
    - Content-Type
    - Idempotency-Key
    - X-Request-Id
  exposed-headers:
    - X-Total-Count
    - X-Trace-Id
  max-age: 3600  # 1 hour
```

---

### 5. Rate Limiting

**API Gateway**:
- **Global**: 10000 req/s
- **Per IP**: 100 req/min
- **Per User**: 1000 req/hour

**Headers de resposta**:
```
HTTP/1.1 200 OK
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 950
X-RateLimit-Reset: 1701954000
```

**Quando excedido**:
```
HTTP/1.1 429 Too Many Requests
Retry-After: 60

{
  "error": "Rate limit exceeded",
  "retryAfter": 60
}
```

---

## 🧪 Exemplos de Endpoints

### Autenticação
```http
POST /api/v1/auth/validate
Content-Type: application/json

{
  "cpf": "12345678901"
}

→ 200 OK
{
  "token": "eyJhbGci...",
  "expiresIn": 3600,
  "customer": {
    "id": "uuid-123",
    "name": "João Silva"
  }
}
```

---

### Listar Clientes (com paginação)
```http
GET /api/v1/customers?page=0&size=20&sort=name,asc
Authorization: Bearer eyJhbGci...

→ 200 OK
{
  "content": [...],
  "page": {...},
  "links": {...}
}
```

---

### Criar Ordem de Serviço (com idempotência)
```http
POST /api/v1/work-orders
Authorization: Bearer eyJhbGci...
Idempotency-Key: uuid-client-123
Content-Type: application/json

{
  "customerId": "uuid-customer",
  "vehicleId": "uuid-vehicle",
  "description": "Troca de óleo",
  "services": [{"id": "uuid-srv1", "quantity": 1}],
  "parts": [{"id": "uuid-part1", "quantity": 4}]
}

→ 201 Created
Location: /api/v1/work-orders/uuid-os-123
{
  "id": "uuid-os-123",
  "status": "DIAGNOSTICO",
  "total": 450.00,
  "createdAt": "2025-12-07T10:30:00Z"
}
```

---

### Atualizar Status (PATCH parcial)
```http
PATCH /api/v1/work-orders/uuid-os-123
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "status": "EM_EXECUCAO"
}

→ 200 OK
{
  "id": "uuid-os-123",
  "status": "EM_EXECUCAO",
  "updatedAt": "2025-12-07T11:00:00Z"
}
```

---

### Erro de Validação
```http
POST /api/v1/work-orders
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "customerId": "",  // Inválido
  "description": ""  // Inválido
}

→ 400 Bad Request
{
  "timestamp": "2025-12-07T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "path": "/api/v1/work-orders",
  "traceId": "abc123",
  "details": [
    {
      "field": "customerId",
      "message": "não deve estar vazio"
    },
    {
      "field": "description",
      "message": "não deve estar vazio"
    }
  ]
}
```

---

## 📚 OpenAPI/Swagger Specification

**Gerar documentação automaticamente**:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

**Acessar**:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

**Exemplo de anotação**:
```java
@RestController
@RequestMapping("/api/v1/work-orders")
@Tag(name = "Work Orders", description = "Gestão de Ordens de Serviço")
public class WorkOrderController {
    
    @PostMapping
    @Operation(summary = "Criar ordem de serviço", 
               description = "Cria uma nova ordem de serviço com serviços e peças")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "409", description = "Estoque insuficiente")
    })
    public ResponseEntity<WorkOrderDTO> create(
        @Parameter(description = "Chave de idempotência")
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        
        @Parameter(description = "Dados da ordem de serviço")
        @Valid @RequestBody WorkOrderCreateDTO dto) {
        // ...
    }
}
```

---

## 📊 Monitoramento e Observabilidade

### New Relic APM

**Métricas automáticas**:
- Throughput por endpoint
- Latência (P50, P95, P99)
- Error rate por status code
- Database query time

**Custom Attributes em Transactions**:
```java
NewRelic.addCustomParameter("customerId", customerId);
NewRelic.addCustomParameter("workOrderId", workOrderId);
NewRelic.addCustomParameter("total", total);
```

**Dashboard NRQL**:
```sql
-- Throughput por endpoint
SELECT count(*) FROM Transaction
WHERE appName = 'oficina-service'
FACET request.uri
TIMESERIES 1 minute

-- Error rate por status code
SELECT percentage(count(*), WHERE httpResponseCode >= 400)
FROM Transaction
WHERE appName = 'oficina-service'
TIMESERIES 5 minutes

-- Latência P95 por endpoint
SELECT percentile(duration, 95)
FROM Transaction
WHERE appName = 'oficina-service'
FACET request.uri
SINCE 1 hour ago
```

---

## 🎓 Melhores Práticas

### ✅ DO

1. **Sempre retornar JSON consistente**
   ```json
   {"data": {...}, "error": null}
   {"data": null, "error": {...}}
   ```

2. **Usar UTC para timestamps**
   ```json
   "createdAt": "2025-12-07T10:30:00Z"
   ```

3. **Incluir links HATEOAS em lista de recursos** (nível 3, opcional)
   ```json
   {
     "content": [...],
     "links": {
       "self": "/customers?page=1",
       "next": "/customers?page=2"
     }
   }
   ```

4. **Validar entrada com Bean Validation**
   ```java
   @NotNull(message = "customerId não deve ser nulo")
   private String customerId;
   ```

5. **Logar requisições com correlação**
   ```java
   log.info("Order created: orderId={}, customerId={}, traceId={}",
            orderId, customerId, traceId);
   ```

---

### ❌ DON'T

1. **Não retornar stacks traces para cliente**
   ```json
   // ❌ ERRADO
   {"error": "NullPointerException at line 42..."}
   
   // ✅ CORRETO
   {"error": "Erro interno do servidor", "traceId": "abc123"}
   ```

2. **Não usar verbos em URLs**
   ```
   ❌ POST /createOrder
   ✅ POST /orders
   ```

3. **Não quebrar idempotência de GET**
   ```java
   ❌ GET /customers/increment-counter  // Side effect!
   ✅ POST /customers/123/views         // Correto
   ```

4. **Não retornar 200 OK para erros**
   ```json
   ❌ 200 OK {"success": false, "error": "..."}
   ✅ 400 Bad Request {"error": "..."}
   ```

5. **Não ignorar Content-Type**
   ```java
   ❌ Aceitar qualquer content-type
   ✅ @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
   ```

---

## 🔮 Próximos Passos

### Curto Prazo (3 meses)
1. **API Versioning**: Preparar para v2
2. **Refresh Tokens**: Implementar para JWT
3. **GraphQL Endpoint**: Admin dashboard (opcional)

### Médio Prazo (6 meses)
4. **Webhooks**: Notificar clientes de mudanças
5. **Rate Limiting Granular**: Por endpoint
6. **API Gateway v2**: Migrar para HTTP/2

### Longo Prazo (12 meses)
7. **gRPC Interno**: Para comunicação Pod ↔ Pod
8. **GraphQL Federation**: Se adotarmos microsserviços
9. **API Marketplace**: Abrir APIs para parceiros

---

## 📚 Referências

- **REST API Design**: https://restfulapi.net/
- **HTTP Status Codes**: https://httpstatuses.com/
- **OpenAPI Spec**: https://spec.openapis.org/oas/latest.html
- **JWT RFC**: https://datatracker.ietf.org/doc/html/rfc7519
- **Idempotency**: https://stripe.com/docs/api/idempotent_requests

---

**Documento aprovado por**: Equipe de Arquitetura  
**Próxima revisão**: 2026-06-07 (6 meses)
