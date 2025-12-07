# 🚀 Infraestrutura AWS ECS Fargate - Oficina Mecânica

[![Terraform](https://img.shields.io/badge/Terraform-1.5+-623CE4?logo=terraform)](https://www.terraform.io/)
[![AWS ECS](https://img.shields.io/badge/AWS-ECS_Fargate-FF9900?logo=amazonaws)](https://aws.amazon.com/ecs/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15.5-336791?logo=postgresql)](https://www.postgresql.org/)

Provisionamento automatizado de infraestrutura serverless usando **ECS Fargate**, **Application Load Balancer**, **RDS PostgreSQL** e serviços gerenciados AWS para o sistema de Oficina Mecânica.

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Módulos](#-módulos)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração](#-configuração)
- [Deploy](#-deploy)
- [Gerenciamento de Secrets](#-gerenciamento-de-secrets)
- [Monitoramento](#-monitoramento)
- [Auto Scaling](#-auto-scaling)
- [Troubleshooting](#-troubleshooting)
- [Custos](#-custos)
- [Manutenção](#-manutenção)

## 🎯 Visão Geral

Este repositório contém a infraestrutura como código (IaC) usando **Terraform** para provisionar uma arquitetura serverless completa na AWS usando **ECS Fargate**.

### 🔄 Migração Kubernetes → ECS Fargate

Este projeto foi **refatorado de EKS (Kubernetes) para ECS Fargate** para:
- ✅ **Simplificar o gerenciamento** - sem necessidade de gerenciar nodes ou control plane
- ✅ **Reduzir custos operacionais** - pagamento apenas pelos recursos utilizados
- ✅ **Arquitetura serverless** - escalabilidade automática sem provisionamento manual
- ✅ **Menor complexidade** - menos componentes para manter e atualizar

### Características Principais

- ✅ **ECS Fargate**: Containers serverless sem gerenciamento de infraestrutura
- ✅ **Application Load Balancer**: Distribuição de tráfego HTTP/HTTPS com health checks
- ✅ **RDS PostgreSQL 15.5**: Banco de dados gerenciado com backups automáticos
- ✅ **AWS Secrets Manager**: Gerenciamento seguro de credenciais e chaves JWT
- ✅ **Amazon ECR**: Registry privado para imagens Docker
- ✅ **VPC Multi-AZ**: 3 zonas de disponibilidade para alta disponibilidade
- ✅ **Auto Scaling**: Escalabilidade automática baseada em CPU e memória
- ✅ **CloudWatch**: Monitoramento completo com Container Insights
- ✅ **VPC Endpoints**: Comunicação privada com serviços AWS (ECR, Secrets Manager, S3)


## 🏗️ Arquitetura

### Diagrama de Infraestrutura

```
                                    Internet
                                       │
                                       │
                        ┌──────────────▼─────────────────┐
                        │    Application Load Balancer   │
                        │     (Internet-facing ALB)       │
                        │    Health Check: /actuator/    │
                        │           health               │
                        └──────────────┬─────────────────┘
                                       │
        ┌──────────────────────────────┼──────────────────────────────┐
        │                         AWS VPC                              │
        │                      (10.0.0.0/16)                           │
        │                                                              │
        │  ┌─────────────────────────────────────────────────────┐   │
        │  │           ECS Fargate Cluster                       │   │
        │  │                                                      │   │
        │  │   ┌──────────────┐  ┌──────────────┐               │   │
        │  │   │ ECS Task 1   │  │ ECS Task 2   │  ... (2-10)   │   │
        │  │   │              │  │              │               │   │
        │  │   │ Container:   │  │ Container:   │               │   │
        │  │   │ oficina-app  │  │ oficina-app  │               │   │
        │  │   │ Port: 8080   │  │ Port: 8080   │               │   │
        │  │   │              │  │              │               │   │
        │  │   │ CPU: 512     │  │ CPU: 512     │               │   │
        │  │   │ Memory: 1024 │  │ Memory: 1024 │               │   │
        │  │   └──────┬───────┘  └──────┬───────┘               │   │
        │  │          │                  │                       │   │
        │  └──────────┼──────────────────┼───────────────────────┘   │
        │             │                  │                           │
        │             └──────────┬───────┘                           │
        │                        │                                   │
        │              ┌─────────▼──────────┐                        │
        │              │  RDS PostgreSQL    │                        │
        │              │    (15.5)          │                        │
        │              │  Multi-AZ          │                        │
        │              │  Encrypted         │                        │
        │              └────────────────────┘                        │
        │                                                              │
        │  ┌──────────────────────────────────────────────────────┐  │
        │  │          AWS Secrets Manager                         │  │
        │  │  - Database credentials (auto-generated)             │  │
        │  │  - JWT secret key                                    │  │
        │  │  - Application secrets                               │  │
        │  └──────────────────────────────────────────────────────┘  │
        │                                                              │
        │  ┌──────────────────────────────────────────────────────┐  │
        │  │          Amazon ECR                                  │  │
        │  │  - Private Docker registry                           │  │
        │  │  - Vulnerability scanning                            │  │
        │  │  - Lifecycle policies                                │  │
        │  └──────────────────────────────────────────────────────┘  │
        │                                                              │
        │  ┌──────────────────────────────────────────────────────┐  │
        │  │          CloudWatch                                  │  │
        │  │  - Container Insights                                │  │
        │  │  - Application logs                                  │  │
        │  │  - Metrics & Alarms                                  │  │
        │  └──────────────────────────────────────────────────────┘  │
        │                                                              │
        └──────────────────────────────────────────────────────────────┘
```

### Componentes da Arquitetura

1. **VPC (Virtual Private Cloud)**
   - 3 Availability Zones para alta disponibilidade
   - Subnets públicas (ALB) e privadas (ECS tasks, RDS)
   - 3 NAT Gateways para acesso à internet das subnets privadas
   - VPC Endpoints para comunicação privada com ECR, Secrets Manager, S3, CloudWatch

2. **Application Load Balancer (ALB)**
   - Internet-facing para receber tráfego externo
   - Target Group com health checks em `/actuator/health`
   - Suporte a HTTP e HTTPS (certificado ACM)
   - Integração com ECS Service

3. **ECS Fargate**
   - Cluster ECS sem gerenciamento de nodes
   - Service com 2-10 tasks (auto scaling)
   - Task Definition: 512 CPU units (0.5 vCPU), 1024 MB memory
   - Logs enviados para CloudWatch
   - Secrets injetados do Secrets Manager

4. **RDS PostgreSQL**
   - Versão 15.5
   - Multi-AZ para failover automático
   - Backups automáticos (7 dias de retenção)
   - Criptografia em repouso (KMS)
   - Enhanced monitoring
   - Credenciais gerenciadas pelo Secrets Manager

5. **Amazon ECR**
   - Registry privado para imagens Docker
   - Scan de vulnerabilidades em push
   - Lifecycle policy: manter 10 imagens, remover untagged após 7 dias

6. **AWS Secrets Manager**
   - Armazenamento de credenciais do banco de dados
   - JWT secret key
   - Rotação automática de secrets (opcional)
   - Integração com ECS tasks via IAM

7. **API Gateway (Auth)**
   - Mantido da arquitetura anterior
   - Integração com Lambda de autenticação
   - RBAC (Role-Based Access Control)

## 📦 Módulos

### Estrutura de Diretórios

```
infra-kubernetes-terraform/
├── modules/
│   ├── vpc/                    # Módulo de rede (reutilizado)
│   ├── ecr/                    # Amazon ECR registry
│   ├── alb/                    # Application Load Balancer
│   ├── rds/                    # RDS PostgreSQL
│   ├── secrets/                # AWS Secrets Manager
│   ├── ecs/                    # ECS Fargate (cluster, service, tasks)
│   ├── api-gateway/            # API Gateway (mantido da arquitetura anterior)
│   └── api-aprovacao/          # API Aprovação Orçamento (Kubernetes deployment)
│
├── environments/
│   └── dev/
│       ├── main.tf             # Configuração principal
│       ├── variables.tf        # Definição de variáveis
│       ├── outputs.tf          # Outputs da infraestrutura
│       └── terraform.tfvars.example  # Exemplo de configuração
│
└── README.md
```

### Descrição dos Módulos

#### 1. VPC Module
- **Propósito**: Rede isolada com multi-AZ
- **Componentes**: Subnets públicas/privadas, NAT Gateways, Internet Gateway, VPC Endpoints
- **Configuração**: 3 AZs, CIDR 10.0.0.0/16

#### 2. ECR Module
- **Propósito**: Registry privado para imagens Docker
- **Recursos**: Repository, lifecycle policy, image scanning
- **Features**: Scan on push, manter 10 últimas imagens

#### 3. ALB Module
- **Propósito**: Balanceamento de carga HTTP/HTTPS
- **Recursos**: ALB, Target Group, Listeners
- **Health Check**: `/actuator/health` (Spring Boot Actuator)

#### 4. RDS Module
- **Propósito**: Banco de dados PostgreSQL gerenciado
- **Recursos**: DB Instance, Subnet Group, Parameter Group, Secrets Manager integration
- **Features**: Multi-AZ, backups automáticos, enhanced monitoring

#### 5. Secrets Manager Module
- **Propósito**: Gerenciamento seguro de secrets
- **Recursos**: Secrets, versões, rotação (opcional)
- **Uso**: Credenciais DB, JWT secret, app secrets

#### 6. ECS Module
- **Propósito**: Orquestração de containers serverless
- **Recursos**: ECS Cluster, Service, Task Definition, IAM Roles, Auto Scaling
- **Features**: CloudWatch Container Insights, auto scaling baseado em CPU/Memory

#### 7. API Aprovação Module
- **Propósito**: Deploy da API de Aprovação de Orçamento no Kubernetes/EKS
- **Recursos**: Deployment, Service, HPA, ConfigMap, Ingress (opcional)
- **Features**: Auto scaling, health checks via Actuator, métricas Prometheus

## 🔧 Pré-requisitos

### Ferramentas Necessárias

- **Terraform** >= 1.5.0
- **AWS CLI** >= 2.0
- **Docker** >= 20.10
- **Conta AWS** com permissões apropriadas

### Permissões AWS Necessárias

O usuário/role do Terraform precisa das seguintes permissões:
- `AmazonEC2FullAccess`
- `AmazonECSFullAccess`
- `AmazonRDSFullAccess`
- `AmazonVPCFullAccess`
- `SecretsManagerReadWrite`
- `ElasticLoadBalancingFullAccess`
- `AmazonEC2ContainerRegistryFullAccess`
- `CloudWatchFullAccess`
- `IAMFullAccess` (para criar roles de serviço)

## ⚙️ Configuração

### 1. Clonar o Repositório

```bash
git clone https://github.com/seu-usuario/infra-kubernetes-terraform.git
cd infra-kubernetes-terraform/environments/dev
```

### 2. Configurar Variáveis

Copie o arquivo de exemplo e edite com seus valores:

```bash
cp terraform.tfvars.example terraform.tfvars
```

Edite `terraform.tfvars`:

```hcl
aws_region = "us-east-1"

# VPC Configuration
vpc_cidr             = "10.0.0.0/16"
availability_zones   = ["us-east-1a", "us-east-1b", "us-east-1c"]
environment          = "dev"

# ECR Configuration
ecr_repository_name = "oficina-mecânica"

# Container Configuration
container_name  = "oficina-app"
container_image = "<ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/oficina-mecânica:latest"
container_port  = 8080

# ECS Task Resources
task_cpu    = 512   # 0.5 vCPU
task_memory = 1024  # 1 GB

# ECS Service
desired_count = 2

# Auto Scaling
min_capacity = 2
max_capacity = 10

# RDS Configuration
db_instance_class    = "db.t3.micro"
db_allocated_storage = 20
db_engine_version    = "15.5"
db_name              = "oficina_db"
db_username          = "admin"
multi_az             = false  # true para produção

# JWT Secret (ALTERAR!)
jwt_secret_key = "your-super-secret-jwt-key-change-me"
```

### 3. Autenticar na AWS

```bash
aws configure
# ou
export AWS_PROFILE=seu-perfil
```

## 🚀 Deploy

### Passo 1: Inicializar Terraform

```bash
cd environments/dev
terraform init
```

### Passo 2: Validar Configuração

```bash
terraform validate
terraform plan
```

### Passo 3: Aplicar Infraestrutura

```bash
terraform apply
```

Revise as mudanças e confirme digitando `yes`.

### Passo 4: Build e Push da Imagem Docker

Após criar o ECR repository:

```bash
# Obter URL do ECR (output do Terraform)
ECR_URL=$(terraform output -raw ecr_repository_url)

# Login no ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_URL

# Build da imagem (na raiz do projeto Spring Boot)
cd ../../oficina-service-k8s
docker build -t oficina-mecânica:latest .

# Tag da imagem
docker tag oficina-mecânica:latest $ECR_URL:latest

# Push para ECR
docker push $ECR_URL:latest
```

### Passo 5: Atualizar ECS Service

O ECS Service detectará a nova imagem e atualizará as tasks automaticamente. Para forçar um novo deployment:

```bash
aws ecs update-service \
  --cluster oficina-dev-cluster \
  --service oficina-dev-service \
  --force-new-deployment
```

### Passo 6: Verificar Deployment

```bash
# Obter URL do ALB
terraform output application_url

# Testar health check
curl http://<ALB_DNS>/actuator/health
```


## 🔐 Gerenciamento de Secrets

### Secrets Criados Automaticamente

O Terraform cria os seguintes secrets no AWS Secrets Manager:

1. **Database Credentials** (`oficina-dev-db-credentials`)
   - Gerado automaticamente pelo RDS module
   - Contém: username, password, host, port, dbname

2. **JWT Secret** (`oficina-dev-jwt-secret`)
   - Configurado via variável `jwt_secret_key`
   - Usado para autenticação JWT

3. **Application Secrets** (`oficina-dev-app-secrets`)
   - Mapeamento dos secrets do banco de dados
   - Injetado nas tasks ECS via environment variables

### Acessar Secrets

```bash
# Listar todos os secrets
aws secretsmanager list-secrets

# Obter credenciais do banco de dados
aws secretsmanager get-secret-value \
  --secret-id oficina-dev-db-credentials \
  --query SecretString --output text | jq .

# Obter JWT secret
aws secretsmanager get-secret-value \
  --secret-id oficina-dev-jwt-secret \
  --query SecretString --output text
```

### Atualizar Secrets

```bash
# Atualizar JWT secret
aws secretsmanager update-secret \
  --secret-id oficina-dev-jwt-secret \
  --secret-string "new-super-secret-key"

# Forçar novo deployment para aplicar mudança
aws ecs update-service \
  --cluster oficina-dev-cluster \
  --service oficina-dev-service \
  --force-new-deployment
```

## 📊 Monitoramento

### CloudWatch Container Insights

Habilitado automaticamente no cluster ECS. Acesse via Console AWS:
- **CloudWatch** → **Container Insights** → **Performance monitoring**

Métricas disponíveis:
- CPU utilization (cluster, service, task)
- Memory utilization
- Network I/O
- Task count
- Service restarts

### Logs de Aplicação

Todos os logs da aplicação são enviados para CloudWatch Logs:

```bash
# Visualizar logs do serviço
aws logs tail /ecs/oficina-dev --follow

# Filtrar por erro
aws logs tail /ecs/oficina-dev --follow --filter-pattern "ERROR"

# Últimas 100 linhas
aws logs tail /ecs/oficina-dev --since 1h
```

### CloudWatch Alarms

Alarms configurados automaticamente:

1. **High CPU** - Dispara quando CPU > 80% por 2 minutos consecutivos
2. **High Memory** - Dispara quando Memory > 80% por 2 minutos consecutivos
3. **Unhealthy Targets** - Dispara quando ALB health check falha

Visualizar alarms:

```bash
aws cloudwatch describe-alarms --alarm-names \
  oficina-dev-cpu-high \
  oficina-dev-memory-high \
  oficina-dev-unhealthy-targets
```

### Dashboards

Acesse dashboards pré-configurados:
- **Console AWS** → **CloudWatch** → **Dashboards** → `oficina-dev-dashboard`

Métricas incluídas:
- Request count (ALB)
- Target response time
- HTTP 4xx/5xx errors
- ECS CPU/Memory utilization
- RDS connections

## ⚡ Auto Scaling

### Configuração de Auto Scaling

O ECS Service escala automaticamente entre 2 e 10 tasks baseado em:

**Target Tracking - CPU Utilization**
- Métrica: `ECSServiceAverageCPUUtilization`
- Target: 70%
- Scale out quando CPU > 70%
- Scale in quando CPU < 70%

**Target Tracking - Memory Utilization**
- Métrica: `ECSServiceAverageMemoryUtilization`
- Target: 80%
- Scale out quando Memory > 80%
- Scale in quando Memory < 80%

### Ajustar Limites de Scaling

Edite `terraform.tfvars`:

```hcl
min_capacity = 3  # Mínimo de tasks
max_capacity = 20 # Máximo de tasks
```

Aplique as mudanças:

```bash
terraform apply
```

### Monitorar Scaling

```bash
# Verificar atividades de scaling
aws application-autoscaling describe-scaling-activities \
  --service-namespace ecs \
  --resource-id service/oficina-dev-cluster/oficina-dev-service

# Ver número atual de tasks
aws ecs describe-services \
  --cluster oficina-dev-cluster \
  --services oficina-dev-service \
  --query 'services[0].runningCount'
```

## 🐛 Troubleshooting

### Verificar Status do Service

```bash
aws ecs describe-services \
  --cluster oficina-dev-cluster \
  --services oficina-dev-service
```

### Logs de Deployment

```bash
# Últimos eventos do service
aws ecs describe-services \
  --cluster oficina-dev-cluster \
  --services oficina-dev-service \
  --query 'services[0].events[:10]'
```

### Tasks Não Iniciam

**Possíveis causas:**

1. **Imagem não encontrada no ECR**
   ```bash
   aws ecr describe-images \
     --repository-name oficina-mecânica \
     --query 'imageDetails[*].imageTags'
   ```

2. **Secrets Manager inacessível**
   - Verifique IAM role da task execution role
   - Confirme que VPC endpoints estão configurados

3. **Health check falhando**
   ```bash
   # Testar health check localmente
   docker run -p 8080:8080 <ECR_URL>:latest
   curl http://localhost:8080/actuator/health
   ```

### Acessar Container em Execução

Habilite ECS Exec (já configurado no module):

```bash
# Listar tasks em execução
aws ecs list-tasks \
  --cluster oficina-dev-cluster \
  --service-name oficina-dev-service

# Conectar ao container
aws ecs execute-command \
  --cluster oficina-dev-cluster \
  --task <TASK_ID> \
  --container oficina-app \
  --interactive \
  --command "/bin/sh"
```

### RDS Connection Issues

```bash
# Testar conexão ao RDS
DB_ENDPOINT=$(terraform output -raw rds_endpoint)
psql -h $DB_ENDPOINT -U admin -d oficina_db

# Verificar security groups
aws ec2 describe-security-groups \
  --group-ids <RDS_SG_ID>
```

### ALB Health Check Failures

```bash
# Ver target health
aws elbv2 describe-target-health \
  --target-group-arn <TARGET_GROUP_ARN>

# Logs do ALB
aws logs tail /aws/elasticloadbalancing/app/oficina-dev-alb --follow
```

## 💰 Custos

### Estimativa Mensal (Ambiente Dev)

| Serviço | Configuração | Custo Estimado (us-east-1) |
|---------|-------------|---------------------------|
| **ECS Fargate** | 2 tasks x 0.5 vCPU, 1GB | ~$30/mês |
| **Application Load Balancer** | 1 ALB | ~$16/mês |
| **RDS PostgreSQL** | db.t3.micro, 20GB | ~$15/mês |
| **NAT Gateway** | 3 NAT Gateways | ~$97/mês |
| **VPC Endpoints** | 4 endpoints | ~$30/mês |
| **Secrets Manager** | 3 secrets | ~$1.20/mês |
| **ECR** | 10GB storage | ~$1/mês |
| **CloudWatch Logs** | 5GB/mês | ~$2.50/mês |
| **Data Transfer** | 10GB out | ~$0.90/mês |
| **TOTAL** | | **~$175-200/mês** |

### Otimizações de Custo

**Para Produção:**
- Usar Reserved Instances para RDS (economize até 60%)
- Considerar Savings Plans para ECS Fargate
- Habilitar S3 VPC Endpoint (gratuito, reduz NAT Gateway usage)

**Para Dev/Test:**
- Reduzir número de NAT Gateways (usar 1 ao invés de 3): **economize ~$65/mês**
- Desabilitar Multi-AZ no RDS: **economize ~$15/mês**
- Usar db.t4g.micro (ARM): **economize ~$5/mês**

Edite `terraform.tfvars` para ambiente dev:

```hcl
# Usar apenas 1 NAT Gateway
single_nat_gateway = true

# Desabilitar Multi-AZ no RDS
multi_az = false

# Usar instância menor
db_instance_class = "db.t4g.micro"
```

**Custo estimado após otimizações:** **~$90-100/mês**

## 🔧 Manutenção

### Atualizar Aplicação

```bash
# 1. Build nova imagem
docker build -t oficina-mecânica:v2.0 .

# 2. Tag e push
ECR_URL=$(terraform output -raw ecr_repository_url)
docker tag oficina-mecânica:v2.0 $ECR_URL:v2.0
docker push $ECR_URL:v2.0

# 3. Atualizar task definition (via Terraform)
# Edite terraform.tfvars:
container_image = "<ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/oficina-mecânica:v2.0"

# 4. Aplicar mudanças
terraform apply

# 5. Deployment rolling automático será iniciado
```

### Backup do Banco de Dados

**Backups Automáticos:**
- Configurados para 7 dias de retenção
- Janela de backup: 03:00-04:00 UTC
- Snapshots armazenados no S3

**Backup Manual:**

```bash
aws rds create-db-snapshot \
  --db-instance-identifier oficina-dev-db \
  --db-snapshot-identifier oficina-dev-backup-$(date +%Y%m%d)
```

**Restaurar Backup:**

```bash
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier oficina-dev-db-restored \
  --db-snapshot-identifier oficina-dev-backup-20250115
```

### Atualizar Terraform Modules

```bash
# 1. Atualizar provider versions
terraform init -upgrade

# 2. Validar mudanças
terraform plan

# 3. Aplicar (sempre revise o plan antes!)
terraform apply
```

### Rollback de Deployment

```bash
# 1. Listar task definitions anteriores
aws ecs list-task-definitions \
  --family-prefix oficina-dev

# 2. Atualizar service para usar task definition anterior
aws ecs update-service \
  --cluster oficina-dev-cluster \
  --service oficina-dev-service \
  --task-definition oficina-dev:42  # versão anterior
```

### Escalar Manualmente (Temporário)

```bash
# Aumentar para 5 tasks
aws ecs update-service \
  --cluster oficina-dev-cluster \
  --service oficina-dev-service \
  --desired-count 5

# Auto scaling retomará controle após algum tempo
```

## 📚 Referências

### Documentação AWS
- [ECS Fargate](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html)
- [Application Load Balancer](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/)
- [RDS PostgreSQL](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_PostgreSQL.html)
- [Secrets Manager](https://docs.aws.amazon.com/secretsmanager/)
- [Container Insights](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/ContainerInsights.html)

### Terraform
- [AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [ECS Module](https://registry.terraform.io/modules/terraform-aws-modules/ecs/aws/latest)
- [VPC Module](https://registry.terraform.io/modules/terraform-aws-modules/vpc/aws/latest)

### Best Practices
- [ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/intro.html)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)

---

**Maintained by**: Equipe Oficina Mecânica  
**License**: MIT  
**Last Updated**: 2025-01-15  
**Versão**: 2.0.0 (Migração ECS Fargate)
