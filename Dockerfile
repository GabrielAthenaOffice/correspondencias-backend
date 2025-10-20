# ==========================================
# Etapa 1 — Build da aplicação
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o pom.xml e baixa dependências (cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o restante do código
COPY src ./src

# Gera o JAR final (sem testes, para agilizar build)
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2 — Runtime (container leve)
# ==========================================
FROM eclipse-temurin:21-jre

# Diretório da aplicação
WORKDIR /app

# Copia apenas o JAR do build anterior
COPY --from=build /app/target/*.jar app.jar

# Define a variável de ambiente do profile
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
