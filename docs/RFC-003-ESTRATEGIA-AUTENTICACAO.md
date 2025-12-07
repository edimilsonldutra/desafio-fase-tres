# RFC-003: Estratégia de Autenticação

**Status**: Aprovado  
**Data**: 2025-12-05  
**Autor**: Edimilson L. Dutra  
**Revisores**: Equipe de Arquitetura, Security Team  

---

## 📋 Sumário Executivo

Este RFC documenta a decisão técnica sobre qual estratégia de autenticação e autorização utilizar para o Sistema de Gestão de Oficina Mecânica.

---

## 🎯 Problema

Precisamos autenticar clientes e mecânicos com diferentes níveis de acesso:

### Requisitos de Segurança
1. **Autenticação de Clientes**: Via CPF (11 dígitos)
2. **Autenticação de Mecânicos**: Via credenciais (email + senha)
3. **Sessão Segura**: Tokens com expiração
4. **Stateless**: Sem armazenamento de sessão no servidor
5. **Renovação**: Refresh tokens para evitar re-autenticação

### Requisitos de Negócio
1. **Simplicidade**: Clientes não precisam criar senha
2. **Segurança**: Senhas seguras para mecânicos (admin)
3. **Escalabilidade**: Suportar milhares de sessões simultâneas
4. **Auditoria**: Rastreamento de todas as ações

---

## 🔍 Opções Avaliadas

### Opção 1: JWT (JSON Web Tokens)

#### ✅ Prós
- **Stateless**: Nenhum armazenamento no servidor
- **Escalável**: Sem dependência de sessões centralizadas
- **Auto-Contido**: Token contém todas as claims necessárias
- **Padrão da Indústria**: RFC 7519 amplamente adotado
- **Cross-Platform**: Funciona em web, mobile, APIs
- **Performance**: Validação rápida (verificação de assinatura)
- **Descentralizado**: API Gateway pode validar sem chamar Lambda

#### ❌ Contras
- **Revogação**: Difícil invalidar antes da expiração
- **Tamanho**: Tokens grandes (150-300 bytes) em headers
- **Secrets Management**: Chave secreta precisa ser rotacionada
- **Sem Estado**: Não rastreia sessões ativas

#### 🔐 Implementação
```javascript
// Estrutura do JWT
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "123",           // Customer ID
    "cpf": "12345678901",   // Customer CPF
    "name": "João Silva",   // Customer Name
    "role": "CUSTOMER",     // Role
    "iat": 1638360000,      // Issued At
    "exp": 1638363600       // Expiration (1 hour)
  },
  "signature": "..." // HMAC-SHA256
}
```

#### 💰 Custo
- **Implementação**: Biblioteca gratuita (jose4j, jsonwebtoken)
- **Secrets Manager**: $0.40/secret/month
- **API Gateway**: Sem custo adicional (validação built-in)
- **Total**: **~$0.40/mês**

---

### Opção 2: OAuth 2.0 + OpenID Connect

#### ✅ Prós
- **Padrão Moderno**: OAuth 2.0 + OIDC (RFC 6749, 6750)
- **Delegação**: Permite login social (Google, Facebook)
- **Scopes**: Autorização granular por recurso
- **Refresh Tokens**: Renovação sem re-autenticação
- **Revogação**: Mecanismo nativo de revoke
- **Identity Providers**: Cognito, Auth0, Okta prontos

#### ❌ Contras
- **Complexidade**: Fluxo de autorização mais complexo
- **Overhead**: Múltiplas chamadas (authorize, token, refresh)
- **Custo**: Cognito/Auth0 cobram por MAU (Monthly Active Users)
- **Over-Engineering**: Excessivo para nosso caso de uso simples

#### 💰 Custo (AWS Cognito)
- **50k MAU**: $0.0055 × 50000 = $275/mês
- **SMS MFA**: $0.00645/SMS (opcional)
- **Total**: **~$275/mês**

---

### Opção 3: API Keys

#### ✅ Prós
- **Simplicidade**: Apenas uma string fixa
- **Performance**: Validação instantânea (lookup em cache)
- **Controle**: Fácil revogar (deletar do banco)

#### ❌ Contras
- **Segurança**: Keys podem vazar (logs, URLs)
- **Sem Expiração**: Válidas indefinidamente
- **Sem Contexto**: Não carrega informações do usuário
- **Rotação Manual**: Requer intervenção do usuário

---

### Opção 4: Session-Based (Cookies)

#### ✅ Prós
- **Revogação Fácil**: Basta deletar sessão no Redis
- **Segurança**: HttpOnly cookies protegem contra XSS
- **Auditoria**: Rastreia sessões ativas facilmente

#### ❌ Contras
- **Stateful**: Requer Redis/Memcached
- **Escalabilidade**: Gargalo no armazenamento de sessões
- **CORS**: Complicado para SPAs/mobile apps
- **Custo**: Redis cluster (~$50/mês)

---

## 📊 Matriz de Decisão

| Critério | Peso | JWT | OAuth 2.0 | API Keys | Sessions |
|----------|------|-----|-----------|----------|----------|
| **Segurança** | 25% | 8 | 10 | 4 | 7 |
| **Escalabilidade** | 20% | 10 | 8 | 9 | 5 |
| **Simplicidade** | 20% | 9 | 5 | 10 | 7 |
| **Custo** | 15% | 10 | 3 | 10 | 6 |
| **Revogação** | 10% | 5 | 10 | 9 | 10 |
| **Performance** | 10% | 9 | 6 | 10 | 7 |
| **Total** | 100% | **8.45** | **7.15** | **7.85** | **6.75** |

### Cálculo
- **JWT**: (8×0.25) + (10×0.2) + (9×0.2) + (10×0.15) + (5×0.1) + (9×0.1) = **8.45**
- **OAuth 2.0**: (10×0.25) + (8×0.2) + (5×0.2) + (3×0.15) + (10×0.1) + (6×0.1) = **7.15**
- **API Keys**: (4×0.25) + (9×0.2) + (10×0.2) + (10×0.15) + (9×0.1) + (10×0.1) = **7.85**
- **Sessions**: (7×0.25) + (5×0.2) + (7×0.2) + (6×0.15) + (10×0.1) + (7×0.1) = **6.75**

---

## ✅ Decisão

**Escolhemos JWT (JSON Web Tokens)** com as seguintes características:

### Arquitetura da Solução

```
┌─────────────────────────────────────────────────────────┐
│                    AUTHENTICATION FLOW                   │
└─────────────────────────────────────────────────────────┘

1. Cliente → POST /auth/validate {cpf: "123"}
2. Lambda valida CPF no RDS
3. Lambda gera JWT com HS256
4. Cliente armazena token (localStorage)
5. Requisições incluem: Authorization: Bearer <token>
6. API Gateway valida JWT (custom authorizer)
7. Se válido, passa request para backend
8. Backend extrai claims do JWT (sub, cpf, role)
```

### Estrutura do Token

#### Access Token (Expiration: 1 hora)
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "cpf": "12345678901",
  "name": "João Silva",
  "email": "joao@example.com",
  "role": "CUSTOMER",
  "iat": 1701784800,
  "exp": 1701788400,
  "iss": "oficina-auth-service",
  "aud": "oficina-api"
}
```

#### Refresh Token (Expiration: 7 dias)
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "type": "refresh",
  "iat": 1701784800,
  "exp": 1702389600,
  "jti": "unique-token-id"
}
```

### Fatores Decisivos

1. **Stateless e Escalável**
   - Nenhuma dependência de Redis/Memcached
   - Lambda pode escalar infinitamente
   - API Gateway valida tokens sem backend

2. **Performance**
   - Validação de assinatura: <1ms
   - Sem roundtrips para banco de dados
   - Cache de chaves públicas (se usar RS256 no futuro)

3. **Custo-Benefício**
   - $0.40/mês (apenas Secrets Manager)
   - Sem custos de Cognito ($275/mês economizados)
   - Sem custos de Redis ($50/mês economizados)

4. **Simplicidade**
   - Implementação direta com biblioteca `jsonwebtoken` (Java)
   - Integração nativa com API Gateway (Custom Authorizer)
   - Formato padrão da indústria

5. **Segurança**
   - Assinatura HMAC-SHA256 (32 bytes de secret)
   - Expiração curta (1 hora) limita janela de ataque
   - Refresh tokens armazenados em httpOnly cookies
   - Secret rotacionado via Secrets Manager (30 dias)

---

## 🛡️ Mitigação de Riscos

### Risco 1: Revogação de Tokens

**Problema**: JWT não pode ser invalidado antes da expiração

**Soluções**:
1. **Expiração Curta (1 hora)**: Minimiza janela de risco
2. **Refresh Token Blacklist**: Armazenar JTI de tokens revogados no DynamoDB
   ```javascript
   // Verificação na validação do JWT
   if (await isTokenBlacklisted(token.jti)) {
     throw new Error('Token revoked');
   }
   ```
3. **Logout**: Adiciona refresh token na blacklist

**Custo**: DynamoDB on-demand ~$2/mês

---

### Risco 2: Token Leakage

**Problema**: Token pode vazar em logs, URLs, etc.

**Soluções**:
1. **HTTPS Only**: TLS 1.3 obrigatório
2. **No URL Params**: Token apenas em header `Authorization`
3. **Logging**: Sanitizar tokens em logs (regex)
   ```javascript
   log.info(`Request headers: ${sanitize(headers)}`);
   // Authorization: Bearer ey*** → Authorization: Bearer [REDACTED]
   ```
4. **Secure Storage**: localStorage com XSS protection

---

### Risco 3: Secret Key Compromise

**Problema**: Se secret vazar, todos os tokens são comprometidos

**Soluções**:
1. **Secrets Manager**: Secret armazenado criptografado (KMS)
2. **Rotação Automática**: A cada 30 dias
3. **Multi-Version**: Suportar 2 versões durante rotação
   ```javascript
   // Validar com ambas as chaves durante período de transição
   const keys = [currentSecret, previousSecret];
   for (const key of keys) {
     try {
       return jwt.verify(token, key);
     } catch (err) {
       continue; // Tentar próxima chave
     }
   }
   throw new Error('Invalid token');
   ```
4. **Monitoring**: CloudWatch alarm se secret for acessado >1000x/hora

---

## 🚀 Plano de Implementação

### Fase 1: Lambda Auth Service (Semana 1)

```java
// LambdaAuthHandler.java
public class LambdaAuthHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String cpf = extractCpf(input);
        
        // Validate CPF format
        if (!isValidCpf(cpf)) {
            return errorResponse(400, "CPF inválido");
        }
        
        // Query customer from RDS
        Customer customer = customerRepository.findByCpf(cpf);
        if (customer == null) {
            return errorResponse(404, "Cliente não encontrado");
        }
        
        // Generate JWT
        String secret = getSecretFromSecretsManager("jwt-secret");
        String accessToken = generateAccessToken(customer, secret);
        String refreshToken = generateRefreshToken(customer, secret);
        
        // Return tokens
        return successResponse(200, Map.of(
            "accessToken", accessToken,
            "refreshToken", refreshToken,
            "expiresIn", 3600,
            "customer", customer
        ));
    }
    
    private String generateAccessToken(Customer customer, String secret) {
        return Jwts.builder()
            .setSubject(customer.getId())
            .claim("cpf", customer.getCpf())
            .claim("name", customer.getName())
            .claim("role", "CUSTOMER")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .setIssuer("oficina-auth-service")
            .setAudience("oficina-api")
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }
}
```

### Fase 2: API Gateway Custom Authorizer (Semana 2)

```java
// JwtAuthorizerHandler.java
public class JwtAuthorizerHandler implements RequestHandler<APIGatewayCustomAuthorizerEvent, APIGatewayCustomAuthorizerResponse> {
    
    @Override
    public APIGatewayCustomAuthorizerResponse handleRequest(APIGatewayCustomAuthorizerEvent input, Context context) {
        String token = extractToken(input.getAuthorizationToken());
        
        try {
            // Verify JWT
            String secret = getSecretFromSecretsManager("jwt-secret");
            Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
            
            // Check if blacklisted (optional)
            if (isTokenBlacklisted(claims.getId())) {
                return denyPolicy(claims.getSubject(), input.getMethodArn());
            }
            
            // Generate IAM policy
            return allowPolicy(claims.getSubject(), input.getMethodArn(), claims);
            
        } catch (JwtException e) {
            context.getLogger().log("Invalid token: " + e.getMessage());
            return denyPolicy("user", input.getMethodArn());
        }
    }
    
    private APIGatewayCustomAuthorizerResponse allowPolicy(String principalId, String methodArn, Claims claims) {
        return APIGatewayCustomAuthorizerResponse.builder()
            .withPrincipalId(principalId)
            .withPolicyDocument(PolicyDocument.builder()
                .withStatement(Statement.builder()
                    .withEffect("Allow")
                    .withAction("execute-api:Invoke")
                    .withResource(methodArn)
                    .build())
                .build())
            .withContext(Map.of(
                "customerId", claims.getSubject(),
                "cpf", claims.get("cpf", String.class),
                "role", claims.get("role", String.class)
            ))
            .build();
    }
}
```

### Fase 3: Token Refresh Endpoint (Semana 3)

```java
// POST /auth/refresh
public APIGatewayProxyResponseEvent refreshToken(APIGatewayProxyRequestEvent input, Context context) {
    String refreshToken = extractRefreshToken(input);
    
    try {
        // Verify refresh token
        String secret = getSecretFromSecretsManager("jwt-secret");
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(refreshToken)
            .getBody();
        
        // Check if type is refresh
        if (!"refresh".equals(claims.get("type"))) {
            return errorResponse(400, "Invalid token type");
        }
        
        // Check if blacklisted
        if (isTokenBlacklisted(claims.getId())) {
            return errorResponse(401, "Token revoked");
        }
        
        // Get customer from database
        Customer customer = customerRepository.findById(claims.getSubject());
        
        // Generate new tokens
        String newAccessToken = generateAccessToken(customer, secret);
        String newRefreshToken = generateRefreshToken(customer, secret);
        
        return successResponse(200, Map.of(
            "accessToken", newAccessToken,
            "refreshToken", newRefreshToken,
            "expiresIn", 3600
        ));
        
    } catch (JwtException e) {
        return errorResponse(401, "Invalid refresh token");
    }
}
```

### Fase 4: Logout com Blacklist (Semana 4)

```java
// POST /auth/logout
public APIGatewayProxyResponseEvent logout(APIGatewayProxyRequestEvent input, Context context) {
    String refreshToken = extractRefreshToken(input);
    
    try {
        Claims claims = Jwts.parser()
            .setSigningKey(getSecret())
            .parseClaimsJws(refreshToken)
            .getBody();
        
        // Add to blacklist in DynamoDB
        addToBlacklist(claims.getId(), claims.getExpiration());
        
        return successResponse(200, Map.of("message", "Logout successful"));
        
    } catch (JwtException e) {
        return errorResponse(400, "Invalid token");
    }
}

private void addToBlacklist(String jti, Date expiration) {
    dynamoDbClient.putItem(PutItemRequest.builder()
        .tableName("token_blacklist")
        .item(Map.of(
            "jti", AttributeValue.builder().s(jti).build(),
            "expiration", AttributeValue.builder().n(String.valueOf(expiration.getTime())).build()
        ))
        .build());
}
```

---

## 📈 Métricas de Sucesso

| Métrica | Target | Como Medir |
|---------|--------|------------|
| **Token Generation Latency** | <100ms | Lambda duration |
| **Token Validation Latency** | <10ms | Custom authorizer duration |
| **Auth Success Rate** | >99% | Lambda success count |
| **Token Compromise Incidents** | 0 | Security logs |
| **Secret Rotation Failures** | 0 | Secrets Manager metrics |

---

## 🔄 Roadmap Futuro

### Melhorias Planejadas (6-12 meses)

1. **RS256 (Asymmetric)**
   - Trocar HS256 (symmetric) por RS256 (asymmetric)
   - Chave privada para assinar (Lambda)
   - Chave pública para validar (API Gateway, Spring Boot)
   - Rotação mais segura

2. **MFA (Multi-Factor Authentication)**
   - SMS ou TOTP para mecânicos (admin)
   - Apenas para operações críticas (aprovação de orçamento)

3. **Rate Limiting por Usuário**
   - DynamoDB com TTL para contadores
   - Limitar tentativas de login (5/minuto)

---

## 📚 Referências

- [RFC 7519 - JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)
- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [AWS API Gateway Custom Authorizers](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-use-lambda-authorizer.html)
- [jwt.io - JWT Debugger](https://jwt.io/)

---

**Aprovado por**: Equipe de Arquitetura, Security Lead  
**Data de Aprovação**: 2025-12-05  
**Próxima Revisão**: 2026-06-05
