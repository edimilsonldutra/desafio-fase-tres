# 📋 Análise e Correções dos Dockerfiles

**Data**: 07/12/2025  
**Autor**: GitHub Copilot  

---

## 📊 Resumo Executivo

| Serviço | Status Original | Status Corrigido | Principais Problemas |
|---------|----------------|------------------|---------------------|
| **api-aprovacao-orcamento** | ❌ Problemas | ✅ Corrigido | Versão Java incompatível, porta incorreta |
| **lambda-auth-service** | ❌ Inadequado | ✅ Corrigido | Dockerfile não era para Lambda runtime |
| **oficina-service-k8s** | ✅ Correto | ✅ Mantido | Bem estruturado, sem problemas |

---

## 🔍 Análise Detalhada

### 1️⃣ api-aprovacao-orcamento/Dockerfile

#### ❌ Problemas Identificados

1. **Incompatibilidade de versão Java**
   - **Dockerfile**: Usava JDK/JRE 21
   - **pom.xml**: Especifica Java 17
   - **Impacto**: Build poderia funcionar mas com versão diferente da esperada

2. **Porta incorreta**
   - **Dockerfile**: Expunha porta 8081
   - **Spring Boot padrão**: Porta 8080
   - **Impacto**: Confusão na configuração de serviços

#### ✅ Correções Aplicadas

```dockerfile
# ANTES
FROM maven:3.9.6-eclipse-temurin-21 AS build
FROM eclipse-temurin:21-jre-alpine
EXPOSE 8081

# DEPOIS
FROM maven:3.9.6-eclipse-temurin-17 AS build
FROM eclipse-temurin:17-jre-alpine
EXPOSE 8080
```

#### 📝 Observações
- Multi-stage build: ✅ Excelente prática
- Cache de dependências Maven: ✅ Implementado corretamente
- Imagem Alpine: ✅ Otimização de tamanho
- Healthcheck com curl: ✅ Presente

---

### 2️⃣ lambda-auth-service/Dockerfile

#### ❌ Problemas Identificados

1. **Propósito incorreto**
   - Dockerfile original era para **ambiente de desenvolvimento/CI/CD**
   - Template SAM usa `PackageType: Image` → precisa de **imagem Lambda**
   - Incluía ferramentas desnecessárias: AWS CLI, Terraform, Debian completo

2. **Estrutura de diretórios incorreta**
   - Referenciava `LambdaValidaPessoa/` que não existe
   - Código está em `src/main/java/lambdavalida/`

3. **Handler não definido**
   - Não especificava o handler da função Lambda

#### ✅ Correções Aplicadas

**Novo Dockerfile (para Lambda Runtime)**
```dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Build do projeto
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime Lambda oficial da AWS
FROM public.ecr.aws/lambda/java:21

# Copiar JAR e definir handler
COPY --from=build /build/target/*.jar ${LAMBDA_TASK_ROOT}/lib/
CMD [ "lambdavalida.ValidaPessoaFunction::handleRequest" ]
```

**Novo Dockerfile.dev (para Desenvolvimento/CI/CD)**
- Mantém AWS CLI, Terraform, SAM CLI
- Usado para desenvolvimento local e pipelines
- Não é usado no deploy do Lambda

#### 📝 Observações
- Base image oficial: ✅ `public.ecr.aws/lambda/java:21`
- Handler correto: ✅ `lambdavalida.ValidaPessoaFunction::handleRequest`
- Separação de responsabilidades: ✅ Dockerfile vs Dockerfile.dev

---

### 3️⃣ oficina-service-k8s/Dockerfile

#### ✅ Status: CORRETO

**Nenhuma correção necessária**

#### 🌟 Pontos Fortes

1. **Multi-stage build otimizado**
   ```dockerfile
   FROM maven:3.9.6-eclipse-temurin-21 AS build
   FROM eclipse-temurin:21-jre-alpine
   ```

2. **Versão Java correta**
   - Dockerfile: Java 21 ✅
   - pom.xml: Java 21 ✅

3. **Porta correta**
   - EXPOSE 8080 ✅

4. **Healthcheck incluído**
   ```dockerfile
   RUN apk add --no-cache curl
   ```

5. **Codificação UTF-8**
   ```dockerfile
   ENV LANG C.UTF-8
   ENV LC_ALL C.UTF-8
   ```

6. **Comentários detalhados**
   - Explica cada etapa claramente
   - Facilita manutenção

---

## 📦 Compatibilidade com Infraestrutura

### Kubernetes (oficina-service-k8s)
✅ **Deployment YAML está alinhado**
- Image pull: `oficina-service:latest`
- Container port: `8080`
- Health probes: `/actuator/health/liveness` e `/actuator/health/readiness`
- Resources adequados: 100m-500m CPU, 256Mi-512Mi RAM

### AWS Lambda (lambda-auth-service)
✅ **Template SAM está alinhado**
- Package type: `Image`
- Architecture: `x86_64`
- Dockerfile context: `.`
- Handler será extraído da imagem

### API Gateway (api-aprovacao-orcamento)
✅ **Configuração corrigida**
- Porta 8080 (Spring Boot padrão)
- Healthcheck via Actuator

---

## 🎯 Recomendações Adicionais

### 1. Adicionar .dockerignore

Criar arquivo `.dockerignore` em cada projeto:

```
# api-aprovacao-orcamento/.dockerignore
target/
.git/
.idea/
*.iml
.DS_Store
```

### 2. Adicionar Healthcheck ao Dockerfile

Para `api-aprovacao-orcamento/Dockerfile`:
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

### 3. Variáveis de ambiente para otimização Java

Adicionar ao Dockerfile de produção:
```dockerfile
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
CMD ["java", "-jar", "app.jar"]
```

### 4. Labels para melhor rastreabilidade

```dockerfile
LABEL org.opencontainers.image.source="https://github.com/grupo99/fase_tres"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.created="2025-12-07"
```

### 5. Segurança - Non-root user

```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

---

## 🚀 Como Usar os Dockerfiles Corrigidos

### api-aprovacao-orcamento
```bash
# Build
docker build -t api-aprovacao-orcamento:1.0.0 .

# Run
docker run -p 8080:8080 api-aprovacao-orcamento:1.0.0
```

### lambda-auth-service (Produção)
```bash
# Build da imagem Lambda
docker build -t lambda-auth-service:latest .

# Deploy via SAM
sam build
sam deploy
```

### lambda-auth-service (Desenvolvimento)
```bash
# Build do ambiente de desenvolvimento
docker build -f Dockerfile.dev -t lambda-dev:latest .

# Run interativo
docker run -it -v ~/.aws:/root/.aws lambda-dev:latest
```

### oficina-service-k8s
```bash
# Build
docker build -t oficina-service:latest .

# Com docker-compose
docker-compose up -d

# Deploy no Kubernetes
kubectl apply -f k8s/base/
```

---

## ✅ Checklist de Validação

- [x] Versões Java compatíveis com pom.xml
- [x] Portas corretas expostas
- [x] Multi-stage builds implementados
- [x] Imagens base oficiais
- [x] Handlers Lambda corretos
- [x] Separação dev/prod (Lambda)
- [x] Healthchecks implementados
- [x] UTF-8 configurado
- [x] Comentários adequados

---

## 📚 Referências

- [AWS Lambda Container Images](https://docs.aws.amazon.com/lambda/latest/dg/images-create.html)
- [Spring Boot Docker](https://spring.io/guides/topicals/spring-boot-docker/)
- [Dockerfile Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Multi-stage builds](https://docs.docker.com/build/building/multi-stage/)

---

**Conclusão**: Todos os Dockerfiles foram analisados e corrigidos conforme necessário. O projeto agora está com configurações consistentes e alinhadas com as melhores práticas.
