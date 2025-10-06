# Etapa 1 — Build do projeto com Maven
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Garante que o Maven e o Java usem UTF-8 (evita o erro MalformedInputException)
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

COPY pom.xml .
COPY src ./src

# Faz o build sem rodar testes
RUN mvn clean package -DskipTests

# Etapa 2 — Imagem final para execução
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia o jar gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Executa o aplicativo
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
