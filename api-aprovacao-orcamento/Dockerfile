# Estágio 1: Build da Aplicação com Maven e JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Define o ambiente do container para usar UTF-8
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Copia o arquivo de definição do projeto primeiro
COPY pom.xml .

# Baixa todas as dependências do projeto
RUN mvn dependency:go-offline

# Copia todo o código-fonte da aplicação para o container
COPY src ./src

# Compila a aplicação e empacota-a num arquivo .jar
RUN mvn package -DskipTests


# Estágio 2: Criação da Imagem Final de Execução
FROM eclipse-temurin:21-jre-alpine

# Instala curl para o healthcheck funcionar
RUN apk add --no-cache curl unzip

# Define o diretório de trabalho na imagem final
WORKDIR /app

# Download e instalação do New Relic Java Agent
RUN curl -L -o newrelic-java.zip https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip && \
    rm newrelic-java.zip

# Copia apenas o arquivo .jar compilado do estágio 'build' para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Copia a configuração do New Relic
COPY src/main/resources/newrelic.yml newrelic/newrelic.yml

# Expõe a porta da aplicação Spring Boot
EXPOSE 8080

# Instrução para iniciar o app com New Relic Agent
CMD ["java", "-javaagent:/app/newrelic/newrelic.jar", "-jar", "app.jar"]

