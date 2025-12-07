# 🗄️ Infraestrutura de Banco de Dados - PostgreSQL RDS Multi-AZ

[![Terraform](https://img.shields.io/badge/Terraform-1.6+-623CE4?logo=terraform)](https://www.terraform.io/)
[![AWS RDS](https://img.shields.io/badge/AWS-RDS_PostgreSQL_15-FF9900?logo=amazonaws)](https://aws.amazon.com/rds/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Provisionamento automatizado de banco de dados PostgreSQL RDS Multi-AZ com Terraform para o sistema de Oficina Mecânica.

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração](#-configuração)
- [Deploy](#-deploy)
- [Gerenciamento](#-gerenciamento)
- [Monitoramento](#-monitoramento)
- [Backups](#-backups)
- [Segurança](#-segurança)
- [Troubleshooting](#-troubleshooting)

## 🎯 Visão Geral

Este repositório contém a infraestrutura como código (IaC) para provisionar e gerenciar o banco de dados PostgreSQL RDS Multi-AZ que serve como camada de persistência para todos os serviços da aplicação de Oficina Mecânica.

### Características Principais

- ✅ **Alta Disponibilidade**: RDS Multi-AZ com failover automático
- ✅ **Segurança**: Criptografia em repouso (KMS) e em trânsito (SSL/TLS)
- ✅ **Backups Automatizados**: Retenção configurável e point-in-time recovery
- ✅ **Monitoramento**: CloudWatch Alarms e Performance Insights
- ✅ **Escalabilidade**: Read replicas e auto-scaling de storage
- ✅ **Conformidade**: Logs de auditoria e rotação de credenciais
- ✅ **Multi-Ambiente**: Configurações separadas para dev, staging e prod

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                        AWS CLOUD (VPC)                          │
│                                                                 │
│  ┌──────────────┐        ┌──────────────┐                      │
│  │  AZ us-east-1a│       │  AZ us-east-1b│                      │
│  │              │        │              │                       │
│  │  ┌────────┐  │        │  ┌────────┐  │                      │
│  │  │Private │  │        │  │Private │  │                      │
│  │  │Subnet  │  │        │  │Subnet  │  │                      │
│  │  │        │  │        │  │        │  │                      │
│  │  │  RDS   │◄─┼────────┼─►│  RDS   │  │                      │
│  │  │Primary │  │        │  │Standby │  │                      │
│  │  │Instance│  │ Multi- │  │Instance│  │                      │
│  │  │        │  │   AZ   │  │        │  │                      │
│  │  │ :5432  │  │  Sync  │  │ :5432  │  │                      │
│  │  └────▲───┘  │        │  └────────┘  │                      │
│  │       │      │        │              │                       │
│  └───────┼──────┘        └──────────────┘                       │
│          │                                                      │
│  ┌───────┼────────────────────────────────────────────┐        │
│  │ Security Group (RDS)                               │        │
│  │ ┌─────────────────────────────────────────────┐    │        │
│  │ │ Inbound: TCP 5432 from Lambda SG            │    │        │
│  │ │ Inbound: TCP 5432 from EKS Node SG          │    │        │
│  │ │ Outbound: All traffic                       │    │        │
│  │ └─────────────────────────────────────────────┘    │        │
│  └────────────────────────────────────────────────────┘        │
│          │                                                      │
│          ▼                                                      │
│  ┌──────────────────────┐                                      │
│  │  AWS Secrets Manager │                                      │
│  │  ┌────────────────┐  │                                      │
│  │  │ DB Credentials │  │  Auto-rotation: 30 days              │
│  │  │ - username     │  │  KMS encrypted                       │
│  │  │ - password     │  │                                      │
│  │  │ - endpoint     │  │                                      │
│  │  │ - port         │  │                                      │
│  │  └────────────────┘  │                                      │
│  └──────────────────────┘                                      │
│                                                                 │
│  ┌──────────────────────────────────────────────────┐          │
│  │  AWS KMS                                         │          │
│  │  ┌────────────────────────────────────────────┐  │          │
│  │  │ Customer Managed Key (CMK)                 │  │          │
│  │  │ - Encrypts RDS storage                     │  │          │
│  │  │ - Encrypts automated backups               │  │          │
│  │  │ - Encrypts snapshots                       │  │          │
│  │  │ - Rotation: Automatic (1 year)             │  │          │
│  │  └────────────────────────────────────────────┘  │          │
│  └──────────────────────────────────────────────────┘          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

         ┌──────────────────────────────────────┐
         │  CloudWatch Monitoring               │
         │  ┌────────────────────────────────┐  │
         │  │ Alarms:                        │  │
         │  │ - CPU > 80%                    │  │
         │  │ - Storage < 10%                │  │
         │  │ - Connection count > 80%       │  │
         │  │ - Replica lag > 10s            │  │
         │  └────────────────────────────────┘  │
         └──────────────────────────────────────┘
```

### Diagrama de Backup e Recovery

```
┌─────────────────────────────────────────────────────────┐
│  RDS Automated Backups                                  │
│                                                         │
│  ┌──────────────┐      ┌──────────────┐               │
│  │  Daily Full  │      │  WAL Logs    │               │
│  │   Backup     │      │  (5-min)     │               │
│  │              │      │              │               │
│  │  Retention:  │      │  Point-in-   │               │
│  │  30 days     │──────│  Time        │               │
│  │              │      │  Recovery    │               │
│  │  Window:     │      │  (PITR)      │               │
│  │  03:00-04:00 │      │              │               │
│  │     UTC      │      │  Up to 30    │               │
│  │              │      │  days back   │               │
│  └──────────────┘      └──────────────┘               │
│                                                        │
│  ┌─────────────────────────────────────────┐          │
│  │  Manual Snapshots                       │          │
│  │  - Before major upgrades                │          │
│  │  - Production releases                  │          │
│  │  - Retention: Until deleted             │          │
│  │  - Cross-region copy enabled            │          │
│  └─────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────┘
```

## 🛠️ Tecnologias

| Categoria | Tecnologia | Versão | Propósito |
|-----------|-----------|--------|-----------|
| **IaC** | Terraform | 1.6+ | Provisionamento de infraestrutura |
| **Database** | PostgreSQL | 15.4 | Sistema de banco de dados relacional |
| **Cloud Provider** | AWS RDS | - | Serviço gerenciado de banco de dados |
| **Segurança** | AWS KMS | - | Criptografia de dados |
| **Secrets** | AWS Secrets Manager | - | Gerenciamento de credenciais |
| **Monitoramento** | CloudWatch | - | Métricas e alarmes |
| **CI/CD** | GitHub Actions | - | Automação de deploy |

### Recursos AWS Utilizados

- **RDS PostgreSQL 15**: Instância Multi-AZ `db.t3.medium` (prod) / `db.t3.small` (dev)
- **KMS**: Customer Managed Key para criptografia
- **Secrets Manager**: Armazenamento e rotação de credenciais
- **VPC**: Isolamento de rede privada
- **Security Groups**: Controle de acesso à camada de rede
- **CloudWatch**: Monitoramento e alertas
- **SNS**: Notificações de alarmes

## 📦 Pré-requisitos

### Software Necessário

```bash
# Terraform
terraform --version  # >= 1.6.0

# AWS CLI
aws --version  # >= 2.13.0

# Git
git --version  # >= 2.40.0
```

### Credenciais AWS

Configure suas credenciais AWS:

```bash
aws configure
# AWS Access Key ID: <seu-access-key>
# AWS Secret Access Key: <seu-secret-key>
# Default region name: us-east-1
# Default output format: json
```

### Permissões IAM Necessárias

O usuário/role IAM precisa das seguintes permissões:

- `rds:*`
- `kms:CreateKey`, `kms:DescribeKey`, `kms:CreateAlias`
- `secretsmanager:CreateSecret`, `secretsmanager:GetSecretValue`
- `ec2:DescribeVpcs`, `ec2:DescribeSubnets`, `ec2:CreateSecurityGroup`
- `cloudwatch:PutMetricAlarm`
- `sns:CreateTopic`, `sns:Subscribe`

## ⚙️ Configuração

### 1. Clone o Repositório

```bash
git clone https://github.com/edimilsonldutra/infra-database-terraform.git
cd infra-database-terraform
```

### 2. Estrutura do Projeto

```
infra-database-terraform/
├── modules/
│   ├── rds/
│   │   ├── main.tf              # Recurso RDS principal
│   │   ├── variables.tf         # Variáveis do módulo
│   │   ├── outputs.tf           # Outputs exportados
│   │   └── security-groups.tf   # Security Groups
│   ├── kms/
│   │   ├── main.tf              # KMS key para criptografia
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── secrets/
│   │   ├── main.tf              # Secrets Manager
│   │   ├── rotation.tf          # Lambda de rotação
│   │   ├── variables.tf
│   │   └── outputs.tf
│   └── monitoring/
│       ├── alarms.tf            # CloudWatch Alarms
│       ├── dashboards.tf        # CloudWatch Dashboards
│       ├── variables.tf
│       └── outputs.tf
├── environments/
│   ├── dev/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── terraform.tfvars
│   │   └── backend.tf
│   ├── staging/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── terraform.tfvars
│   │   └── backend.tf
│   └── prod/
│       ├── main.tf
│       ├── variables.tf
│       ├── terraform.tfvars
│       └── backend.tf
├── scripts/
│   ├── init-db.sql              # Script de inicialização
│   ├── backup.sh                # Script de backup manual
│   └── restore.sh               # Script de restore
├── .github/
│   └── workflows/
│       └── deploy.yml           # CI/CD pipeline
├── README.md
└── BRANCH-PROTECTION.md
```

### 3. Configurar Backend do Terraform

Crie um bucket S3 para o state do Terraform:

```bash
aws s3 mb s3://oficina-terraform-state-<seu-id>
aws s3api put-bucket-versioning \
  --bucket oficina-terraform-state-<seu-id> \
  --versioning-configuration Status=Enabled
```

Atualize `environments/dev/backend.tf`:

```hcl
terraform {
  backend "s3" {
    bucket         = "oficina-terraform-state-<seu-id>"
    key            = "rds/dev/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
  }
}
```

### 4. Configurar Variáveis

Edite `environments/dev/terraform.tfvars`:

```hcl
# Configuração do Ambiente
environment = "dev"
project     = "oficina-mecanica"

# Configuração do RDS
db_name              = "oficina_db"
db_instance_class    = "db.t3.small"
db_allocated_storage = 20
db_engine_version    = "15.4"
multi_az             = false  # true para prod

# Configuração de Backup
backup_retention_period = 7   # 30 para prod
backup_window          = "03:00-04:00"
maintenance_window     = "sun:04:00-sun:05:00"

# Configuração de Rede
vpc_id             = "vpc-xxxxxxxxx"  # Usar output do infra-kubernetes-terraform
private_subnet_ids = ["subnet-xxxxxx", "subnet-yyyyyy"]

# Tags
tags = {
  Environment = "dev"
  Project     = "oficina-mecanica"
  ManagedBy   = "terraform"
  Repository  = "infra-database-terraform"
}
```

## 🚀 Deploy

### Deploy Manual

#### 1. Inicializar Terraform

```bash
cd environments/dev
terraform init
```

#### 2. Planejar Mudanças

```bash
terraform plan -out=tfplan
```

#### 3. Aplicar Infraestrutura

```bash
terraform apply tfplan
```

#### 4. Verificar Outputs

```bash
terraform output
```

Saída esperada:

```
db_endpoint = "oficina-db-dev.xxxxxxxxx.us-east-1.rds.amazonaws.com:5432"
db_secret_arn = "arn:aws:secretsmanager:us-east-1:xxxxx:secret:oficina-db-dev-xxxxx"
db_instance_id = "oficina-db-dev"
kms_key_id = "arn:aws:kms:us-east-1:xxxxx:key/xxxxx"
```

### Deploy Automatizado (CI/CD)

O deploy automático é executado via GitHub Actions:

- **Dev**: Deploy automático em push para `develop`
- **Staging**: Deploy automático em push para `staging`
- **Prod**: Deploy manual com aprovação em push para `main`

## 🔧 Gerenciamento

### Acessar Credenciais do Banco

```bash
# Via AWS CLI
aws secretsmanager get-secret-value \
  --secret-id oficina-db-dev \
  --query SecretString \
  --output text | jq '.'

# Via Console AWS
# Secrets Manager > oficina-db-dev > Retrieve secret value
```

### Conectar ao Banco de Dados

```bash
# Obter credenciais
export DB_HOST=$(terraform output -raw db_endpoint | cut -d: -f1)
export DB_SECRET=$(aws secretsmanager get-secret-value --secret-id oficina-db-dev --query SecretString --output text)
export DB_USER=$(echo $DB_SECRET | jq -r '.username')
export DB_PASS=$(echo $DB_SECRET | jq -r '.password')

# Conectar via psql
psql "postgresql://$DB_USER:$DB_PASS@$DB_HOST:5432/oficina_db?sslmode=require"
```

### Executar Migrações

```bash
# Aplicar schema inicial
psql "postgresql://$DB_USER:$DB_PASS@$DB_HOST:5432/oficina_db?sslmode=require" \
  -f scripts/init-db.sql
```

### Escalar Instância

Edite `terraform.tfvars`:

```hcl
db_instance_class = "db.t3.medium"  # ou db.r6g.large para prod
```

Aplique:

```bash
terraform apply
```

> ⚠️ **Atenção**: Escalar para uma instância maior pode causar alguns minutos de downtime.

### Criar Snapshot Manual

```bash
aws rds create-db-snapshot \
  --db-instance-identifier oficina-db-prod \
  --db-snapshot-identifier oficina-db-prod-$(date +%Y%m%d-%H%M%S)
```

## 📊 Monitoramento

### CloudWatch Dashboards

Acesse: [CloudWatch Console](https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#dashboards:name=RDS-oficina-db-prod)

**Métricas Principais:**
- CPU Utilization
- Database Connections
- Free Storage Space
- Read/Write IOPS
- Read/Write Latency
- Replica Lag (Multi-AZ)

### Alarmes Configurados

| Alarme | Condição | Ação |
|--------|----------|------|
| **HighCPU** | CPU > 80% por 5 min | SNS notification |
| **LowStorage** | Storage < 10% | SNS notification |
| **HighConnections** | Connections > 80% max | SNS notification |
| **ReplicaLag** | Lag > 10s | SNS notification |
| **DatabaseDown** | Status != available | SNS notification + PagerDuty |

### Performance Insights

Habilitado por padrão em todas as instâncias:

```bash
# Via Console AWS
RDS > Performance Insights > oficina-db-prod

# Via CLI
aws rds describe-db-instances \
  --db-instance-identifier oficina-db-prod \
  --query 'DBInstances[0].PerformanceInsightsEnabled'
```

### Logs

Logs disponíveis no CloudWatch Logs:

- **postgresql.log**: Logs gerais do PostgreSQL
- **upgrade.log**: Logs de upgrade de versão

```bash
# Ver logs recentes
aws logs tail /aws/rds/instance/oficina-db-prod/postgresql --follow
```

## 💾 Backups

### Backups Automatizados

- **Frequência**: Diária
- **Janela**: 03:00-04:00 UTC
- **Retenção**: 30 dias (prod) / 7 dias (dev)
- **Point-in-Time Recovery**: Habilitado (últimos 30 dias)

### Restaurar de Backup Automatizado

```bash
# Listar backups disponíveis
aws rds describe-db-snapshots \
  --db-instance-identifier oficina-db-prod

# Restaurar para um ponto específico
aws rds restore-db-instance-to-point-in-time \
  --source-db-instance-identifier oficina-db-prod \
  --target-db-instance-identifier oficina-db-prod-restored \
  --restore-time 2025-12-05T10:00:00Z
```

### Backup Cross-Region

Configurado para replicar snapshots para `us-west-2`:

```bash
# Verificar snapshots copiados
aws rds describe-db-snapshots \
  --region us-west-2 \
  --snapshot-type manual
```

## 🔒 Segurança

### Criptografia

- ✅ **Em Repouso**: KMS Customer Managed Key (CMK)
- ✅ **Em Trânsito**: SSL/TLS obrigatório
- ✅ **Backups**: Criptografados com a mesma KMS key

### Network Isolation

- ✅ **Private Subnets**: RDS não tem IP público
- ✅ **Security Groups**: Acesso apenas de Lambda e EKS
- ✅ **VPC Peering**: Não configurado (não necessário)

### Rotação de Credenciais

- ✅ **Automática**: A cada 30 dias via Secrets Manager
- ✅ **Zero Downtime**: Aplicações usam Secrets Manager SDK

### Auditoria

```bash
# Habilitar logs de auditoria PostgreSQL
# Já configurado via parameter group no Terraform
aws rds describe-db-parameters \
  --db-parameter-group-name oficina-postgres15-params \
  --query "Parameters[?ParameterName=='log_statement']"
```

### Compliance

- ✅ **Encryption at Rest**: HIPAA, PCI-DSS compliant
- ✅ **Backup Retention**: SOC 2 compliant (30 dias)
- ✅ **Audit Logs**: Retidos no CloudWatch por 90 dias

## 🐛 Troubleshooting

### Conexão Recusada

```bash
# Verificar Security Group
aws ec2 describe-security-groups \
  --group-ids sg-xxxxxxxxx \
  --query 'SecurityGroups[0].IpPermissions'

# Verificar status da instância
aws rds describe-db-instances \
  --db-instance-identifier oficina-db-prod \
  --query 'DBInstances[0].DBInstanceStatus'
```

### Alta Latência

```bash
# Verificar Performance Insights
# Console > RDS > Performance Insights

# Verificar IOPS
aws cloudwatch get-metric-statistics \
  --namespace AWS/RDS \
  --metric-name ReadIOPS \
  --dimensions Name=DBInstanceIdentifier,Value=oficina-db-prod \
  --start-time 2025-12-05T00:00:00Z \
  --end-time 2025-12-05T23:59:59Z \
  --period 3600 \
  --statistics Average
```

### Storage Cheio

```bash
# Aumentar storage (sem downtime)
aws rds modify-db-instance \
  --db-instance-identifier oficina-db-prod \
  --allocated-storage 200 \
  --apply-immediately
```

### Falha no Failover Multi-AZ

```bash
# Forçar failover para testar
aws rds reboot-db-instance \
  --db-instance-identifier oficina-db-prod \
  --force-failover

# Monitorar eventos
aws rds describe-events \
  --source-identifier oficina-db-prod \
  --duration 60
```

### Erros Comuns

| Erro | Causa | Solução |
|------|-------|---------|
| `could not connect to server` | Security Group bloqueando | Adicionar regra de ingress |
| `FATAL: password authentication failed` | Credenciais desatualizadas | Verificar Secrets Manager |
| `too many connections` | Limit de conexões atingido | Aumentar `max_connections` no parameter group |
| `disk full` | Storage esgotado | Aumentar `allocated_storage` |

## 📚 Recursos Adicionais

### Documentação

- [Terraform AWS Provider - RDS](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/db_instance)
- [AWS RDS PostgreSQL Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_PostgreSQL.html)
- [PostgreSQL 15 Documentation](https://www.postgresql.org/docs/15/index.html)

### Links Úteis

- 📖 [BRANCH-PROTECTION.md](BRANCH-PROTECTION.md) - Configuração de proteção de branches
- 🏗️ [Repositório da Aplicação K8s](https://github.com/edimilsonldutra/oficina-service-k8s)
- 🔐 [Repositório Lambda Auth](https://github.com/edimilsonldutra/lambda-auth-service)
- ☸️ [Repositório Infra K8s](https://github.com/edimilsonldutra/infra-kubernetes-terraform)

### Suporte

- **Issues**: [GitHub Issues](https://github.com/edimilsonldutra/infra-database-terraform/issues)
- **Email**: edimilsonldutra@example.com

---

**Maintained by**: Edimilson L. Dutra  
**License**: MIT  
**Last Updated**: 2025-12-05
