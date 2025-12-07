# 🧾 API Aprovação de Orçamento

API REST para aprovação de orçamentos da oficina mecânica.

## 📋 Descrição

API complementar ao sistema principal que permite aprovar ou rejeitar orçamentos de serviços.

## 🚀 Tecnologias

- **Java 17**
- **Spring Boot 3.x**
- **Maven**
- **Docker**

## 🏗️ Estrutura do Projeto

```
api-aprovacao-orcamento/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/grupo99/api_aprovacao_orcamento/
│   │   │       ├── controller/      # Controllers REST
│   │   │       ├── dto/              # Data Transfer Objects
│   │   │       └── ApiAprovacaoOrcamentoApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── Dockerfile                        # Container da aplicação
├── pom.xml                           # Dependências Maven
└── README.md
```

## 🐳 Docker

### Build da Imagem

```bash
docker build -t api-aprovacao-orcamento:latest .
```

### Executar Container

```bash
docker run -p 8081:8081 \
  -e API_PRINCIPAL_URL=http://localhost:8080 \
  api-aprovacao-orcamento:latest
```

## ⚙️ Configuração

### Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SERVER_PORT` | Porta da aplicação | `8081` |
| `API_PRINCIPAL_URL` | URL da API principal | `http://localhost:8080` |

## 🏢 Infraestrutura

A infraestrutura desta API está centralizada no projeto `infra-kubernetes-terraform`.

### Deploy

Para fazer deploy da aplicação:

1. **Build da imagem Docker**:
   ```bash
   docker build -t api-aprovacao-orcamento:latest .
   ```

2. **Push para ECR**:
   ```bash
   # Autenticar no ECR
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
   
   # Tag e push
   docker tag api-aprovacao-orcamento:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/api-aprovacao-orcamento:latest
   docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/api-aprovacao-orcamento:latest
   ```

3. **Deploy via Terraform**:
   ```bash
   cd ../infra-kubernetes-terraform/environments/dev
   terraform apply
   ```

### Arquitetura

A aplicação é deployada como um **Deployment Kubernetes** no cluster EKS compartilhado:

```
┌─────────────────────────────────────┐
│         Amazon EKS Cluster          │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  api-aprovacao-orcamento     │  │
│  │  - Deployment (2-5 replicas) │  │
│  │  - Service (ClusterIP)       │  │
│  │  - HPA (Auto Scaling)        │  │
│  │  - Port: 8081                │  │
│  └──────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
```

### Recursos Kubernetes

- **Namespace**: `default`
- **Replicas**: 2-5 (auto scaling)
- **Container Port**: `8081`
- **Service Port**: `80`
- **Health Checks**: Spring Boot Actuator (`/actuator/health`)
- **Metrics**: Prometheus (`/actuator/prometheus`)

## 📊 Endpoints

### Health Check

```bash
GET /actuator/health
```

### Aprovar Orçamento

```bash
POST /aprovacao/aprovar/{orcamentoId}
```

### Rejeitar Orçamento

```bash
POST /aprovacao/rejeitar/{orcamentoId}
```

## 🔍 Monitoramento

- **Health**: `/actuator/health`
- **Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## 📝 Logs

Logs são enviados para **CloudWatch** via Fluent Bit no cluster EKS.

## 🔗 Links Relacionados

- [Infraestrutura Kubernetes](../infra-kubernetes-terraform/)
- [Oficina Service](../oficina-service-k8s/)
- [Lambda Auth Service](../lambda-auth-service/)
