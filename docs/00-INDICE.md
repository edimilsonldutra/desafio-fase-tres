# 📚 Índice de Documentação Arquitetural

**Projeto**: Sistema de Gestão de Oficina Mecânica  
**Data**: 2025-12-05  
**Versão**: 1.0  

---

## 📋 Visão Geral

Esta pasta contém toda a documentação arquitetural do projeto, incluindo diagramas, decisões técnicas, justificativas e guias de referência.

---

## 📁 Estrutura de Documentos

### 🎨 Diagramas

#### [DIAGRAMA-COMPONENTES.md](./DIAGRAMA-COMPONENTES.md)
**Tipo**: Diagrama de Arquitetura  
**Descrição**: Visualização completa da arquitetura cloud com todos os componentes AWS  
**Conteúdo**:
- Diagrama de componentes em ASCII (7 camadas)
- Camadas: Edge, Serverless, Microservices, Data, Observability, Network, CI/CD
- Descrição detalhada de cada componente
- Fluxos de dados para autenticação e APIs de negócio
- Configurações de segurança, escalabilidade e monitoramento
- Estimativa de custos ($568/mês)
- Benefícios da arquitetura

**Quando usar**: Para entender a arquitetura geral do sistema e como os componentes se integram

---

#### [DIAGRAMA-SEQUENCIA.md](./DIAGRAMA-SEQUENCIA.md)
**Tipo**: Diagramas de Sequência  
**Descrição**: Fluxos detalhados de interação entre componentes  
**Conteúdo**:
- **Fluxo 1**: Autenticação de Cliente via CPF
  - Validação WAF → API Gateway → Lambda → RDS
  - Geração de JWT token
  - Casos de erro (CPF inválido, cliente não encontrado)
  
- **Fluxo 2**: Criação de Ordem de Serviço
  - Validação JWT → ALB → Ingress → Pod → RDS
  - Transação atômica (12 steps)
  - Validações de negócio (cliente, veículo, estoque)
  - Garantias de consistência (ACID, locks)
  
- **Fluxo 3**: Aprovação de Orçamento
  - Integração com API externa
  - Atualização de status

- Métricas de performance (latência P95, P99)
- Considerações de segurança e otimizações

**Quando usar**: Para entender como as requisições fluem pelo sistema e quais validações ocorrem

---

### 📝 RFCs (Request for Comments)

#### [RFC-001-ESCOLHA-CLOUD.md](./RFC-001-ESCOLHA-CLOUD.md)
**Tipo**: Decisão Técnica  
**Descrição**: Escolha do provedor cloud (AWS vs Azure vs GCP)  
**Conteúdo**:
- Análise comparativa de 3 provedores
- Matriz de decisão ponderada (8 critérios)
- Estimativas de custo detalhadas
- Justificativa para escolha da AWS (score 8.65/10)
- Plano de implementação (8 semanas)
- Métricas de sucesso
- Estratégia de saída (evitar lock-in)

**Resultado**: AWS escolhida por maturidade, custo-benefício ($359/mês) e ecossistema

---

#### [RFC-002-ESCOLHA-DATABASE.md](./RFC-002-ESCOLHA-DATABASE.md)
**Tipo**: Decisão Técnica  
**Descrição**: Escolha do banco de dados (PostgreSQL vs MySQL vs DynamoDB vs MongoDB)  
**Conteúdo**:
- Requisitos funcionais e não-funcionais
- Análise comparativa de 4 opções
- Matriz de decisão ponderada (7 critérios)
- Justificativa para PostgreSQL 15 (score 8.25/10)
- Schema relacional com 8 tabelas
- Configuração RDS Multi-AZ
- Estimativa de custo ($152.50/mês)
- Estratégias de escalabilidade futura (read replicas, sharding)

**Resultado**: PostgreSQL 15 escolhido por ACID completo, relacionamentos complexos e custo-benefício

---

#### [RFC-003-ESTRATEGIA-AUTENTICACAO.md](./RFC-003-ESTRATEGIA-AUTENTICACAO.md)
**Tipo**: Decisão Técnica  
**Descrição**: Estratégia de autenticação e autorização (JWT vs OAuth 2.0 vs API Keys vs Sessions)  
**Conteúdo**:
- Requisitos de segurança e negócio
- Análise comparativa de 4 estratégias
- Matriz de decisão ponderada (6 critérios)
- Justificativa para JWT (score 8.45/10)
- Estrutura do access token (1 hora) e refresh token (7 dias)
- Implementação em Java (Lambda + Custom Authorizer)
- Mitigação de riscos (revogação, leakage, secret compromise)
- Plano de implementação (4 semanas)
- Roadmap futuro (RS256, MFA, rate limiting)

**Resultado**: JWT escolhido por ser stateless, escalável e custo-benefício ($0.40/mês)

---

### 🏛️ ADRs (Architecture Decision Records)

#### [ADR-001-SERVERLESS-ARCHITECTURE.md](./ADR-001-SERVERLESS-ARCHITECTURE.md)
**Status**: Aceito  
**Descrição**: Decisão entre arquitetura Serverless vs Containers  
**Conteúdo**:
- Contexto do projeto (autenticação + APIs de negócio)
- **Decisão**: Arquitetura HÍBRIDA
  - Serverless (Lambda) para autenticação
  - Containers (EKS) para APIs de negócio
- Comparação técnica (10 aspectos)
- Diagrama da arquitetura final
- Alternativas consideradas e rejeitadas (Serverless Full, Containers Full, Monolito)
- Consequências positivas e negativas
- Plano de implementação (8 semanas)
- Critérios de reavaliação (12 meses)

**Justificativa**: Otimização de custos (serverless para variável, containers para previsível) e melhor UX

---

#### [ADR-002-API-GATEWAY-SYNC.md](./ADR-002-API-GATEWAY-SYNC.md)
**Status**: Aceito  
**Descrição**: Decisão entre comunicação Síncrona vs Assíncrona  
**Conteúdo**:
- Cenários de comunicação (auth, business APIs, notificações)
- **Decisão**: HTTP REST (síncrona) como padrão + Event-Driven para casos específicos
  - Síncrona: Autenticação, business APIs, aprovação de orçamento
  - Assíncrona: Notificações, auditoria, relatórios
- Diagramas de fluxos síncronos e assíncronos
- Comparação detalhada (7 aspectos)
- Implementações em código (Spring Boot + SNS/SQS/Lambda)
- Alternativas consideradas (Event-Driven Full, GraphQL, gRPC)
- Consequências e riscos
- Plano de implementação (7 semanas)

**Justificativa**: REST para simplicidade e consistência; eventos para desacoplamento onde apropriado

---

### 🗄️ Banco de Dados

#### [JUSTIFICATIVA-BANCO-DADOS.md](./JUSTIFICATIVA-BANCO-DADOS.md)
**Tipo**: Justificativa Formal + Diagrama ER  
**Descrição**: Escolha e modelagem completa do banco de dados  
**Conteúdo**:
- **Análise Comparativa**: PostgreSQL vs MySQL vs DynamoDB vs MongoDB (12 critérios)
- **Diagrama ER Completo**: 8 tabelas com relacionamentos detalhados
  - `customers` (1:N) `vehicles` (1:N) `work_orders`
  - `work_orders` (M:N) `services` via `work_order_services`
  - `work_orders` (M:N) `parts` via `work_order_parts`
- **Relacionamentos Detalhados**: Foreign keys, cascades, constraints
- **Normalização**: 3NF (Terceira Forma Normal)
- **Estratégia de Indexação**:
  - Primary keys (UUID)
  - Unique constraints (CPF, email, placa)
  - Foreign keys (performance de joins)
  - Índices compostos (queries complexas)
  - Full-text search (pg_trgm)
  - Índices parciais (otimização de espaço)
- **Otimizações de Performance**:
  - Triggers automáticos (estoque, totais)
  - Views materializadas (dashboards)
  - Particionamento por data (preparação futura)
  - Connection pooling (HikariCP)
- **Benchmarks**: Latência P95 e throughput esperados
- **Segurança**: Encryption at rest/transit, RLS (futuro)
- **Roadmap de Melhorias**: Curto, médio e longo prazo

**Resultado**: PostgreSQL 15 em RDS Multi-AZ com schema otimizado para OLTP

---

## 🗺️ Mapa de Navegação

### Por Tipo de Informação

#### Quero entender a arquitetura geral
1. Comece com [DIAGRAMA-COMPONENTES.md](./DIAGRAMA-COMPONENTES.md)
2. Leia [ADR-001-SERVERLESS-ARCHITECTURE.md](./ADR-001-SERVERLESS-ARCHITECTURE.md)

#### Quero entender os fluxos de requisição
1. Leia [DIAGRAMA-SEQUENCIA.md](./DIAGRAMA-SEQUENCIA.md)
2. Complemente com [ADR-002-API-GATEWAY-SYNC.md](./ADR-002-API-GATEWAY-SYNC.md)

#### Quero entender decisões de infraestrutura
1. Cloud: [RFC-001-ESCOLHA-CLOUD.md](./RFC-001-ESCOLHA-CLOUD.md)
2. Banco de Dados: [RFC-002-ESCOLHA-DATABASE.md](./RFC-002-ESCOLHA-DATABASE.md)
3. Autenticação: [RFC-003-ESTRATEGIA-AUTENTICACAO.md](./RFC-003-ESTRATEGIA-AUTENTICACAO.md)

#### Quero entender o banco de dados
1. Leia [JUSTIFICATIVA-BANCO-DADOS.md](./JUSTIFICATIVA-BANCO-DADOS.md)
2. Consulte [RFC-002-ESCOLHA-DATABASE.md](./RFC-002-ESCOLHA-DATABASE.md) para contexto da decisão

#### Quero implementar algo
1. Autenticação: [RFC-003-ESTRATEGIA-AUTENTICACAO.md](./RFC-003-ESTRATEGIA-AUTENTICACAO.md) (implementação completa)
2. APIs: [ADR-002-API-GATEWAY-SYNC.md](./ADR-002-API-GATEWAY-SYNC.md) (exemplos de código)
3. Banco: [JUSTIFICATIVA-BANCO-DADOS.md](./JUSTIFICATIVA-BANCO-DADOS.md) (schema SQL completo)

---

## 📊 Estatísticas dos Documentos

| Documento | Páginas | Diagramas | Código | Tabelas |
|-----------|---------|-----------|--------|---------|
| DIAGRAMA-COMPONENTES.md | ~25 | 3 | 0 | 1 |
| DIAGRAMA-SEQUENCIA.md | ~15 | 3 | 0 | 4 |
| RFC-001-ESCOLHA-CLOUD.md | ~12 | 0 | 1 | 2 |
| RFC-002-ESCOLHA-DATABASE.md | ~18 | 1 | 3 | 2 |
| RFC-003-ESTRATEGIA-AUTENTICACAO.md | ~20 | 0 | 10 | 2 |
| ADR-001-SERVERLESS-ARCHITECTURE.md | ~15 | 1 | 1 | 1 |
| ADR-002-API-GATEWAY-SYNC.md | ~18 | 2 | 4 | 2 |
| JUSTIFICATIVA-BANCO-DADOS.md | ~22 | 1 | 15 | 3 |
| **TOTAL** | **~145** | **11** | **34** | **17** |

---

## 🔍 Glossário

| Termo | Definição |
|-------|-----------|
| **RFC** | Request for Comments - Documento de decisão técnica com análise comparativa |
| **ADR** | Architecture Decision Record - Registro permanente de decisão arquitetural |
| **ACID** | Atomicity, Consistency, Isolation, Durability - Propriedades de transações |
| **Multi-AZ** | Multi-Availability Zone - Distribuição em múltiplas zonas de disponibilidade |
| **HPA** | Horizontal Pod Autoscaler - Escalador automático de pods Kubernetes |
| **JWT** | JSON Web Token - Token de autenticação stateless |
| **ER** | Entity-Relationship - Modelo de entidades e relacionamentos |
| **3NF** | Third Normal Form - Terceira forma normal de normalização |
| **pg_trgm** | PostgreSQL Trigram - Extensão para full-text search |
| **RLS** | Row-Level Security - Segurança em nível de linha |

---

## 📅 Histórico de Revisões

| Data | Versão | Autor | Alterações |
|------|--------|-------|------------|
| 2025-12-05 | 1.0 | Edimilson L. Dutra | Criação inicial com 8 documentos |
| 2025-12-07 | 1.1 | Sistema | Adição de Monitoramento e Observabilidade com New Relic |

---

## 📊 Monitoramento e Observabilidade

#### [MONITORAMENTO-OBSERVABILIDADE.md](./MONITORAMENTO-OBSERVABILIDADE.md)
**Tipo**: Guia de Implementação  
**Descrição**: Documentação completa da solução de Monitoramento e Observabilidade com New Relic  
**Conteúdo**:
- Configuração do New Relic APM para Java
- Logs estruturados em JSON com correlação de requisições
- Métricas customizadas de negócio (Ordens de Serviço)
- Monitoramento de Kubernetes (CPU, memória, pods)
- Health checks e probes
- Alertas configurados (latência, recursos, erros)
- Dashboards customizados
- Guia de instalação completo
- Troubleshooting e queries NRQL

**Quando usar**: Para configurar, entender ou troubleshoot o monitoramento do sistema

---

#### [NRQL-QUERIES.md](./NRQL-QUERIES.md)
**Tipo**: Referência Rápida  
**Descrição**: Coleção de queries NRQL úteis para New Relic  
**Conteúdo**:
- Queries de performance (throughput, latência, Apdex)
- Métricas de Ordens de Serviço
- Análise de erros e falhas
- Monitoramento de integrações externas
- Performance de banco de dados
- Métricas de Kubernetes
- Health checks
- Logs estruturados
- SLA e uptime

**Quando usar**: Para criar dashboards, alertas ou investigar problemas

---

#### [newrelic-alerts-config.yml](./newrelic-alerts-config.yml)
**Tipo**: Configuração  
**Descrição**: Políticas de alertas do New Relic em formato YAML  
**Conteúdo**:
- Alertas de latência de APIs
- Alertas de consumo de recursos (CPU, memória)
- Alertas de health checks e disponibilidade
- Alertas de processamento de Ordens de Serviço
- Alertas de performance de banco de dados
- Canais de notificação (Email, Slack, PagerDuty)

**Quando usar**: Para importar ou atualizar políticas de alertas no New Relic

---

#### [newrelic-dashboard.json](./newrelic-dashboard.json)
**Tipo**: Configuração  
**Descrição**: Definição de dashboards customizados do New Relic  
**Conteúdo**:
- Dashboard Overview (performance geral)
- Dashboard Ordens de Serviço (métricas de negócio)
- Dashboard Integrações e Erros
- Dashboard Database Performance
- Widgets com queries NRQL configuradas

**Quando usar**: Para importar dashboards prontos no New Relic

---

#### [install-newrelic.sh](./install-newrelic.sh) / [install-newrelic.ps1](./install-newrelic.ps1)
**Tipo**: Script de Instalação  
**Descrição**: Scripts automatizados para instalação do New Relic no Kubernetes  
**Conteúdo**:
- Criação de namespaces
- Configuração de secrets
- Deploy do New Relic Infrastructure DaemonSet
- Deploy do Kube State Metrics
- Verificação de status

**Quando usar**: Para instalação inicial ou reinstalação do New Relic

---

## 📞 Contato

Para dúvidas ou sugestões sobre esta documentação, entre em contato com a equipe de arquitetura.

---

**Gerado em**: 2025-12-05  
**Última atualização**: 2025-12-07
