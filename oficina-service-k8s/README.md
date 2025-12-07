# ⚙️ Oficina Service - Aplicação Spring Boot Kubernetes

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.13-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://openjdk.org/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.28-326CE5?logo=kubernetes)](https://kubernetes.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql)](https://www.postgresql.org/)

Aplicação Spring Boot 3.3.13 para gestão de oficina mecânica, executando em Amazon EKS com arquitetura cloud-native e práticas de DevOps.

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [APIs Disponíveis](#-apis-disponíveis)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração](#-configuração)
- [Deploy](#-deploy)
- [Desenvolvimento](#-desenvolvimento)
- [Testes](#-testes)
- [Monitoramento](#-monitoramento)
- [Troubleshooting](#-troubleshooting)

## 🎯 Visão Geral

Sistema completo de gestão de oficina mecânica com arquitetura de microserviços, executando em Kubernetes (EKS) com integração a serviços AWS.

### Características Principais

- ✅ **Spring Boot 3.3.13**: Framework moderno com Java 21
- ✅ **PostgreSQL 15**: Banco de dados RDS Multi-AZ
- ✅ **Kubernetes Ready**: Manifests completos com HPA, Ingress, ConfigMaps
- ✅ **Cloud Native**: Integração com AWS Secrets Manager, CloudWatch
- ✅ **API RESTful**: 5 endpoints principais com Swagger/OpenAPI 3.0
- ✅ **Observabilidade**: Actuator, Micrometer, logs estruturados
- ✅ **CI/CD**: GitHub Actions com deploy automático multi-ambiente
- ✅ **Escalabilidade**: HPA configurado para CPU e memória

## 🏗️ Arquitetura

### Diagrama de Componentes
kubectl apply -k k8s/overlays/dev/

# Verificar pods
kubectl get pods -n oficina

# Acessar logs
kubectl logs -f deployment/oficina-service -n oficina

# Port forward
kubectl port-forward svc/oficina-service 8080:80 -n oficina
```

### AWS EKS (CI/CD)

```bash
# Via GitHub Actions (automático)
# Push para branch main → Deploy automático

# Ou manual via kubectl
aws eks update-kubeconfig --name oficina-eks-prod --region us-east-1
kubectl apply -k k8s/overlays/prod/
```

## 🔧 Configuração

### Variáveis de Ambiente

```yaml
# Database (from RDS)
DB_HOST: <rds-endpoint>
DB_PORT: 5432
DB_NAME: oficina_db
DB_USERNAME: <from-secret>
DB_PASSWORD: <from-secret>

# API Gateway
API_GATEWAY_URL: https://api.oficina.com
LAMBDA_AUTH_ENDPOINT: https://api.oficina.com/auth

# External APIs
APROVACAO_ORCAMENTO_API_URL: http://api-aprovacao-orcamento:8080

# Spring Profiles
SPRING_PROFILES_ACTIVE: prod

# Logging
LOG_LEVEL: INFO

# JVM Options
JAVA_OPTS: -Xms256m -Xmx512m -XX:+UseG1GC
```

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: oficina-config
data:
  application.properties: |
    spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    spring.jpa.hibernate.ddl-auto=validate
    spring.jpa.show-sql=false
    server.port=8080
```

### Secrets (External Secrets)

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: oficina-db-secret
spec:
  secretStoreRef:
    name: aws-secretsmanager
    kind: SecretStore
  target:
    name: oficina-db-credentials
  data:
    - secretKey: username
      remoteRef:
        key: oficina-db-credentials
        property: username
    - secretKey: password
      remoteRef:
        key: oficina-db-credentials
        property: password
```

## 📊 Kubernetes Resources

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: oficina-service
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    spec:
      containers:
      - name: oficina
        image: <ecr-registry>/oficina-service:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            cpu: 250m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 1Gi
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 90
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 5
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: oficina-service
spec:
  type: ClusterIP
  ports:
    - port: 80
      targetPort: 8080
      protocol: TCP
  selector:
    app: oficina
```

### HPA (Horizontal Pod Autoscaler)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: oficina-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: oficina-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 30
      - type: Pods
        value: 2
        periodSeconds: 30
```

### Ingress (ALB)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: oficina-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internal
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/healthcheck-path: /actuator/health
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}]'
spec:
  rules:
  - http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: oficina-service
            port:
              number: 80
```

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow

```yaml
name: Deploy to EKS

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'corretto'

      - name: Build with Maven
        run: mvn clean package -DskipTests

      - name: Run Tests
        run: mvn test

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/oficina-service:$IMAGE_TAG .
          docker push $ECR_REGISTRY/oficina-service:$IMAGE_TAG
          docker tag $ECR_REGISTRY/oficina-service:$IMAGE_TAG $ECR_REGISTRY/oficina-service:latest
          docker push $ECR_REGISTRY/oficina-service:latest

      - name: Update kubeconfig
        run: aws eks update-kubeconfig --name oficina-eks-prod --region us-east-1

      - name: Deploy to EKS
        run: |
          kubectl set image deployment/oficina-service oficina=$ECR_REGISTRY/oficina-service:$IMAGE_TAG -n oficina
          kubectl rollout status deployment/oficina-service -n oficina
```

## 📈 Monitoramento

### Metrics Exportadas

- **JVM Metrics**: Heap, threads, GC
- **HTTP Metrics**: Request count, latency, errors
- **Database Metrics**: Connection pool, query time
- **Custom Metrics**: Business metrics

### Health Checks

```bash
# Liveness
GET /actuator/health/liveness

# Readiness
GET /actuator/health/readiness

# Metrics (Prometheus)
GET /actuator/prometheus
```

### Logs

```bash
# CloudWatch Logs (via Fluent Bit)
# Log Group: /aws/eks/oficina-cluster/application

# Visualizar logs
kubectl logs -f deployment/oficina-service -n oficina

# Logs estruturados (JSON)
{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "c.g.o.controller.ClienteController",
  "message": "Cliente criado com sucesso",
  "clienteId": "12345"
}
```

## 🔐 Security

- ✅ HTTPS via ALB
- ✅ Secrets via External Secrets Operator
- ✅ Network Policies
- ✅ Pod Security Standards
- ✅ RBAC habilitado
- ✅ Security Context configurado
- ✅ Vulnerability scanning (Trivy)
- ✅ OWASP Dependency Check

## 🏷️ Testes

### Testes Unitários
```bash
mvn test
```

### Testes de Integração
```bash
mvn verify -P integration-tests
```

### Testes de Carga (K6)
```bash
k6 run k6/load-test.js
```

## 📝 Documentação API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## 🔗 Dependências Externas

1. **Lambda Auth Service**: Validação de CPF
2. **RDS PostgreSQL**: Banco de dados
3. **API Aprovação Orçamento**: Workflow de aprovação
4. **AWS Secrets Manager**: Credenciais
5. **API Gateway**: Roteamento externo
