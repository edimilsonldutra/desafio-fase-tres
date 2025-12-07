# 🔐 Lambda Auth Service

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![AWS](https://img.shields.io/badge/AWS-Lambda-orange.svg)
![Java](https://img.shields.io/badge/Java-21-red.svg)
![SAM](https://img.shields.io/badge/SAM-CLI-yellow.svg)

**Repositório**: Função Serverless para Autenticação e Autorização

## 📖 Descrição

Serviço de autenticação serverless que valida CPF de clientes no banco de dados RDS PostgreSQL e gera tokens JWT (JSON Web Token) para acesso autorizado à API principal do sistema de oficina mecânica. A função é invocada via API Gateway e executa em menos de 300ms (warm start).

### 🎯 Propósito

- ✅ Validar autenticidade de clientes por CPF
- ✅ Gerar tokens JWT com claims customizados
- ✅ Integração segura com RDS via VPC
- ✅ Escalabilidade automática sem gerenciamento de servidor
- ✅ Cold start otimizado com SnapStart (Java 21)

---

## 📋 Índice

- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação Local](#-instalação-local)
- [Deploy](#-deploy)
- [Testes](#-testes)
- [CI/CD](#-cicd)
- [API Reference](#-api-reference)
- [Monitoramento](#-monitoramento)
- [Links de Deploy](#-links-de-deploy)
- [Contribuindo](#-contribuindo)

---

## 🏗️ Arquitetura

### Diagrama de Componentes

```
┌──────────────────────────────────────────────────────────────────────┐
│                         AWS Cloud - VPC                               │
│                                                                       │
│  ┌─────────────────┐     ┌────────────────────────────────────┐    │
│  │   API Gateway   │     │      Lambda Auth Service           │    │
│  │   (REST API)    │────▶│  ┌──────────────────────────────┐  │    │
│  │                 │     │  │  Handler                     │  │    │
│  │  POST /auth     │     │  │  - Validate CPF              │  │    │
│  │  {cpf: "..."}   │     │  │  - Query RDS                 │  │    │
│  └────────┬────────┘     │  │  - Generate JWT              │  │    │
│           │              │  └────────┬─────────────────────┘  │    │
│           │              │           │                         │    │
│  ┌────────▼─────────┐    │  ┌────────▼──────────┐  ┌────────▼────┐│
│  │  CloudWatch      │    │  │  VPC Integration  │  │  Secrets    ││
│  │  Logs + Metrics  │    │  │  (ENI)            │  │  Manager    ││
│  └──────────────────┘    │  └────────┬──────────┘  └─────────────┘│
│                          └───────────┼──────────────────────────────┘
│                                      │                               │
│                          ┌───────────▼──────────┐                   │
│                          │   RDS PostgreSQL     │                   │
│                          │   Multi-AZ           │                   │
│                          │   Encrypted          │                   │
│                          └──────────────────────┘                   │
└──────────────────────────────────────────────────────────────────────┘
```

### Fluxo de Autenticação

```
1. Cliente ────POST /auth {cpf}────▶ API Gateway
                                          │
2. API Gateway ─────Invoke─────────▶ Lambda Auth
                                          │
3. Lambda Auth ──Get DB Password──▶ Secrets Manager
                                          │
4. Lambda Auth ─SELECT * FROM clientes─▶ RDS PostgreSQL
                  WHERE cpf = ?           │
                                          ▼
5. Lambda Auth ◀──Cliente encontrado──┘ RDS
                  {id, nome, email}
                          │
6. Lambda Auth ──Generate JWT Token──▶ JWT Library
                  (claims: id, cpf, exp)
                          │
7. API Gateway ◀──────200 OK─────────┘ Lambda
                  {token: "eyJ..."}
                          │
8. Cliente ◀─────{token}─────────────┘ API Gateway
```

### Diagrama Específico deste Repositório

```
┌─────────────────────────────────────────────────────────────┐
│         lambda-auth-service (Este Repositório)              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📂 src/main/java/                                          │
│     └── com/oficina/auth/                                   │
│         ├── AuthHandler.java      ← Handler principal      │
│         ├── JwtService.java       ← Geração de JWT         │
│         ├── DatabaseService.java  ← Conexão com RDS        │
│         └── model/                                          │
│             ├── AuthRequest.java                            │
│             └── AuthResponse.java                           │
│                                                             │
│  📄 template.yaml                 ← SAM Template (IaC)      │
│     ├── Lambda Function                                     │
│     ├── IAM Roles                                           │
│     ├── VPC Configuration                                   │
│     └── CloudWatch Logs                                     │
│                                                             │
│  📄 samconfig.toml                ← Configuração deploy     │
│     ├── dev environment                                     │
│     ├── staging environment                                 │
│     └── prod environment                                    │
│                                                             │
│  ⚙️ .github/workflows/deploy.yml  ← CI/CD Pipeline         │
│     ├── Build (Maven)                                       │
│     ├── Test (Unit + Integration)                           │
│     ├── Security Scan (SAST)                                │
│     └── Deploy (SAM)                                        │
│                                                             │
│  📝 pom.xml                       ← Dependências Java       │
│     ├── aws-lambda-java-core                                │
│     ├── postgresql driver                                   │
│     ├── jjwt (JWT generation)                               │
│     └── aws-sdk-secretsmanager                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
           │                    │                  │
           ▼                    ▼                  ▼
    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
    │ RDS Database │  │ API Gateway  │  │   Secrets    │
    │ (externo)    │  │ (externo)    │  │   Manager    │
    └──────────────┘  └──────────────┘  └──────────────┘
```

### Componentes AWS

| Componente | Finalidade | Configuração |
|------------|------------|--------------|
| **API Gateway** | Endpoint HTTP público | REST API, CORS habilitado |
| **Lambda Function** | Lógica de autenticação | Java 21, 512MB, 30s timeout |
| **VPC Integration** | Acesso ao RDS | ENI em subnets privadas |
| **Secrets Manager** | Credenciais RDS | Rotação automática a cada 30 dias |
| **RDS PostgreSQL** | Validação de clientes | Multi-AZ, encrypted at rest |
| **CloudWatch** | Observabilidade | Logs, métricas, alarmes |

---

## 🛠️ Tecnologias Utilizadas

### Runtime & Frameworks

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| **Java** | 21 (LTS) | Linguagem de programação |
| **AWS Lambda** | Java 21 Runtime | Execução serverless |
| **AWS SAM** | 1.108+ | Framework IaC para serverless |
| **Maven** | 3.9+ | Gerenciamento de dependências |

### Bibliotecas Principais

```xml
<!-- JWT Generation -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>

<!-- AWS SDK -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>secretsmanager</artifactId>
    <version>2.21.0</version>
</dependency>

<!-- Lambda Core -->
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-core</artifactId>
    <version>1.2.3</version>
</dependency>
```

### Infraestrutura AWS

- **AWS Lambda**: Compute serverless
- **API Gateway**: REST API management
- **RDS PostgreSQL**: Database (gerenciado por `infra-database-terraform`)
- **Secrets Manager**: Gerenciamento de credenciais
- **CloudWatch**: Logs, métricas e alarmes
- **VPC**: Isolamento de rede
- **CloudFormation**: Deployment via SAM

### DevOps

- **GitHub Actions**: CI/CD pipeline
- **AWS SAM CLI**: Local testing e deployment
- **Docker**: Containerização para testes locais
- **SonarQube** (opcional): Análise de código

---

## 🔧 Pré-requisitos

### Ferramentas Necessárias

```bash
# AWS CLI
aws --version
# aws-cli/2.15.0 ou superior

# SAM CLI
sam --version
# SAM CLI, version 1.108.0 ou superior

# Java JDK
java -version
# openjdk version "21.0.1" ou superior

# Maven
mvn -version
# Apache Maven 3.9.6 ou superior

# Docker (para testes locais)
docker --version
# Docker version 24.0.0 ou superior
```

### Conta AWS

- ✅ AWS Account ativo
- ✅ IAM User com permissões:
  - `AWSLambda_FullAccess`
  - `AmazonAPIGatewayAdministrator`
  - `CloudFormationFullAccess`
  - `IAMFullAccess`
  - `SecretsManagerReadWrite`
- ✅ AWS CLI configurado (`aws configure`)

### Dependências Externas

Este repositório depende de:

1. **infra-database-terraform** (deve ser deployado primeiro)
   - RDS PostgreSQL endpoint
   - Database credentials no Secrets Manager
   - VPC ID e Subnet IDs
   - Security Group para Lambda

---

## 📦 Instalação Local

### 1. Clone o Repositório

```bash
git clone https://github.com/your-org/lambda-auth-service.git
cd lambda-auth-service
```

### 2. Instale Dependências

```bash
# Build do projeto Maven
mvn clean install

# Validar template SAM
sam validate --lint
```

### 3. Configure Variáveis de Ambiente

Crie arquivo `samconfig.toml` (se não existir):

```toml
version = 0.1

[dev]
[dev.deploy]
[dev.deploy.parameters]
stack_name = "lambda-auth-service-dev"
s3_bucket = "sam-deployments-dev"
s3_prefix = "lambda-auth-service"
region = "us-east-1"
capabilities = "CAPABILITY_IAM"
parameter_overrides = [
  "Environment=dev",
  "VpcId=vpc-0a1b2c3d4e5f6g7h8",              # ← Obter do terraform output
  "SubnetIds=subnet-xxx,subnet-yyy",           # ← Private subnets do VPC
  "SecurityGroupIds=sg-lambda-auth",           # ← Permitir conexão ao RDS
  "DBSecretArn=arn:aws:secretsmanager:us-east-1:123456789012:secret:rds/oficina-XXX"
]

[staging]
[staging.deploy]
[staging.deploy.parameters]
stack_name = "lambda-auth-service-staging"
s3_bucket = "sam-deployments-staging"
region = "us-east-1"
parameter_overrides = [
  "Environment=staging",
  "VpcId=vpc-staging",
  "SubnetIds=subnet-staging-a,subnet-staging-b",
  "SecurityGroupIds=sg-lambda-staging",
  "DBSecretArn=arn:aws:secretsmanager:...:secret:rds-staging-XXX"
]

[prod]
[prod.deploy]
[prod.deploy.parameters]
stack_name = "lambda-auth-service-prod"
s3_bucket = "sam-deployments-prod"
region = "us-east-1"
parameter_overrides = [
  "Environment=prod",
  "VpcId=vpc-prod",
  "SubnetIds=subnet-prod-a,subnet-prod-b,subnet-prod-c",
  "SecurityGroupIds=sg-lambda-prod",
  "DBSecretArn=arn:aws:secretsmanager:...:secret:rds-prod-XXX"
]
```

### 4. Build Local

```bash
# Build da função Lambda
sam build

# Verificar artefatos
ls -la .aws-sam/build/AuthFunction/
```

### 5. Testes Locais (Opcional)

```bash
# Iniciar Lambda localmente
sam local start-api

# Testar endpoint
curl -X POST http://localhost:3000/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678901"}'
```

---

## 🚀 Deploy

### Passos para Execução e Deploy

#### Ordem de Deploy (Importante!)

```
1. infra-database-terraform  ✅ (RDS deve existir primeiro)
2. lambda-auth-service       ← VOCÊ ESTÁ AQUI
3. infra-kubernetes-terraform
4. oficina-service-k8s
```

#### Deploy Manual

**Ambiente de Desenvolvimento:**

```bash
# 1. Build
sam build

# 2. Deploy
sam deploy --config-env dev

# 3. Testar
curl -X POST https://<api-id>.execute-api.us-east-1.amazonaws.com/dev/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678901"}'
```

**Staging:**

```bash
sam deploy --config-env staging
```

**Produção:**

```bash
# Produção requer confirmação
sam deploy --config-env prod --no-confirm-changeset
```

---

### 🏗️ Deploy com Terraform (Alternativa Completa)

Este projeto também inclui infraestrutura Terraform completa em `infra/terraform/` seguindo **AWS Well-Architected Framework**.

#### Diferença SAM vs Terraform

| Aspecto | SAM (template.yaml) | Terraform (infra/terraform/) |
|---------|---------------------|------------------------------|
| **Escopo** | Lambda + API Gateway básico | Infraestrutura completa (VPC, RDS, KMS, etc) |
| **Complexidade** | Simples, foco em serverless | Completo, production-ready |
| **Segurança** | Básica | Avançada (KMS, VPC Endpoints, Flow Logs) |
| **Monitoramento** | CloudWatch básico | 13 alarmes + SNS + dashboards |
| **Custos** | Menor (sem VPC própria) | Maior (VPC, NAT, endpoints) |
| **Recomendado para** | Dev/Staging | Produção Enterprise |

#### Deploy com Terraform

```bash
# 1. Navegar para diretório Terraform
cd infra/terraform/

# 2. Configurar variáveis
cp terraform.tfvars.example terraform.tfvars
# Editar terraform.tfvars com seus valores

# 3. Build da aplicação Java
cd ../..
mvn clean package
cd infra/terraform/

# 4. Inicializar Terraform
terraform init

# 5. Planejar deploy
terraform plan

# 6. Aplicar infraestrutura
terraform apply

# 7. Obter outputs
terraform output api_gateway_url
terraform output lambda_function_name
```

**Recursos criados pelo Terraform:**

✅ **Networking**: VPC Multi-AZ, Subnets, NAT, VPC Endpoints, Flow Logs  
✅ **Compute**: Lambda + DLQ + Reserved Concurrency + Insights  
✅ **Database**: RDS PostgreSQL Multi-AZ + Enhanced Monitoring + Performance Insights  
✅ **Security**: KMS CMK, Secrets Manager + Rotation, IAM Least Privilege  
✅ **Monitoring**: 13 CloudWatch Alarms + SNS Email Notifications  
✅ **API**: API Gateway + Request Validation + Caching + Throttling  

📖 **Documentação completa:** Ver `infra/terraform/README.md`

---

#### Deploy Automatizado via CI/CD

O pipeline GitHub Actions deploya automaticamente:
````

| Branch | Ambiente | Trigger | Aprovação |
|--------|----------|---------|-----------|
| `develop` | **Dev** | Push automático | Não |
| `staging` | **Staging** | Push automático | Não |
| `main` | **Produção** | Push após merge PR | **Sim** (manual) |

**Exemplo de Workflow:**

```bash
# 1. Criar feature branch
git checkout -b feature/new-validation

# 2. Fazer alterações
vim src/main/java/com/oficina/auth/AuthHandler.java

# 3. Commit e push
git add .
git commit -m "feat: adicionar validação de email"
git push origin feature/new-validation

# 4. Criar Pull Request para develop
# (GitHub UI ou gh CLI)

# 5. Após merge em develop → Deploy automático em DEV

# 6. Criar PR de develop → staging
# Deploy automático em STAGING

# 7. Criar PR de staging → main
# Deploy em PRODUÇÃO após aprovação manual
```

#### Verificar Deploy

```bash
# Listar stacks
aws cloudformation list-stacks --stack-status-filter CREATE_COMPLETE UPDATE_COMPLETE

# Descrever função Lambda
aws lambda get-function --function-name lambda-auth-service-dev

# Ver logs recentes
sam logs -n AuthFunction --stack-name lambda-auth-service-dev --tail
```

#### Rollback

```bash
# CloudFormation rollback automático em caso de falha

# Rollback manual para versão anterior
aws cloudformation update-stack \
  --stack-name lambda-auth-service-prod \
  --use-previous-template
```

---

## 🧪 Testes

### Estrutura de Testes

```
src/
├── main/java/com/oficina/auth/
│   └── AuthHandler.java
└── test/java/com/oficina/auth/
    ├── AuthHandlerTest.java          # Testes unitários
    ├── JwtServiceTest.java           # Testes de JWT
    └── integration/
        └── AuthIntegrationTest.java  # Testes de integração
```

### Testes Unitários

```bash
# Executar todos os testes
mvn test

# Executar testes específicos
mvn test -Dtest=AuthHandlerTest

# Com cobertura de código
mvn clean test jacoco:report

# Ver relatório de cobertura
open target/site/jacoco/index.html
```

### Testes de Integração Locais

```bash
# 1. Iniciar API local
sam local start-api --env-vars env.json

# 2. Testar autenticação válida
curl -X POST http://localhost:3000/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678901"}'

# Resposta esperada:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "expiresIn": 3600
# }

# 3. Testar CPF inválido
curl -X POST http://localhost:3000/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf": "00000000000"}'

# Resposta esperada:
# {
#   "error": "CPF não encontrado",
#   "statusCode": 404
# }
```

---

## 🔄 CI/CD

### Pipeline GitHub Actions

Arquivo: `.github/workflows/deploy.yml`

#### Stages do Pipeline

```
┌─────────────┐    ┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│   Checkout  │───▶│    Build    │───▶│     Test     │───▶│   Deploy    │
│   Code      │    │   (Maven)   │    │  (Unit+Int)  │    │   (SAM)     │
└─────────────┘    └─────────────┘    └──────────────┘    └─────────────┘
                                              │
                                              ▼
                                       ┌──────────────┐
                                       │   Security   │
                                       │     Scan     │
                                       └──────────────┘
```

#### Triggers

| Branch | Trigger | Deploy para | Aprovação Manual |
|--------|---------|-------------|------------------|
| `develop` | Push | **DEV** | ❌ Não |
| `staging` | Push | **STAGING** | ❌ Não |
| `main` | Push (após PR merge) | **PRODUÇÃO** | ✅ **Sim** |
| `feature/*` | Push | Nenhum (apenas CI) | - |

### Secrets do GitHub

Configure em **Settings → Secrets and variables → Actions**:

| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `AWS_ACCESS_KEY_ID` | Access key da AWS | `AKIAIOSFODNN7EXAMPLE` |
| `AWS_SECRET_ACCESS_KEY` | Secret key da AWS | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` |
| `AWS_REGION` | Região AWS | `us-east-1` |
| `SAM_S3_BUCKET_DEV` | Bucket para artefatos dev | `sam-deployments-dev` |
| `SAM_S3_BUCKET_STAGING` | Bucket para artefatos staging | `sam-deployments-staging` |
| `SAM_S3_BUCKET_PROD` | Bucket para artefatos prod | `sam-deployments-prod` |

### Proteção de Branches

Configure em **Settings → Branches → Branch protection rules**:

#### Branch `main` (Produção)

- ✅ Require a pull request before merging
- ✅ Require approvals: **2**
- ✅ Require status checks to pass before merging
  - `test`
  - `security-scan`
- ✅ Require branches to be up to date before merging
- ✅ Do not allow bypassing the above settings

#### Branch `staging`

- ✅ Require a pull request before merging
- ✅ Require approvals: **1**
- ✅ Require status checks to pass before merging

#### Branch `develop`

- ✅ Require a pull request before merging
- ✅ Require status checks to pass before merging

---

## 📘 API Reference

### Link para Swagger

| Ambiente | Swagger UI | OpenAPI Spec |
|----------|------------|--------------|
| **DEV** | https://api-dev.oficina.com/swagger-ui | [openapi-dev.json](./docs/openapi-dev.json) |
| **STAGING** | https://api-staging.oficina.com/swagger-ui | [openapi-staging.json](./docs/openapi-staging.json) |
| **PROD** | https://api.oficina.com/swagger-ui | [openapi-prod.json](./docs/openapi-prod.json) |

### Endpoints

#### `POST /auth`

Autentica cliente via CPF e retorna JWT token.

**Request:**
```json
{
  "cpf": "12345678901"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "cpf": "12345678901",
    "nome": "João Silva"
  }
}
```

📄 **Documentação completa**: [SWAGGER.md](./SWAGGER.md)

---

## 📊 Monitoramento

### CloudWatch Dashboards

Acesse os dashboards por ambiente:

| Ambiente | Dashboard URL |
|----------|---------------|
| **DEV** | https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#dashboards:name=lambda-auth-dev |
| **STAGING** | https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#dashboards:name=lambda-auth-staging |
| **PROD** | https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#dashboards:name=lambda-auth-prod |

### Métricas Monitoradas

| Métrica | Descrição | Alarme |
|---------|-----------|--------|
| `Invocations` | Número de invocações | - |
| `Errors` | Taxa de erros | > 5% em 5 min |
| `Duration` | Tempo de execução | Média > 5s |
| `Throttles` | Requests rejeitados | > 10 em 1 min |
| `ConcurrentExecutions` | Execuções simultâneas | > 900 |

### Visualizar Logs

```bash
# Logs em tempo real
sam logs -n AuthFunction --stack-name lambda-auth-service-prod --tail

# Logs com filtro de erro
aws logs filter-log-events \
  --log-group-name /aws/lambda/lambda-auth-service-prod \
  --filter-pattern "ERROR" \
  --start-time $(date -u -d '1 hour ago' +%s)000
```

---

## 🔗 Links de Deploy

| Ambiente | Status | URL | CloudFormation Stack | Última Atualização |
|----------|--------|-----|---------------------|-------------------|
| **DEV** | 🟢 Ativo | https://api-dev.oficina.com/auth | [lambda-auth-service-dev](https://console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/stackinfo?stackId=lambda-auth-service-dev) | 2025-12-05 10:30 UTC |
| **STAGING** | 🟢 Ativo | https://api-staging.oficina.com/auth | [lambda-auth-service-staging](https://console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/stackinfo?stackId=lambda-auth-service-staging) | 2025-12-04 15:20 UTC |
| **PROD** | 🟢 Ativo | https://api.oficina.com/auth | [lambda-auth-service-prod](https://console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/stackinfo?stackId=lambda-auth-service-prod) | 2025-12-01 09:00 UTC |

### Health Checks

```bash
# Verificar saúde da Lambda (DEV)
curl https://api-dev.oficina.com/auth/health

# Verificar latência
time curl -X POST https://api-dev.oficina.com/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678901"}'
```

---

## 📚 Documentação Adicional

- 📖 [Arquitetura Geral](../ARQUITETURA-REFATORADA.md)
- 🚀 [Guia de Migração](../GUIA-MIGRACAO.md)
- ❓ [FAQ](../FAQ.md)
- 📘 [API Reference (Swagger)](./SWAGGER.md)
- 🏗️ [Template SAM](./template.yaml)
- ✅ [Checklist de Validação](../CHECKLIST-VALIDACAO.md)

---

## 🤝 Contribuindo

### Workflow de Contribuição

1. **Fork** o repositório
2. **Clone** seu fork localmente
   ```bash
   git clone https://github.com/your-username/lambda-auth-service.git
   ```
3. **Crie uma branch** para sua feature
   ```bash
   git checkout -b feature/minha-feature
   ```
4. **Desenvolva** e teste localmente
   ```bash
   mvn test
   sam build && sam local start-api
   ```
5. **Commit** seguindo conventional commits
   ```bash
   git commit -m "feat: adiciona validação de email"
   ```
6. **Push** para seu fork
   ```bash
   git push origin feature/minha-feature
   ```
7. **Abra Pull Request** para `develop`

### Conventional Commits

```
feat: nova funcionalidade
fix: correção de bug
docs: atualização de documentação
style: formatação de código
refactor: refatoração sem mudança de funcionalidade
test: adição ou correção de testes
chore: tarefas de manutenção
```

### Code Review

- ✅ Mínimo **1 aprovação** para merge em `develop`
- ✅ Mínimo **2 aprovações** para merge em `main`
- ✅ Todos os testes devem passar
- ✅ Cobertura de código > 80%

---

## 📝 Licença

MIT License - veja [LICENSE](LICENSE) para detalhes.

---

## 📧 Suporte

### Canais de Comunicação

- **Slack**: [#lambda-auth-service](https://workspace.slack.com/archives/lambda-auth)
- **Email**: devops@oficina.com
- **Issues**: [GitHub Issues](https://github.com/your-org/lambda-auth-service/issues)
- **On-call**: PagerDuty (apenas produção)

### SLA

| Ambiente | Uptime | Response Time | Suporte |
|----------|--------|---------------|---------|
| **DEV** | 95% | Best effort | Business hours |
| **STAGING** | 99% | < 5s p95 | Business hours |
| **PROD** | 99.9% | < 300ms p95 | 24x7 |

---

**Desenvolvido com ❤️ pelo time de DevOps**
