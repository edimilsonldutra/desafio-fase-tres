# RFC-001: Escolha de Provedor Cloud

**Status**: Aprovado  
**Data**: 2025-12-05  
**Autor**: Edimilson L. Dutra  
**Revisores**: Equipe de Arquitetura  

---

## 📋 Sumário Executivo

Este RFC documenta a decisão técnica sobre qual provedor cloud utilizar para hospedar o Sistema de Gestão de Oficina Mecânica.

---

## 🎯 Problema

Precisamos escolher um provedor cloud que atenda aos seguintes requisitos:

1. **Alta Disponibilidade**: Uptime SLA de 99.9% ou superior
2. **Escalabilidade**: Suporte a crescimento de 1000 para 100000+ clientes
3. **Segurança**: Certificações de compliance (SOC 2, ISO 27001)
4. **Custo-Benefício**: Otimização de custos com previsibilidade
5. **Ecossistema**: Ferramentas maduras para Kubernetes, Serverless, Databases
6. **Suporte**: Documentação robusta e suporte técnico em português

---

## 🔍 Opções Avaliadas

### Opção 1: AWS (Amazon Web Services)

#### ✅ Prós
- **Maturidade**: Líder de mercado com 18 anos de experiência
- **Serviços**: Mais de 200 serviços disponíveis
- **EKS**: Kubernetes gerenciado com integrações nativas (ALB, EBS, IAM)
- **Lambda**: Serverless maduro com cold start <1s
- **RDS**: PostgreSQL Multi-AZ com backups automáticos
- **Ecossistema**: Ampla comunidade e bibliotecas
- **Regiões**: Presença em São Paulo (latência <20ms para Brasil)
- **Pricing**: Free tier generoso para POC/desenvolvimento
- **Documentação**: Extensa em português

#### ❌ Contras
- **Complexidade**: Curva de aprendizado íngreme
- **Lock-in**: Serviços proprietários (API Gateway, Secrets Manager)
- **Custo**: Pode ficar caro em escala sem otimização
- **Billing**: Modelo de cobrança complexo (centenas de linhas de itens)

#### 💰 Estimativa de Custo (Produção)
- **Lambda**: 10M invocações/mês × $0.20/1M = $2
- **API Gateway**: 10M requests × $3.50/1M = $35
- **EKS**: Cluster $73 + Worker nodes (3 × t3.medium) = $147
- **RDS**: db.t3.medium Multi-AZ = $120
- **ALB**: 720 horas + 10GB processados = $25
- **CloudWatch**: Logs 10GB + métricas = $30
- **Total**: **~$359/mês**

---

### Opção 2: Azure (Microsoft Azure)

#### ✅ Prós
- **Integração Microsoft**: Ideal se já usamos Office 365/Active Directory
- **AKS**: Kubernetes gerenciado com bom suporte
- **Azure Functions**: Serverless robusto
- **Hybrid Cloud**: Melhor suporte para ambientes híbridos (Azure Arc)
- **Compliance**: Certificações locais no Brasil
- **Suporte**: Presença forte no mercado brasileiro

#### ❌ Contras
- **Performance**: Latência ligeiramente maior que AWS na região SA
- **Ecossistema**: Menor que AWS para algumas ferramentas
- **Documentação**: Menos exemplos práticos que AWS
- **Pricing**: Mais caro que AWS em alguns serviços

#### 💰 Estimativa de Custo (Produção)
- **Azure Functions**: Consumption plan ~$5
- **API Management**: Developer tier ~$50
- **AKS**: Cluster gratuito + VMs (3 × B2s) ~$180
- **Azure Database for PostgreSQL**: General Purpose ~$150
- **Load Balancer**: Standard ~$30
- **Monitor**: Logs + metrics ~$40
- **Total**: **~$455/mês**

---

### Opção 3: GCP (Google Cloud Platform)

#### ✅ Prós
- **GKE**: Melhor Kubernetes gerenciado (criado pelo Google)
- **Cloud Run**: Serverless excelente (containers)
- **Inovação**: Tecnologias de ponta (BigQuery, Vertex AI)
- **Pricing**: Modelo de cobrança mais simples
- **Desconto**: Desconto por uso sustentado automático

#### ❌ Contras
- **Market Share**: Menor presença no Brasil
- **Comunidade**: Menor que AWS/Azure
- **Região**: São Paulo com menos datacenters que AWS
- **Suporte**: Menos material em português
- **Compatibilidade**: Menos integrações de terceiros

#### 💰 Estimativa de Custo (Produção)
- **Cloud Functions**: $0.40/million invocations
- **API Gateway**: ~$30
- **GKE**: Cluster $73 + VMs (3 × e2-medium) ~$120
- **Cloud SQL**: PostgreSQL HA ~$160
- **Cloud Load Balancing**: ~$25
- **Cloud Logging**: ~$25
- **Total**: **~$433/mês**

---

## 📊 Matriz de Decisão

| Critério | Peso | AWS | Azure | GCP |
|----------|------|-----|-------|-----|
| **Maturidade** | 20% | 10 | 8 | 7 |
| **Custo** | 25% | 9 | 7 | 8 |
| **Ecossistema** | 20% | 10 | 7 | 6 |
| **Facilidade de Uso** | 15% | 6 | 7 | 8 |
| **Suporte Local** | 10% | 9 | 9 | 6 |
| **Performance** | 10% | 9 | 8 | 8 |
| **Total** | 100% | **8.65** | **7.55** | **7.25** |

### Cálculo
- **AWS**: (10×0.2) + (9×0.25) + (10×0.2) + (6×0.15) + (9×0.1) + (9×0.1) = **8.65**
- **Azure**: (8×0.2) + (7×0.25) + (7×0.2) + (7×0.15) + (9×0.1) + (8×0.1) = **7.55**
- **GCP**: (7×0.2) + (8×0.25) + (6×0.2) + (8×0.15) + (6×0.1) + (8×0.1) = **7.25**

---

## ✅ Decisão

**Escolhemos AWS (Amazon Web Services)** pelos seguintes motivos:

### Fatores Decisivos

1. **Maturidade e Confiabilidade**
   - 18 anos de experiência
   - SLA de 99.99% para RDS Multi-AZ
   - SLA de 99.95% para EKS

2. **Custo Total de Propriedade**
   - $359/mês estimado (menor que concorrentes)
   - Free tier para desenvolvimento
   - Savings Plans para 30% desconto

3. **Ecossistema Completo**
   - 200+ serviços integrados
   - AWS SAM para Lambda
   - Terraform/Pulumi com suporte maduro
   - Bibliotecas Java/Spring com SDKs oficiais

4. **Performance**
   - Região São Paulo (sa-east-1) com 3 AZs
   - Latência <20ms para usuários no Brasil
   - Edge locations para CloudFront

5. **Segurança e Compliance**
   - SOC 2 Type II
   - ISO 27001
   - PCI DSS Level 1
   - LGPD compliance no Brasil

6. **Suporte e Documentação**
   - Documentação extensa em português
   - AWS Support (Business tier) disponível
   - Comunidade ativa no Brasil

---

## 🚀 Plano de Implementação

### Fase 1: Infraestrutura Base (Semana 1-2)
- [ ] Criar conta AWS com MFA
- [ ] Configurar AWS Organizations
- [ ] Setup de VPC Multi-AZ com Terraform
- [ ] Configurar IAM roles e políticas

### Fase 2: Serviços Core (Semana 3-4)
- [ ] Deploy RDS PostgreSQL Multi-AZ
- [ ] Deploy EKS cluster
- [ ] Configurar Secrets Manager
- [ ] Setup KMS para encryption

### Fase 3: Aplicação (Semana 5-6)
- [ ] Deploy Lambda de autenticação
- [ ] Deploy aplicação Spring Boot no EKS
- [ ] Configurar API Gateway
- [ ] Integrar ALB com Ingress

### Fase 4: Observabilidade (Semana 7)
- [ ] Configurar CloudWatch Logs
- [ ] Setup CloudWatch Container Insights
- [ ] Habilitar X-Ray tracing
- [ ] Criar dashboards e alarmes

### Fase 5: CI/CD (Semana 8)
- [ ] Configurar GitHub Actions
- [ ] Setup de deploy automático
- [ ] Implementar approval gates para produção

---

## 📈 Métricas de Sucesso

Após 3 meses de operação, avaliaremos:

| Métrica | Target | Como Medir |
|---------|--------|------------|
| **Uptime** | >99.9% | CloudWatch synthetics |
| **Latência P95** | <500ms | X-Ray traces |
| **Custo/Usuário** | <$0.50/mês | Cost Explorer |
| **Deploy Frequency** | >1/dia | GitHub Actions metrics |
| **MTTR** | <30min | Incident logs |

---

## 🔄 Revisão e Migração

### Reavaliação
Revisaremos esta decisão a cada 12 meses ou se:
- Custo mensal ultrapassar $600 (67% acima da estimativa)
- Uptime cair abaixo de 99% por 2 meses consecutivos
- Novo provedor oferecer economia >40%

### Estratégia de Saída
Para evitar lock-in total:
- Usar Terraform para IaC (multi-cloud)
- Containerizar aplicações (portabilidade)
- Evitar serviços proprietários onde possível
- Documentar equivalentes em Azure/GCP

---

## 📚 Referências

- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
- [AWS Pricing Calculator](https://calculator.aws/)
- [AWS São Paulo Region](https://aws.amazon.com/pt/local/sao-paulo/)
- [EKS Best Practices](https://aws.github.io/aws-eks-best-practices/)

---

**Aprovado por**: Equipe de Arquitetura  
**Data de Aprovação**: 2025-12-05  
**Próxima Revisão**: 2026-12-05
