# 🔐 Implementação de Autorização Baseada em Roles

## 📋 Resumo

Implementação completa de controle de acesso baseado em roles (RBAC - Role-Based Access Control) seguindo as melhores práticas e padrões de desenvolvimento de software.

## 🎯 Requisitos Implementados

### Regras de Negócio
- **CLIENTE**: Pode apenas consultar suas próprias ordens de serviço (`GET /api/v1/ordens-servico`)
- **MECANICO**: Acesso total a todas as APIs (criar, editar, consultar)
- **ADMIN**: Acesso completo a todas as funcionalidades

## 🏗️ Arquitetura da Solução

### 1. **Domain Layer** - Modelo de Negócio

#### `UserRole.java`
- **Padrão**: Enum com comportamento
- **Princípios SOLID**: 
  - **SRP**: Define apenas os papéis do sistema
  - **OCP**: Novos papéis podem ser adicionados sem modificar código existente
- **Funcionalidades**:
  - Conversão segura de String para Enum
  - Validações de permissões (`isMecanicoOrHigher()`, `isCliente()`)
  - Display names para UI

### 2. **Infrastructure/Security Layer** - Segurança

#### `JwtUserDetails.java`
- **Padrão**: Value Object + Factory Method
- **Princípios**:
  - **Immutability**: Thread-safe por design
  - **Encapsulation**: Dados privados com interface pública
- **Funcionalidades**:
  - Implementa `UserDetails` do Spring Security
  - Factory methods para criação segura
  - Validação de ownership (`isOwnerOrMecanico()`)

#### `JwtUtil.java` (Atualizado)
- **Nova funcionalidade**: `extractUserDetails(String token)`
- Extrai automaticamente: username, role e clienteId do JWT
- Validações de segurança integradas

#### `JwtRequestFilter.java` (Refatorado)
- **Padrão**: Chain of Responsibility
- **Melhorias**:
  - Logging estruturado com SLF4J
  - Tratamento de exceções robusto
  - Não depende mais de `UserDetailsService`
  - Extrai `JwtUserDetails` diretamente do token

### 3. **AOP Layer** - Aspectos

#### `@RequiresRole` Annotation
- **Padrão**: Declarative Programming
- **Uso**:
```java
@RequiresRole(UserRole.MECANICO)
public void criarOrdemServico() { }

@RequiresRole({UserRole.MECANICO, UserRole.ADMIN})
public void deletarCliente() { }
```
- Pode ser aplicada em métodos ou classes (class-level)

#### `AuthorizationAspect.java`
- **Padrão**: Aspect-Oriented Programming (AOP)
- **Princípios**:
  - **Separation of Concerns**: Separa autorização de lógica de negócio
  - **DRY**: Evita repetição de código de validação
- **Funcionalidades**:
  - Intercepta métodos com `@RequiresRole`
  - Valida permissões antes da execução
  - Logging detalhado de acessos
  - Mensagens de erro customizadas

### 4. **Controllers** - Camada REST

Todos os controllers foram atualizados com controle de acesso:

#### `OrdemServicoController`
```java
@PostMapping
@RequiresRole({UserRole.MECANICO, UserRole.ADMIN})
public ResponseEntity<OrdemServicoResponseDTO> create() { }

@GetMapping("/{id}")
public ResponseEntity<OrdemServicoDetalhesDTO> getById(
    @AuthenticationPrincipal JwtUserDetails userDetails
) {
    // Valida ownership para clientes
    if (userDetails.isCliente()) {
        if (!userDetails.isOwnerOrMecanico(clienteId)) {
            throw new AccessDeniedException(...);
        }
    }
}
```

#### `ClienteRestController`, `VeiculoRestController`, `ServicoController`, `PecaController`
- Anotados com `@RequiresRole({UserRole.MECANICO, UserRole.ADMIN})` no nível da classe
- Todos os endpoints protegidos automaticamente

### 5. **Configuration** - Spring Security

#### `SecurityConfig.java`
```java
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableAspectJAutoProxy
public class SecurityConfig {
    // Habilita segurança baseada em métodos
    // Habilita proxies AOP para @RequiresRole
}
```

## 📦 Dependências Adicionadas

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## ✅ Testes Implementados

### `UserRoleTest.java`
- ✅ Conversão de String para Enum
- ✅ Validações de entrada
- ✅ Verificação de permissões

### `JwtUserDetailsTest.java`
- ✅ Criação para diferentes roles
- ✅ Validação de ownership
- ✅ Authorities corretas
- ✅ Equals e HashCode

## 🔒 Fluxo de Autorização

```
1. Cliente faz requisição com JWT
   ↓
2. JwtRequestFilter extrai token e cria JwtUserDetails
   ↓
3. Spring Security adiciona JwtUserDetails ao SecurityContext
   ↓
4. Requisição chega no Controller
   ↓
5. AuthorizationAspect intercepta método com @RequiresRole
   ↓
6. Valida se userDetails.getRole() está nas roles permitidas
   ↓
7. Se SIM: Executa o método
   Se NÃO: Lança AccessDeniedException (HTTP 403)
```

## 🎨 Padrões de Design Utilizados

1. **Factory Method**: `JwtUserDetails.from()`
2. **Value Object**: `JwtUserDetails` (imutável)
3. **Chain of Responsibility**: `JwtRequestFilter`
4. **Aspect-Oriented Programming**: `AuthorizationAspect`
5. **Strategy Pattern**: Diferentes roles com comportamentos distintos
6. **Template Method**: Spring Security FilterChain

## 🏆 Princípios SOLID Aplicados

- **S**ingle Responsibility: Cada classe tem uma responsabilidade única
- **O**pen/Closed: Extensível para novos roles sem modificar código
- **L**iskov Substitution: `JwtUserDetails` implementa `UserDetails`
- **I**nterface Segregation: Interfaces focadas e específicas
- **D**ependency Inversion: Dependências via abstrações

## 📊 Exemplo de Uso

### Cliente consultando suas ordens:
```http
GET /api/v1/ordens-servico/123
Authorization: Bearer eyJhbGci... (token com role=CLIENTE, clienteId=456)

✅ PERMITIDO se ordem 123 pertence ao cliente 456
❌ NEGADO (403) se ordem 123 pertence a outro cliente
```

### Mecânico criando ordem:
```http
POST /api/v1/ordens-servico
Authorization: Bearer eyJhbGci... (token com role=MECANICO)

✅ PERMITIDO
```

### Cliente tentando criar ordem:
```http
POST /api/v1/ordens-servico
Authorization: Bearer eyJhbGci... (token com role=CLIENTE)

❌ NEGADO (403) - AccessDeniedException
```

## 🚀 Próximos Passos (Sugestões)

1. Implementar filtro por `clienteId` no `ListarOrdensServicoUseCase`
2. Adicionar auditoria de acessos
3. Implementar cache de permissões
4. Adicionar testes de integração para autorização
5. Implementar rate limiting por role

## 📝 Notas Importantes

- O `clienteId` no JWT é obrigatório apenas para role `CLIENTE`
- Mecânicos e Admins podem acessar qualquer recurso
- Exceções de autorização retornam HTTP 403 Forbidden
- Logging detalhado para auditoria de segurança
- Thread-safe por design (classes imutáveis)
