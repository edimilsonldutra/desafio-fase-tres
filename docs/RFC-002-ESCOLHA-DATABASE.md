# RFC-002: Escolha de Banco de Dados

**Status**: Aprovado  
**Data**: 2025-12-05  
**Autor**: Edimilson L. Dutra  
**Revisores**: Equipe de Arquitetura, DBA  

---

## 📋 Sumário Executivo

Este RFC documenta a decisão técnica sobre qual banco de dados utilizar para persistir os dados do Sistema de Gestão de Oficina Mecânica.

---

## 🎯 Problema

Precisamos escolher um banco de dados que atenda aos seguintes requisitos:

### Requisitos Funcionais
1. **Transações ACID**: Garantir consistência em operações complexas (ordens de serviço)
2. **Relacionamentos Complexos**: Suportar múltiplas entidades relacionadas
3. **Integridade Referencial**: Foreign keys, constraints, triggers
4. **Consultas Analíticas**: Relatórios de faturamento, peças mais usadas
5. **Full-text Search**: Busca por descrições de serviços

### Requisitos Não-Funcionais
1. **Performance**: Latência <50ms para queries simples
2. **Escalabilidade**: Suportar 100k+ registros de ordens de serviço
3. **Disponibilidade**: Multi-AZ com failover automático
4. **Backup**: Point-in-time recovery até 35 dias
5. **Segurança**: Encryption at rest e in transit

---

## 🔍 Opções Avaliadas

### Opção 1: PostgreSQL (Relacional)

#### ✅ Prós
- **ACID Completo**: Transações robustas com isolation levels configuráveis
- **Relacionamentos**: Foreign keys nativas com CASCADE
- **Tipos de Dados**: JSONB para dados semi-estruturados
- **Extensões**: PostGIS (geolocalização), pg_trgm (full-text search)
- **Índices Avançados**: B-tree, Hash, GIN, GiST, BRIN
- **Triggers e Procedures**: Lógica de negócio no banco
- **Window Functions**: Análises complexas (ranking, aggregations)
- **Maturidade**: 30+ anos de desenvolvimento
- **Open Source**: Sem custos de licença
- **RDS Suporte**: AWS RDS com Multi-AZ e read replicas

#### ❌ Contras
- **Escalabilidade Horizontal**: Complexa (sharding manual)
- **Performance em Escala**: Slower que NoSQL para writes massivos
- **Schema Rigidity**: Mudanças de schema requerem migrations

#### 💰 Custo (AWS RDS)
- **db.t3.medium Multi-AZ**: $120/mês (2 vCPU, 4 GB RAM)
- **Storage**: 100 GB GP3 = $23/mês
- **Backups**: 100 GB × $0.095 = $9.50/mês
- **Total**: **~$152.50/mês**

#### 📊 Performance
- **Latência**: 5-20ms (queries simples)
- **Throughput**: 10k+ queries/segundo
- **Connections**: 150 simultâneas (t3.medium)

---

### Opção 2: MySQL (Relacional)

#### ✅ Prós
- **Popularidade**: Mais usado no mundo
- **Performance**: Melhor em reads que PostgreSQL (InnoDB)
- **Replicação**: Nativa e fácil de configurar
- **Ferramentas**: Amplo ecossistema (phpMyAdmin, MySQL Workbench)
- **RDS Suporte**: AWS RDS com Multi-AZ

#### ❌ Contras
- **ACID**: Menos robusto que PostgreSQL (antes do 8.0)
- **Features**: Sem JSONB nativo, window functions limitadas
- **Compliance**: Questões de licença (Oracle ownership)
- **Extensibilidade**: Menos extensões que PostgreSQL

#### 💰 Custo (AWS RDS)
- **db.t3.medium Multi-AZ**: $110/mês
- **Storage**: 100 GB GP3 = $23/mês
- **Total**: **~$133/mês**

---

### Opção 3: DynamoDB (NoSQL)

#### ✅ Prós
- **Serverless**: Auto-scaling completo
- **Performance**: Latência <10ms garantida
- **Escalabilidade**: Horizontal infinita
- **Custo**: Pay-per-request (sem instâncias)
- **Disponibilidade**: 99.99% SLA Multi-AZ nativo
- **Managed**: Zero administração

#### ❌ Contras
- **Sem Joins**: Queries complexas requerem múltiplas chamadas
- **Sem Transações Multi-Item**: Limitado a 25 itens
- **Modelagem**: Difícil para relacionamentos complexos
- **Sem ACID Completo**: Eventual consistency padrão
- **Custos**: Imprevisíveis com escala
- **Vendor Lock-in**: Proprietário da AWS

#### 💰 Custo (On-Demand)
- **10M reads**: 10M × $0.25/1M = $2.50
- **5M writes**: 5M × $1.25/1M = $6.25
- **Storage**: 10 GB × $0.25 = $2.50
- **Total**: **~$11.25/mês** (pode variar muito)

---

### Opção 4: MongoDB (NoSQL Documental)

#### ✅ Prós
- **Flexibilidade**: Schema-less
- **Performance**: Boa para reads com índices
- **Escalabilidade**: Sharding nativo
- **JSONB Nativo**: Documentos aninhados
- **Ecosystem**: Amplo (MongoDB Atlas, Compass)

#### ❌ Contras
- **ACID**: Transações limitadas (single document)
- **Relacionamentos**: Difícil de modelar 1:N e M:N
- **Custo**: MongoDB Atlas caro na AWS
- **Joins**: Agregações complexas lentas ($lookup)
- **Consistência**: Eventual por padrão

#### 💰 Custo (DocumentDB - compatível)
- **db.t3.medium**: $130/mês
- **Storage**: 100 GB × $0.10 = $10/mês
- **Total**: **~$140/mês**

---

## 📊 Matriz de Decisão

| Critério | Peso | PostgreSQL | MySQL | DynamoDB | MongoDB |
|----------|------|------------|-------|----------|---------|
| **ACID** | 25% | 10 | 7 | 4 | 5 |
| **Relacionamentos** | 20% | 10 | 9 | 3 | 4 |
| **Performance** | 15% | 8 | 9 | 10 | 8 |
| **Custo** | 15% | 7 | 8 | 9 | 7 |
| **Maturidade** | 10% | 10 | 10 | 7 | 7 |
| **Escalabilidade** | 10% | 6 | 6 | 10 | 8 |
| **Facilidade** | 5% | 7 | 8 | 5 | 6 |
| **Total** | 100% | **8.25** | **8.00** | **6.35** | **6.15** |

### Cálculo
- **PostgreSQL**: (10×0.25) + (10×0.2) + (8×0.15) + (7×0.15) + (10×0.1) + (6×0.1) + (7×0.05) = **8.25**
- **MySQL**: (7×0.25) + (9×0.2) + (9×0.15) + (8×0.15) + (10×0.1) + (6×0.1) + (8×0.05) = **8.00**
- **DynamoDB**: (4×0.25) + (3×0.2) + (10×0.15) + (9×0.15) + (7×0.1) + (10×0.1) + (5×0.05) = **6.35**
- **MongoDB**: (5×0.25) + (4×0.2) + (8×0.15) + (7×0.15) + (7×0.1) + (8×0.1) + (6×0.05) = **6.15**

---

## ✅ Decisão

**Escolhemos PostgreSQL 15** pelos seguintes motivos:

### Fatores Decisivos

1. **ACID Completo e Robusto**
   - Transações essenciais para ordens de serviço (múltiplas inserções atômicas)
   - Isolation levels configuráveis (Read Committed padrão)
   - Foreign key constraints garantem integridade

2. **Relacionamentos Complexos**
   - Modelo de dados com 8+ tabelas relacionadas:
     - `customers` ↔ `vehicles` (1:N)
     - `customers` ↔ `work_orders` (1:N)
     - `work_orders` ↔ `services` (M:N via `work_order_services`)
     - `work_orders` ↔ `parts` (M:N via `work_order_parts`)
   - Joins eficientes com índices otimizados

3. **Features Avançadas**
   - **JSONB**: Armazenar metadados flexíveis (configurações, histórico)
   - **Triggers**: Atualizar estoque automaticamente
   - **Views**: Simplificar queries de relatórios
   - **Window Functions**: Ranking de peças mais usadas
   - **Full-text Search**: `pg_trgm` para busca de serviços

4. **Performance**
   - Índices B-tree em chaves primárias
   - Índices GIN para JSONB e full-text
   - Query planner sofisticado
   - Connection pooling (HikariCP / PgBouncer)

5. **Alta Disponibilidade (RDS Multi-AZ)**
   - Replicação síncrona para standby
   - Failover automático em <60 segundos
   - Backups automáticos com retention de 7-35 dias
   - Point-in-time recovery

6. **Segurança**
   - Encryption at rest (KMS)
   - Encryption in transit (SSL/TLS)
   - IAM authentication
   - Network isolation (VPC)

7. **Custo-Benefício**
   - $152/mês previsível
   - Sem surpresas de billing (vs DynamoDB on-demand)
   - Reserved Instances para 40% desconto

---

## 🗄️ Schema Relacional

### Modelo de Entidades

```
┌─────────────────┐
│   customers     │
├─────────────────┤
│ id (PK)         │
│ cpf (UNIQUE)    │──┐
│ name            │  │
│ email           │  │
│ phone           │  │
│ created_at      │  │
└─────────────────┘  │
                     │ 1:N
                     ▼
              ┌──────────────┐
              │  vehicles    │
              ├──────────────┤
              │ id (PK)      │
              │ customer_id  │
              │ license_plate│
              │ brand        │
              │ model        │
              │ year         │
              └──────────────┘
                     │ 1:N
                     ▼
              ┌────────────────┐
              │ work_orders    │
              ├────────────────┤
              │ id (PK)        │
              │ order_number   │
              │ customer_id    │
              │ vehicle_id     │
              │ status         │
              │ total_price    │
              │ created_at     │
              └────────────────┘
                  │       │
          ┌───────┘       └───────┐
          │ M:N                M:N│
          ▼                       ▼
┌──────────────────┐   ┌──────────────────┐
│ work_order_      │   │ work_order_parts │
│ services         │   ├──────────────────┤
├──────────────────┤   │ id (PK)          │
│ id (PK)          │   │ work_order_id    │
│ work_order_id    │   │ part_id          │
│ service_id       │   │ quantity         │
│ quantity         │   │ unit_price       │
│ unit_price       │   └──────────────────┘
└──────────────────┘           │
          │                    │
          ▼                    ▼
┌──────────────┐      ┌──────────────┐
│  services    │      │    parts     │
├──────────────┤      ├──────────────┤
│ id (PK)      │      │ id (PK)      │
│ name         │      │ name         │
│ description  │      │ description  │
│ price        │      │ price        │
│ duration_min │      │ stock_qty    │
└──────────────┘      └──────────────┘
```

---

## 🚀 Plano de Implementação

### Fase 1: Provisionamento (Semana 1)
```hcl
# Terraform para RDS PostgreSQL
resource "aws_db_instance" "oficina_db" {
  identifier             = "oficina-${var.environment}"
  engine                 = "postgres"
  engine_version         = "15.4"
  instance_class         = "db.t3.medium"
  allocated_storage      = 100
  storage_type           = "gp3"
  storage_encrypted      = true
  kms_key_id            = aws_kms_key.rds.arn
  
  multi_az               = true
  db_subnet_group_name   = aws_db_subnet_group.oficina.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  
  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"
  
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]
  performance_insights_enabled    = true
  
  deletion_protection = true
  skip_final_snapshot = false
  final_snapshot_identifier = "oficina-${var.environment}-final"
}
```

### Fase 2: Schema Creation (Semana 1-2)
```sql
-- Criar tabelas na ordem de dependências
CREATE TABLE customers (...);
CREATE TABLE vehicles (...);
CREATE TABLE services (...);
CREATE TABLE parts (...);
CREATE TABLE work_orders (...);
CREATE TABLE work_order_services (...);
CREATE TABLE work_order_parts (...);

-- Criar índices
CREATE INDEX idx_customers_cpf ON customers(cpf);
CREATE INDEX idx_vehicles_customer ON vehicles(customer_id);
CREATE INDEX idx_orders_customer_date ON work_orders(customer_id, created_at);

-- Criar triggers
CREATE TRIGGER update_stock_after_order ...
```

### Fase 3: Migração de Dados (Semana 3)
- [ ] Exportar dados de sistema legado (se existir)
- [ ] Transformar para formato PostgreSQL
- [ ] Usar `pg_restore` para importação
- [ ] Validar integridade referencial

### Fase 4: Connection Pooling (Semana 3)
```java
// HikariCP configuration
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://...");
config.setMaximumPoolSize(20);
config.setMinimumIdle(5);
config.setConnectionTimeout(30000);
config.setIdleTimeout(600000);
```

### Fase 5: Backup e Recovery (Semana 4)
- [ ] Configurar automated backups (7 dias)
- [ ] Testar point-in-time recovery
- [ ] Criar runbook para disaster recovery

---

## 📈 Métricas de Sucesso

| Métrica | Target | Como Medir |
|---------|--------|------------|
| **Query Latency (P95)** | <50ms | CloudWatch RDS metrics |
| **Connection Errors** | <0.1% | Application logs |
| **Database CPU** | <70% | CloudWatch RDS CPU |
| **Storage Growth** | <5GB/mês | CloudWatch RDS storage |
| **Backup Success Rate** | 100% | RDS automated backups |

---

## 🔄 Estratégia de Migração Futura

### Se precisarmos escalar além de 1M+ ordens/dia:

#### Opção 1: Read Replicas
- Criar 2-3 read replicas
- Direcionar reads para replicas
- Writes apenas no master
- **Custo**: +$300/mês

#### Opção 2: Sharding Horizontal
- Particionar por `customer_id` (mod 4)
- 4 instâncias PostgreSQL
- **Complexidade**: Alta
- **Custo**: +$600/mês

#### Opção 3: Hybrid (PostgreSQL + DynamoDB)
- PostgreSQL para transações (write-heavy)
- DynamoDB para cache/leitura (read-heavy)
- **Complexidade**: Média
- **Custo**: +$200/mês

---

## 📚 Referências

- [PostgreSQL 15 Documentation](https://www.postgresql.org/docs/15/)
- [AWS RDS PostgreSQL Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)
- [Database Design Patterns](https://www.oreilly.com/library/view/sql-antipatterns/9781680500073/)

---

**Aprovado por**: Equipe de Arquitetura, DBA Lead  
**Data de Aprovação**: 2025-12-05  
**Próxima Revisão**: 2026-06-05
