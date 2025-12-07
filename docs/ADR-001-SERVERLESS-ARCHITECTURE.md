# ADR-001: Arquitetura Serverless vs Containers

**Status**: Aceito  
**Data**: 2025-12-05  
**Decisores**: Edimilson L. Dutra, Equipe de Arquitetura  

---

## 📋 Contexto

Precisamos decidir a arquitetura base para o Sistema de Gestão de Oficina Mecânica, escolhendo entre uma abordagem **Serverless** (AWS Lambda + API Gateway) ou **Container-based** (EKS + Kubernetes).

### Cenário do Projeto
- **Autenticação**: Fluxo simples de validação de CPF
- **APIs de Negócio**: CRUD de clientes, veículos, ordens de serviço
- **Tráfego**: Variável (picos em horário comercial, ocioso à noite)
- **Equipe**: 2-3 desenvolvedores (conhecimento em Spring Boot e Java)

---

## ⚖️ Decisão

**Escolhemos uma arquitetura HÍBRIDA**:

1. **Serverless (AWS Lambda)** para:
   - Serviço de autenticação (validação de CPF)
   - Endpoints com tráfego altamente variável

2. **Containers (EKS)** para:
   - APIs de negócio (Spring Boot)
   - Aplicações com dependências complexas
   - Workloads de longa duração

---

## 🎯 Justificativa

### Por que Serverless para Autenticação?

#### ✅ Prós
1. **Escalabilidade Automática**
   - Lambda escala de 0 a 1000+ invocações instantaneamente
   - Sem provisionamento de instâncias

2. **Custo**
   - Pay-per-invocation: $0.20 por 1M de requests
   - Sem custos em idle (noites, finais de semana)
   - Estimativa: $2-5/mês

3. **Simplicidade**
   - Deploy via AWS SAM (Infrastructure as Code)
   - Zero gerenciamento de servidores
   - Cold start <1s aceitável para autenticação

4. **Resiliência**
   - Multi-AZ por padrão
   - Retry automático
   - Dead Letter Queue (DLQ) para erros

#### ❌ Contras
1. **Cold Start**: 500ms-1s (mitigado com provisioned concurrency)
2. **Timeout Limite**: 15 minutos máximo
3. **Vendor Lock-in**: Específico da AWS

---

### Por que Containers para APIs de Negócio?

#### ✅ Prós
1. **Portabilidade**
   - Docker images podem rodar em qualquer cloud
   - Fácil migração entre provedores

2. **Desenvolvimento Local**
   - Desenvolvedores rodam ambiente completo localmente
   - docker-compose para dependencies (PostgreSQL, Redis)

3. **Controle Granular**
   - Configuração de CPU, memória, recursos
   - Health checks customizados
   - Graceful shutdown

4. **Workloads Complexas**
   - Spring Boot com múltiplas dependências (JPA, caching)
   - Processos de background (scheduled tasks)
   - WebSockets (se necessário no futuro)

5. **Observabilidade**
   - Logs estruturados
   - Métricas customizadas (Prometheus)
   - Tracing distribuído (Jaeger/X-Ray)

#### ❌ Contras
1. **Custo Fixo**: ~$220/mês para cluster EKS + worker nodes
2. **Complexidade**: Kubernetes tem curva de aprendizado
3. **Gerenciamento**: Patches de segurança, updates

---

## 📊 Comparação Técnica

| Aspecto | Serverless (Lambda) | Containers (EKS) |
|---------|---------------------|------------------|
| **Cold Start** | 500ms-1s | N/A (sempre rodando) |
| **Custo Fixo** | $0 | $220/mês |
| **Custo Variável** | $0.20/1M req | N/A |
| **Escalabilidade** | Automática (0-1000+) | HPA (2-10 pods) |
| **Latência** | 10-50ms | 5-20ms |
| **Max Timeout** | 15 min | Infinito |
| **Portabilidade** | Baixa (AWS only) | Alta (multi-cloud) |
| **Complexidade** | Baixa | Média-Alta |
| **Local Dev** | Difícil (emuladores) | Fácil (Docker) |

---

## 🏗️ Arquitetura Final

```
┌────────────────────────────────────────────────────────────┐
│                      CLIENT (Browser/Mobile)                │
└────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌────────────────────────────────────────────────────────────┐
│                    API Gateway (HTTP API)                   │
│  - /auth/validate      → Lambda (Serverless)                │
│  - /auth/refresh       → Lambda (Serverless)                │
│  - /api/v1/*          → ALB → EKS (Containers)             │
└────────────────────────────────────────────────────────────┘
         │                                    │
         │ (Serverless)                       │ (Containers)
         ▼                                    ▼
┌─────────────────────┐        ┌────────────────────────────┐
│  Lambda Auth        │        │  EKS Cluster               │
│  - CPF validation   │        │  ┌──────────────────────┐  │
│  - JWT generation   │        │  │ Spring Boot Pods     │  │
│  - Token refresh    │        │  │ - Customers API      │  │
│                     │        │  │ - Vehicles API       │  │
│  Triggers:          │        │  │ - Work Orders API    │  │
│  - API Gateway      │        │  └──────────────────────┘  │
│  - CloudWatch Events│        │                            │
│                     │        │  HPA: 2-10 replicas        │
│  Timeout: 30s       │        │  CPU: 70%, Memory: 80%     │
│  Memory: 512 MB     │        │                            │
└─────────────────────┘        └────────────────────────────┘
         │                                    │
         └────────────────┬───────────────────┘
                          ▼
                 ┌────────────────┐
                 │  RDS PostgreSQL│
                 │  (Multi-AZ)    │
                 └────────────────┘
```

---

## 🔍 Alternativas Consideradas

### Alternativa 1: Serverless Full (Lambda + API Gateway)

#### ✅ Prós
- Custo extremamente baixo ($10-20/mês)
- Escalabilidade infinita
- Zero gerenciamento

#### ❌ Contras
- Cold start impacta UX (~500ms-1s)
- Timeout de 15 minutos (limita processos longos)
- Difícil desenvolvimento local
- Complexo para workloads stateful

**Motivo da Rejeição**: APIs de negócio precisam de latência consistente (<50ms) e desenvolvimento local eficiente.

---

### Alternativa 2: Containers Full (EKS apenas)

#### ✅ Prós
- Arquitetura uniforme
- Latência consistente
- Desenvolvimento local fácil

#### ❌ Contras
- Custo fixo alto ($220/mês mesmo sem uso)
- Over-provisioning para workloads variáveis
- Complexidade desnecessária para autenticação simples

**Motivo da Rejeição**: Autenticação tem tráfego muito variável (picos de 100x), desperdiçando recursos em idle.

---

### Alternativa 3: Monolito (Single EC2)

#### ✅ Prós
- Simplicidade extrema
- Custo baixo ($30/mês - t3.medium)
- Fácil debugar

#### ❌ Contras
- Escalabilidade limitada (vertical only)
- Single point of failure
- Deploy de risco (downtime)
- Não cloud-native

**Motivo da Rejeição**: Não atende requisitos de alta disponibilidade e escalabilidade.

---

## 📈 Consequências

### Positivas ✅
1. **Custo Otimizado**: Serverless para workloads variáveis, containers para previsíveis
2. **Melhor UX**: Latência baixa nas APIs principais (containers)
3. **Escalabilidade**: Lambda auto-scaling + HPA no Kubernetes
4. **Desenvolvimento Produtivo**: Docker local para APIs, AWS SAM para Lambda
5. **Portabilidade Parcial**: APIs podem migrar para GCP/Azure facilmente

### Negativas ❌
1. **Complexidade**: Dois modelos de deploy (SAM + Kubernetes)
2. **Curva de Aprendizado**: Equipe precisa dominar Lambda E Kubernetes
3. **Debugging**: Mais difícil debugar interações entre Lambda e EKS
4. **Monitoramento**: Precisa de ferramentas para ambos (CloudWatch + Container Insights)

### Riscos ⚠️
1. **Latência Cross-Service**: Lambda → EKS pode adicionar 10-20ms
2. **Custo Inesperado**: Lambda pode escalar demais em ataques DDoS
3. **Vendor Lock-in Parcial**: Lambda é específico da AWS

---

## 🚀 Plano de Implementação

### Fase 1: Serverless Auth (Semana 1-2)
- [ ] Criar Lambda function em Java 21
- [ ] Configurar API Gateway HTTP API
- [ ] Deploy via AWS SAM
- [ ] Testes de carga (1000 req/s)

### Fase 2: Container APIs (Semana 3-5)
- [ ] Dockerizar Spring Boot app
- [ ] Criar cluster EKS com Terraform
- [ ] Deploy com Kubernetes manifests
- [ ] Configurar HPA e Cluster Autoscaler

### Fase 3: Integração (Semana 6)
- [ ] Configurar API Gateway para rotear para ALB
- [ ] Testes end-to-end
- [ ] Monitoramento integrado (CloudWatch + Container Insights)

### Fase 4: Otimizações (Semana 7-8)
- [ ] Provisioned concurrency para Lambda (reduzir cold starts)
- [ ] Tuning de HPA (CPU/Memory thresholds)
- [ ] Caching de secrets (Secrets Manager)

---

## 🔄 Revisão

Esta decisão será **reavaliada em 12 meses** ou se:
- Custo mensal ultrapassar $400 (30% acima do esperado)
- Cold start de Lambda causar >5% de reclamações de usuários
- Equipe solicitar consolidação em uma arquitetura única

---

## 📚 Referências

- [AWS Lambda Best Practices](https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html)
- [EKS Best Practices Guide](https://aws.github.io/aws-eks-best-practices/)
- [Serverless vs Containers: When to Use Each](https://aws.amazon.com/blogs/compute/choosing-between-aws-lambda-and-amazon-ecs/)
- [The Serverless Spectrum](https://read.acloud.guru/the-serverless-spectrum-147b02cb2292)

---

**Status**: Aceito  
**Data de Decisão**: 2025-12-05  
**Última Revisão**: 2025-12-05  
**Próxima Revisão**: 2026-12-05
