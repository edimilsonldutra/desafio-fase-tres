# ============================================================================
# Multi-stage Dockerfile para AWS Lambda com Java 21
# Gera imagem compatível com AWS Lambda Container Image
# ============================================================================

# ============================================================================
# Stage 1: Build da aplicação Java
# ============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Define o ambiente do container para usar UTF-8
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Copiar POM primeiro para cache de dependências
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copiar código fonte e compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Verificar se o JAR foi criado
RUN ls -lh target/*.jar

# ============================================================================
# Stage 2: Imagem Lambda Runtime com Java 21
# ============================================================================
FROM public.ecr.aws/lambda/java:21

# Metadata
LABEL maintainer="FIAP"
LABEL description="Lambda Auth Service - Autenticação e Autorização"
LABEL version="1.0"

# Copiar o JAR compilado do stage de build
COPY --from=build /build/target/*.jar ${LAMBDA_TASK_ROOT}/lib/

# Definir o handler da função Lambda
CMD [ "lambdavalida.ValidaPessoaFunction::handleRequest" ]

