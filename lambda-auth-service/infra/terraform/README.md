# Lambda Auth Service - Terraform Infrastructure

## 📋 Visão Geral

Infraestrutura Terraform para o serviço Lambda de autenticação, organizada seguindo **boas práticas** com separação de ambientes e módulos reutilizáveis.

## 🏗️ Estrutura do Projeto

```
lambda-auth-service/infra/terraform/
├── modules/
│   └── lambda-auth/              # Módulo reutilizável
│       ├── api-gateway.tf        # API Gateway REST
│       ├── vpc.tf                # VPC própria do Lambda
│       ├── rds.tf                # RDS próprio do Lambda
│       ├── lambda.tf             # Função Lambda
│       ├── iam.tf                # Roles e policies
│       ├── secrets.tf            # Secrets Manager
│       ├── cloudwatch.tf         # Logs e alarmes
│       ├── kms.tf                # Encryption keys
│       ├── sns.tf                # Notificações
│       ├── vars.tf               # Variáveis do módulo
│       ├── outputs.tf            # Outputs do módulo
│       └── ...
│
├── environments/
│   ├── dev/                      # Ambiente de desenvolvimento
│   │   ├── main.tf               # Chama o módulo lambda-auth
│   │   ├── variables.tf          # Variáveis específicas (secrets)
│   │   ├── backend.tf            # Backend local
│   │   ├── provider.tf           # Provider AWS
│   │   └── terraform.tfvars.example
│   │
│   ├── staging/                  # Ambiente de staging
│   │   ├── main.tf               # Config staging
│   │   ├── backend.tf            # Backend S3
│   │   └── ...
│   │
│   └── prod/                     # Ambiente de produção
│       ├── main.tf               # Config production
│       ├── backend.tf            # Backend S3
│       └── ...
│
└── backend.tf                    # (Antigo - ignorar)
```

## 🎯 Arquitetura

### ✅ Lambda Auth usa VPC e RDS COMPARTILHADOS

```
┌──────────────────────────────────────────────────────────┐
│         infra-kubernetes-terraform (VPC Compartilhada)    │
│                                                           │
│  VPC: 10.0.0.0/16 (dev) / 10.1.0.0/16 (staging)          │
│       10.2.0.0/16 (prod)                                  │
│                                                           │
│  ├── Public Subnets (10.x.1.0/24, 10.x.2.0/24)           │
│  ├── Private Subnets (10.x.3.0/24, 10.x.4.0/24)          │
│  └── Security Groups                                     │
└──────────────────────────────────────────────────────────┘
           ▲                              ▲
           │ Remote State                 │ Remote State
           │ (VPC)                        │ (VPC)
           │                              │
┌──────────────────────────┐   ┌─────────────────────────┐
│  lambda-auth-service     │   │ infra-database-         │
│                          │   │ terraform               │
│  ┌────────────────────┐  │   │                         │
│  │ Lambda Function    │  │   │ ┌──────────────────┐    │
│  │ - Java 21          │──┼───┼─│ RDS PostgreSQL   │    │
│  │ - VPC: Shared      │  │   │ │ - Multi-AZ       │    │
│  │ - Subnets: Shared  │  │   │ │ - Encrypted      │    │
│  └────────────────────┘  │   │ └──────────────────┘    │
│  ┌────────────────────┐  │   │                         │
│  │ API Gateway        │  │   │ Database compartilhado  │
│  │ - /auth endpoint   │  │   │ usado por:              │
│  └────────────────────┘  │   │ - Lambda Auth ✅        │
└──────────────────────────┘   │ - Oficina Service ✅    │
                                └─────────────────────────┘
```

### ✅ Por que Lambda usa infraestrutura compartilhada?

| Aspecto | Antes (Duplicado) | Agora (Compartilhado) |
|---------|-------------------|----------------------|
| **VPC** | ❌ Lambda tinha VPC própria | ✅ Usa VPC do K8s |
| **RDS** | ❌ Database separado | ✅ Database compartilhado |
| **Custo** | 💰 ~$200-300/mês extras | 💰 Economia de ~70% |
| **Dados** | ❌ Clientes duplicados | ✅ Mesma tabela `clientes` |
| **Deploy** | ❌ Independente | ✅ Depende de VPC + RDS |

**Vantagens:**
- 💰 **Economia de custo**: Uma VPC, um RDS para todo o projeto
- 🔐 **Mesmos dados**: Lambda autentica contra a mesma tabela que a aplicação usa
- 🚀 **Simplicidade**: Menos infraestrutura para gerenciar
- 📊 **Consistência**: Clientes cadastrados na aplicação podem autenticar imediatamente

## 🌍 Ambientes

### Development (dev)
- **VPC**: `10.0.0.0/16` (compartilhada com K8s)
- **RDS**: Database compartilhado `oficina_dev`
- **Backend**: Local (terraform.tfstate)
- **Remote State**: Local paths para VPC e RDS

### Staging
- **VPC**: `10.1.0.0/16` (compartilhada com K8s)
- **RDS**: Database compartilhado `oficina_staging`
- **Backend**: S3 (`lambda-auth/staging/terraform.tfstate`)
- **Remote State**: S3 paths para VPC e RDS

### Production
- **VPC**: `10.2.0.0/16` (compartilhada com K8s)
- **RDS**: Database compartilhado `oficina_prod`
- **Backend**: S3 (`lambda-auth/prod/terraform.tfstate`)
- **Remote State**: S3 paths para VPC e RDS

## 🚀 Como Usar

### 1. Desenvolvimento (dev)

```bash
cd environments/dev

# Copiar exemplo
cp terraform.tfvars.example terraform.tfvars

# Editar secrets (NUNCA COMMITAR!)
nano terraform.tfvars

# Build do JAR (importante!)
cd ../../../
mvn clean package
cd infra/terraform/environments/dev

# Deploy
terraform init
terraform plan
terraform apply

# Testar
curl -X POST $(terraform output -raw api_gateway_invoke_url)/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf":"12345678900"}'
```

### 2. Staging

```bash
cd environments/staging

# Configurar secrets
cp terraform.tfvars.example terraform.tfvars
nano terraform.tfvars

# Build
cd ../../../ && mvn clean package && cd infra/terraform/environments/staging

# Deploy (backend S3 - criar bucket antes!)
terraform init
terraform plan
terraform apply
```

### 3. Production

```bash
cd environments/prod

# Build
cd ../../../ && mvn clean package && cd infra/terraform/environments/prod

# Deploy com aprovação
terraform plan -out=tfplan
terraform apply tfplan
```

## 📦 Variáveis Obrigatórias

Cada ambiente requer `terraform.tfvars` com:

```hcl
# JWT Secret (mínimo 32 caracteres)
jwt_secret = "your-super-secret-jwt-key-minimum-32-characters-long"
```

⚠️ **NUNCA commitar `terraform.tfvars`** (já está no .gitignore)

**IMPORTANTE**: Database password é gerenciado pelo `infra-database-terraform` via Secrets Manager

## 🔧 Recursos Criados por Ambiente
## 🔧 Recursos Criados por Ambiente

Cada ambiente cria **APENAS** recursos Lambda:

| Recurso | Quantidade | Finalidade |
|---------|-----------|------------|
| **Lambda Function** | 1 | Lógica de autenticação |
| **API Gateway** | 1 | Endpoint HTTP público |
| **Security Group** | 1 | Firewall para Lambda |
| **Security Group Rule** | 1 | Acesso Lambda → RDS |
| **Secrets Manager** | 1 | JWT secret |
| **CloudWatch Log Group** | 1 | Logs do Lambda |
| **CloudWatch Alarms** | 5-10 | Monitoramento |
| **SNS Topic** | 1 | Notificações de alarmes |
| **IAM Role** | 1 | Permissões Lambda |

**Recursos NÃO criados** (vêm de remote state):
- ❌ VPC (do `infra-kubernetes-terraform`)
- ❌ Subnets (do `infra-kubernetes-terraform`)  
- ❌ RDS (do `infra-database-terraform`)
- ❌ Database credentials (do `infra-database-terraform`)
## 📤 Outputs

Após `terraform apply`:

```bash
# API Gateway URL
terraform output -raw api_gateway_invoke_url
# https://abc123.execute-api.us-east-1.amazonaws.com/dev

# Lambda ARN
terraform output lambda_function_arn

# RDS Endpoint
terraform output -raw rds_endpoint
## 🔍 Diferenças entre Ambientes

| Configuração | Dev | Staging | Prod |
|--------------|-----|---------|------|
| **VPC** | 10.0.0.0/16 (shared) | 10.1.0.0/16 (shared) | 10.2.0.0/16 (shared) |
| **RDS** | Shared DB | Shared DB | Shared DB |
| **Lambda Memory** | 512 MB | 512 MB | 1024 MB |
| **Log Retention** | 7 days | 14 days | 30 days |
| **X-Ray Tracing** | ❌ | ❌ | ✅ |
| **Backend** | Local | S3 | S3 |
| **Remote State** | Local paths | S3 paths | S3 paths |MB |
| **Backup Days** | 7 | 14 | 30 |
| **Log Retention** | 7 days | 14 days | 30 days |
| **X-Ray Tracing** | ❌ | ❌ | ✅ |
| **Backend** | Local | S3 | S3 |
| **Subnets** | 2+2 AZs | 2+2 AZs | 3+3 AZs |

## ⚠️ Dependências

### Pré-requisitos

1. **Terraform** >= 1.0
2. **AWS CLI** configurado
3. **Maven** 3.9+ (build do JAR)
4. **Java** 21

### Build antes do Deploy

```bash
# SEMPRE fazer build do JAR antes de terraform apply
cd lambda-auth-service
mvn clean package

# Verificar JAR
ls -lh target/lambda-auth-service-1.0.0.jar
```

### S3 Backend (staging/prod)

Criar bucket S3 primeiro:

```bash
aws s3 mb s3://fiap-oficina-terraform-state --region us-east-1

aws s3api put-bucket-versioning \
  --bucket fiap-oficina-terraform-state \
  --versioning-configuration Status=Enabled

aws dynamodb create-table \
  --table-name fiap-oficina-terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
```

## 🐛 Troubleshooting

### ❌ "Error: module not found"

```bash
# Solução: Rodar terraform init
cd environments/dev
terraform init
```

### ❌ "JAR file not found"

```bash
# Solução: Build do projeto
cd ../../../
mvn clean package
cd infra/terraform/environments/dev
terraform apply
```

### ❌ "Backend configuration changed"

```bash
# Solução: Re-inicializar backend
terraform init -reconfigure
```

### ❌ "No value for required variable"

```bash
# Solução: Criar terraform.tfvars
cp terraform.tfvars.example terraform.tfvars
nano terraform.tfvars
```

## 📚 Relação com Outros Repositórios

## 📚 Relação com Outros Repositórios

### ✅ Lambda Auth DEPENDE de:

1. **infra-kubernetes-terraform** (VPC compartilhada)
   - VPC ID
   - Private Subnet IDs
   - Deploy ANTES do Lambda

2. **infra-database-terraform** (RDS compartilhado)
   - RDS Endpoint
   - RDS Security Group ID
   - Database Name
   - Database Credentials (Secrets Manager ARN)
   - Deploy ANTES do Lambda

### 📋 Ordem de Deploy Obrigatória

```
1. infra-kubernetes-terraform  ✅ (cria VPC e subnets)
2. infra-database-terraform    ✅ (cria RDS na VPC)
3. lambda-auth-service         ← VOCÊ ESTÁ AQUI
4. oficina-service-k8s         (usa mesma VPC e RDS)
```

**Se deployer Lambda antes**, terá erro:
```
Error: No remote state found for VPC
Error: No remote state found for Database
```🔐 Segurança

- ✅ Secrets no Secrets Manager
- ✅ RDS em subnet privada
- ✅ Lambda em VPC
- ✅ Encryption at rest (KMS)
- ✅ Rotation de secrets (opcional)
- ✅ VPC Flow Logs
- ✅ CloudWatch Alarms

## 📝 Próximos Passos

1. ✅ Estrutura de ambientes criada
2. ⏳ Testar deploy em dev
3. ⏳ Configurar GitHub Actions para CI/CD
4. ⏳ Ativar New Relic monitoring (opcional)
5. ⏳ Configurar WAF no API Gateway (prod)

## 📞 Suporte

**Dúvidas?**
- Ver `lambda-auth-service/README.md` (documentação principal)
- Ver módulo: `modules/lambda-auth/README.md`

---

**Última atualização**: Dezembro 2025  
**Terraform Version**: >= 1.0  
**AWS Provider**: ~> 5.0
