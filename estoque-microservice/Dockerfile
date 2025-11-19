# Dockerfile para o Microserviço de Estoque
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copiar Maven Wrapper e arquivos de configuração
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY estoque-microservice/pom.xml estoque-microservice/

# Baixar dependências
RUN ./mvnw -f estoque-microservice/pom.xml dependency:go-offline

# Copiar código fonte
COPY estoque-microservice/src estoque-microservice/src

# Compilar aplicação
RUN ./mvnw -f estoque-microservice/pom.xml clean package -DskipTests

# Estágio final - imagem mínima
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Instalar curl para healthcheck
RUN apk add --no-cache curl

# Copiar JAR da aplicação
COPY --from=build /app/estoque-microservice/target/*.jar app.jar

# Expor porta
EXPOSE 8081

# Variáveis de ambiente
ENV SPRING_PROFILES_ACTIVE=prod

# Executar aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
