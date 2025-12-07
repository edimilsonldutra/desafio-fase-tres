# 🗄️ Justificativa Técnica: Escolha e Modelagem do Banco de Dados

**Projeto**: Sistema de Gestão de Oficina Mecânica  
**Data**: 2025-12-05  
**Versão**: 1.0  
**Autor**: Edimilson L. Dutra  

---

## 📋 Sumário Executivo

Este documento apresenta a justificativa formal para a escolha do **PostgreSQL 15** como banco de dados relacional, incluindo análise comparativa, modelagem entidade-relacionamento (ER), normalização, estratégia de indexação e otimizações de performance.

---

## 🎯 Por que PostgreSQL 15?

### Decisão Final
**PostgreSQL 15.4** foi escolhido como banco de dados principal por oferecer o melhor equilíbrio entre:
- **ACID completo** para transações críticas
- **Relacionamentos complexos** (8 tabelas com foreign keys)
- **Performance otimizada** (latência <50ms)
- **Alta disponibilidade** (RDS Multi-AZ)
- **Custo-benefício** ($152/mês)

---

## 📊 Análise Comparativa

### Opções Avaliadas

| Critério | PostgreSQL | MySQL | DynamoDB | MongoDB |
|----------|------------|-------|----------|---------|
| **ACID Completo** | ✅ Robusto | ⚠️ Limitado | ❌ Eventual | ⚠️ Single-doc |
| **Foreign Keys** | ✅ Nativas | ✅ Nativas | ❌ N/A | ❌ Manuais |
| **Joins Eficientes** | ✅ Otimizados | ✅ Bons | ❌ N/A | ⚠️ $lookup lento |
| **Transações Multi-Tabela** | ✅ Sim | ✅ Sim | ❌ Limitado (25 itens) | ⚠️ Limitado |
| **JSONB Nativo** | ✅ Sim | ❌ Não | ✅ Sim | ✅ Sim |
| **Window Functions** | ✅ Completas | ⚠️ Limitadas | ❌ N/A | ⚠️ Agregações |
| **Full-Text Search** | ✅ pg_trgm | ⚠️ FULLTEXT | ❌ N/A | ✅ Text indexes |
| **Triggers/Procedures** | ✅ PL/pgSQL | ✅ Sim | ❌ N/A | ❌ N/A |
| **Custo (RDS)** | $152/mês | $133/mês | ~$11/mês* | $140/mês |
| **Latência** | 5-20ms | 5-20ms | <10ms | 10-30ms |
| **Escalabilidade Horizontal** | ⚠️ Manual | ⚠️ Manual | ✅ Automática | ✅ Sharding |
| **Maturidade** | 30+ anos | 28+ anos | 10 anos | 15 anos |
| **Open Source** | ✅ Sim | ⚠️ Oracle | ❌ Proprietário | ✅ Sim |

*DynamoDB: Custo variável, pode explodir com escala

### Score Final (Matriz de Decisão - RFC-002)
1. **PostgreSQL**: 8.25/10 ✅ **ESCOLHIDO**
2. MySQL: 8.00/10
3. DynamoDB: 6.35/10
4. MongoDB: 6.15/10

---

## 🏗️ Modelo Entidade-Relacionamento (ER)

### Diagrama ER Completo

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      OFICINA MECÂNICA - MODELO ER                       │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────┐
│         CUSTOMERS           │
├─────────────────────────────┤
│ PK  id (UUID)               │
│ UQ  cpf (VARCHAR(11))       │────┐
│     name (VARCHAR(100))     │    │
│     email (VARCHAR(100))    │    │ 1:N (Um cliente possui vários veículos)
│     phone (VARCHAR(20))     │    │
│     address (TEXT)          │    │
│     created_at (TIMESTAMP)  │    │
│     updated_at (TIMESTAMP)  │    │
└─────────────────────────────┘    │
                                   │
                                   ▼
                      ┌──────────────────────────┐
                      │        VEHICLES          │
                      ├──────────────────────────┤
                      │ PK  id (UUID)            │
                      │ FK  customer_id          │────┐
                      │ UQ  license_plate        │    │
                      │     brand (VARCHAR(50))  │    │
                      │     model (VARCHAR(50))  │    │ 1:N (Um veículo possui várias ordens)
                      │     year (INTEGER)       │    │
                      │     color (VARCHAR(30))  │    │
                      │     created_at           │    │
                      │     updated_at           │    │
                      └──────────────────────────┘    │
                                                      │
                                                      ▼
                                         ┌────────────────────────────┐
                                         │      WORK_ORDERS           │
                                         ├────────────────────────────┤
                                         │ PK  id (UUID)              │
┌────────────────────────────────────────│ UQ  order_number          │
│                                        │ FK  customer_id            │
│                                        │ FK  vehicle_id             │
│                                        │     description (TEXT)     │
│                                        │     status (ENUM)          │
│                                        │     total_price (DECIMAL)  │
│                                        │     approved_at            │
│                                        │     completed_at           │
│                                        │     created_at             │
│                                        │     updated_at             │
│                                        └────────────────────────────┘
│                                                   │
│                                                   │
│                           ┌───────────────────────┼───────────────────────┐
│                           │                       │                       │
│                           │ M:N                   │ M:N                   │
│                           ▼                       ▼                       │
│              ┌──────────────────────┐  ┌──────────────────────┐          │
│              │ WORK_ORDER_SERVICES  │  │   WORK_ORDER_PARTS   │          │
│              ├──────────────────────┤  ├──────────────────────┤          │
│              │ PK  id (UUID)        │  │ PK  id (UUID)        │          │
│              │ FK  work_order_id    │  │ FK  work_order_id    │          │
│              │ FK  service_id       │  │ FK  part_id          │          │
│              │     quantity (INT)   │  │     quantity (INT)   │          │
│              │     unit_price       │  │     unit_price       │          │
│              │     subtotal         │  │     subtotal         │          │
│              │     created_at       │  │     created_at       │          │
│              └──────────────────────┘  └──────────────────────┘          │
│                           │                       │                       │
│                           │                       │                       │
│                           ▼                       ▼                       │
│                 ┌─────────────────┐     ┌─────────────────┐              │
│                 │    SERVICES     │     │      PARTS      │              │
│                 ├─────────────────┤     ├─────────────────┤              │
│                 │ PK  id (UUID)   │     │ PK  id (UUID)   │              │
│                 │     name        │     │     name        │              │
│                 │     description │     │     description │              │
│                 │     price       │     │     price       │              │
│                 │     duration_min│     │     stock_qty   │              │
│                 │     active      │     │     min_stock   │              │
│                 │     created_at  │     │     supplier    │              │
│                 │     updated_at  │     │     active      │              │
│                 └─────────────────┘     │     created_at  │              │
│                                         │     updated_at  │              │
│                                         └─────────────────┘              │
│                                                                           │
│ 1:N (Um cliente possui várias ordens de serviço)                         │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## 🔗 Relacionamentos Detalhados

### 1. CUSTOMERS ↔ VEHICLES (1:N)

**Cardinalidade**: Um cliente pode ter **zero ou mais** veículos.

```sql
-- Foreign Key
ALTER TABLE vehicles
ADD CONSTRAINT fk_vehicles_customer
FOREIGN KEY (customer_id) REFERENCES customers(id)
ON DELETE CASCADE -- Se cliente for deletado, veículos também são
ON UPDATE CASCADE;
```

**Justificativa**:
- Cliente pode cadastrar múltiplos veículos (carro, moto, caminhão)
- Veículo pertence a apenas um cliente (simplificação - não consideramos venda)
- `ON DELETE CASCADE`: Se cliente for deletado (LGPD), veículos também são removidos

---

### 2. CUSTOMERS ↔ WORK_ORDERS (1:N)

**Cardinalidade**: Um cliente pode ter **zero ou mais** ordens de serviço.

```sql
-- Foreign Key
ALTER TABLE work_orders
ADD CONSTRAINT fk_work_orders_customer
FOREIGN KEY (customer_id) REFERENCES customers(id)
ON DELETE RESTRICT -- Não permite deletar cliente com ordens
ON UPDATE CASCADE;
```

**Justificativa**:
- Cliente pode solicitar múltiplas ordens ao longo do tempo
- Ordem pertence a apenas um cliente
- `ON DELETE RESTRICT`: Preserva histórico de ordens (auditoria fiscal)

---

### 3. VEHICLES ↔ WORK_ORDERS (1:N)

**Cardinalidade**: Um veículo pode ter **zero ou mais** ordens de serviço.

```sql
-- Foreign Key
ALTER TABLE work_orders
ADD CONSTRAINT fk_work_orders_vehicle
FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
ON DELETE RESTRICT
ON UPDATE CASCADE;
```

**Justificativa**:
- Veículo pode ter múltiplas manutenções ao longo da vida
- Ordem refere-se a apenas um veículo
- `ON DELETE RESTRICT`: Preserva histórico de manutenções (valor de revenda)

---

### 4. WORK_ORDERS ↔ SERVICES (M:N via WORK_ORDER_SERVICES)

**Cardinalidade**: Uma ordem pode ter **um ou mais** serviços. Um serviço pode estar em **zero ou mais** ordens.

```sql
-- Junction Table
CREATE TABLE work_order_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    work_order_id UUID NOT NULL,
    service_id UUID NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_wos_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_wos_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT,
    CONSTRAINT uq_work_order_service UNIQUE (work_order_id, service_id) -- Evita duplicação
);
```

**Justificativa**:
- Ordem pode incluir múltiplos serviços (troca de óleo + alinhamento)
- Serviço pode ser oferecido em múltiplas ordens
- `subtotal` é calculado automaticamente (GENERATED column)
- `ON DELETE CASCADE` para work_order: Se ordem for deletada, itens também são
- `ON DELETE RESTRICT` para service: Não pode deletar serviço usado em ordens

---

### 5. WORK_ORDERS ↔ PARTS (M:N via WORK_ORDER_PARTS)

**Cardinalidade**: Uma ordem pode usar **zero ou mais** peças. Uma peça pode estar em **zero ou mais** ordens.

```sql
-- Junction Table
CREATE TABLE work_order_parts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    work_order_id UUID NOT NULL,
    part_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_wop_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_wop_part FOREIGN KEY (part_id) REFERENCES parts(id) ON DELETE RESTRICT,
    CONSTRAINT uq_work_order_part UNIQUE (work_order_id, part_id)
);
```

**Justificativa**:
- Ordem pode usar múltiplas peças (4 pneus + 1 bateria)
- Peça pode ser usada em múltiplas ordens
- `quantity CHECK`: Garante quantidade positiva
- `ON DELETE CASCADE`: Se ordem for deletada, itens de peças também são
- `ON DELETE RESTRICT`: Não pode deletar peça usada em ordens (auditoria)

---

## 📐 Normalização

### Forma Normal Atual: **3NF (Terceira Forma Normal)**

#### 1NF (Primeira Forma Normal) ✅
- **Regra**: Nenhum atributo multivalorado
- **Conformidade**: Todos os campos são atômicos (sem arrays ou JSON complexos)

#### 2NF (Segunda Forma Normal) ✅
- **Regra**: Nenhuma dependência parcial (todos os atributos dependem da chave primária completa)
- **Conformidade**: Todas as tabelas têm chave primária UUID, e atributos dependem dela

#### 3NF (Terceira Forma Normal) ✅
- **Regra**: Nenhuma dependência transitiva (atributos não-chave não dependem de outros atributos não-chave)
- **Conformidade**:
  - `subtotal` em junction tables é **GENERATED column** (derivado, não armazenado como dependência transitiva)
  - `total_price` em `work_orders` é calculado via trigger (não duplicação)

#### Por que não BCNF (Boyce-Codd Normal Form)?
- BCNF exige que toda dependência funcional tenha determinante como superchave
- Nosso modelo já está em 3NF e não apresenta anomalias
- BCNF traria complexidade sem benefícios práticos para este domínio

---

## 🚀 Estratégia de Indexação

### Índices Criados

#### 1. Primary Keys (Automáticos)
```sql
-- Todas as tabelas têm PK com UUID (índice B-tree automático)
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Índice automático
    ...
);
```

#### 2. Unique Constraints
```sql
-- CUSTOMERS
CREATE UNIQUE INDEX idx_customers_cpf ON customers(cpf);
CREATE UNIQUE INDEX idx_customers_email ON customers(email);

-- VEHICLES
CREATE UNIQUE INDEX idx_vehicles_license_plate ON vehicles(license_plate);

-- WORK_ORDERS
CREATE UNIQUE INDEX idx_work_orders_order_number ON work_orders(order_number);
```

#### 3. Foreign Keys (Performance de Joins)
```sql
-- VEHICLES
CREATE INDEX idx_vehicles_customer_id ON vehicles(customer_id);

-- WORK_ORDERS
CREATE INDEX idx_work_orders_customer_id ON work_orders(customer_id);
CREATE INDEX idx_work_orders_vehicle_id ON work_orders(vehicle_id);

-- JUNCTION TABLES
CREATE INDEX idx_wos_work_order_id ON work_order_services(work_order_id);
CREATE INDEX idx_wos_service_id ON work_order_services(service_id);
CREATE INDEX idx_wop_work_order_id ON work_order_parts(work_order_id);
CREATE INDEX idx_wop_part_id ON work_order_parts(part_id);
```

#### 4. Índices Compostos (Queries Complexas)
```sql
-- Buscar ordens de um cliente por data (relatórios)
CREATE INDEX idx_work_orders_customer_created 
ON work_orders(customer_id, created_at DESC);

-- Buscar ordens por status e data (dashboard)
CREATE INDEX idx_work_orders_status_created 
ON work_orders(status, created_at DESC);

-- Buscar veículos de cliente ordenados por marca/modelo
CREATE INDEX idx_vehicles_customer_brand_model 
ON vehicles(customer_id, brand, model);
```

#### 5. Full-Text Search (pg_trgm)
```sql
-- Habilitar extensão
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Índice GIN para busca fuzzy em serviços
CREATE INDEX idx_services_name_trgm ON services USING GIN (name gin_trgm_ops);
CREATE INDEX idx_services_description_trgm ON services USING GIN (description gin_trgm_ops);

-- Busca: SELECT * FROM services WHERE name % 'alinhamento';
-- Similaridade: 'alinhament' encontra 'alinhamento'
```

#### 6. Índices Parciais (Otimização de Espaço)
```sql
-- Apenas ordens pendentes (status mais consultado)
CREATE INDEX idx_work_orders_pending 
ON work_orders(created_at) 
WHERE status = 'PENDING';

-- Apenas peças com estoque baixo (alertas)
CREATE INDEX idx_parts_low_stock 
ON parts(name) 
WHERE stock_quantity < min_stock_quantity;
```

---

## ⚡ Otimizações de Performance

### 1. Triggers para Cálculos Automáticos

#### Atualizar Estoque ao Criar Ordem
```sql
CREATE OR REPLACE FUNCTION update_parts_stock()
RETURNS TRIGGER AS $$
BEGIN
    -- Reduzir estoque das peças usadas
    UPDATE parts
    SET stock_quantity = stock_quantity - NEW.quantity
    WHERE id = NEW.part_id;
    
    -- Verificar se estoque ficou negativo (constraint)
    IF (SELECT stock_quantity FROM parts WHERE id = NEW.part_id) < 0 THEN
        RAISE EXCEPTION 'Estoque insuficiente para a peça %', NEW.part_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_parts_stock
AFTER INSERT ON work_order_parts
FOR EACH ROW EXECUTE FUNCTION update_parts_stock();
```

#### Calcular Total da Ordem
```sql
CREATE OR REPLACE FUNCTION calculate_work_order_total()
RETURNS TRIGGER AS $$
BEGIN
    -- Somar serviços
    UPDATE work_orders
    SET total_price = (
        SELECT COALESCE(SUM(subtotal), 0)
        FROM work_order_services
        WHERE work_order_id = NEW.work_order_id
    ) + (
        SELECT COALESCE(SUM(subtotal), 0)
        FROM work_order_parts
        WHERE work_order_id = NEW.work_order_id
    )
    WHERE id = NEW.work_order_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calculate_total_services
AFTER INSERT OR UPDATE ON work_order_services
FOR EACH ROW EXECUTE FUNCTION calculate_work_order_total();

CREATE TRIGGER trg_calculate_total_parts
AFTER INSERT OR UPDATE ON work_order_parts
FOR EACH ROW EXECUTE FUNCTION calculate_work_order_total();
```

---

### 2. Views Materializadas (Relatórios)

#### Dashboard de Vendas (Atualizado Diariamente)
```sql
CREATE MATERIALIZED VIEW mv_daily_sales AS
SELECT
    DATE(created_at) AS sale_date,
    COUNT(*) AS total_orders,
    SUM(total_price) AS total_revenue,
    AVG(total_price) AS avg_order_value
FROM work_orders
WHERE status IN ('COMPLETED', 'INVOICED')
GROUP BY DATE(created_at)
ORDER BY sale_date DESC;

-- Índice para buscas rápidas
CREATE INDEX idx_mv_daily_sales_date ON mv_daily_sales(sale_date);

-- Refresh automático (via cron job ou Lambda)
REFRESH MATERIALIZED VIEW mv_daily_sales;
```

#### Top 10 Peças Mais Usadas
```sql
CREATE MATERIALIZED VIEW mv_top_parts AS
SELECT
    p.id,
    p.name,
    COUNT(wop.id) AS usage_count,
    SUM(wop.quantity) AS total_quantity_used,
    SUM(wop.subtotal) AS total_revenue
FROM parts p
JOIN work_order_parts wop ON p.id = wop.part_id
GROUP BY p.id, p.name
ORDER BY usage_count DESC
LIMIT 10;

REFRESH MATERIALIZED VIEW mv_top_parts;
```

---

### 3. Particionamento (Preparação Futura)

#### Particionar WORK_ORDERS por Data (Range Partitioning)
```sql
-- Criar tabela particionada (quando atingir 1M+ ordens)
CREATE TABLE work_orders_partitioned (
    LIKE work_orders INCLUDING ALL
) PARTITION BY RANGE (created_at);

-- Criar partições por trimestre
CREATE TABLE work_orders_2025_q1 PARTITION OF work_orders_partitioned
FOR VALUES FROM ('2025-01-01') TO ('2025-04-01');

CREATE TABLE work_orders_2025_q2 PARTITION OF work_orders_partitioned
FOR VALUES FROM ('2025-04-01') TO ('2025-07-01');

-- Queries automaticamente usam partição correta
-- SELECT * FROM work_orders WHERE created_at >= '2025-02-01';
-- (Apenas scannea work_orders_2025_q1)
```

---

### 4. Connection Pooling (Aplicação)

#### HikariCP Configuration (Spring Boot)
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Max 20 conexões simultâneas
      minimum-idle: 5            # Mínimo 5 conexões em idle
      connection-timeout: 30000  # 30s timeout
      idle-timeout: 600000       # 10 min idle timeout
      max-lifetime: 1800000      # 30 min max lifetime
      pool-name: OficinaHikariCP
```

---

## 📈 Métricas de Performance

### Benchmarks Esperados

| Operação | Latência (P95) | Throughput |
|----------|----------------|------------|
| **SELECT customer by CPF** | <5ms | 10k+ QPS |
| **INSERT work_order** | 20-50ms | 1k+ TPS |
| **SELECT work_orders by customer** | <20ms | 5k+ QPS |
| **UPDATE work_order status** | <10ms | 5k+ TPS |
| **Complex JOIN (order + items)** | <50ms | 2k+ QPS |

### Query Optimization Example

#### Antes (Sem Índice)
```sql
-- Query Plan: Seq Scan (lento)
EXPLAIN ANALYZE
SELECT * FROM work_orders WHERE customer_id = '123';

-- Planning Time: 0.1ms
-- Execution Time: 250ms (varredura completa)
```

#### Depois (Com Índice Composto)
```sql
-- Query Plan: Index Scan using idx_work_orders_customer_created
EXPLAIN ANALYZE
SELECT * FROM work_orders 
WHERE customer_id = '123' 
ORDER BY created_at DESC;

-- Planning Time: 0.1ms
-- Execution Time: 5ms (busca por índice)
```

---

## 🔐 Segurança e Compliance

### 1. Encryption at Rest (RDS)
```hcl
resource "aws_db_instance" "oficina_db" {
  storage_encrypted = true
  kms_key_id       = aws_kms_key.rds.arn
}
```

### 2. Encryption in Transit (SSL/TLS)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://oficina-db.aws.com:5432/oficina?sslmode=require
```

### 3. Row-Level Security (RLS) - Futuro
```sql
-- Apenas clientes podem ver seus próprios dados
ALTER TABLE work_orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY customer_isolation ON work_orders
FOR SELECT
USING (customer_id = current_setting('app.current_customer_id')::UUID);
```

---

## 📚 Schema SQL Completo

Confira o schema completo em: `infra-database-terraform/scripts/init-db.sql`

---

## 🔄 Roadmap de Melhorias

### Curto Prazo (3 meses)
- [ ] Implementar views materializadas para dashboards
- [ ] Adicionar índices full-text search em `parts.description`
- [ ] Configurar automated backups com retention de 30 dias

### Médio Prazo (6 meses)
- [ ] Implementar particionamento em `work_orders` (quando >500k registros)
- [ ] Adicionar read replicas para queries de relatórios
- [ ] Implementar Row-Level Security (RLS)

### Longo Prazo (12 meses)
- [ ] Avaliar migração para Aurora PostgreSQL (serverless)
- [ ] Implementar caching com Redis para queries frequentes
- [ ] Considerar sharding horizontal se ultrapassar 10M+ ordens

---

**Documento gerado em**: 2025-12-05  
**Última revisão**: 2025-12-05  
**Próxima revisão**: 2026-06-05
