
# Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
# Click nbfs://nbhost/SystemFileSystem/Templates/Other/Dockerfile to edit this template

# 1) Etapa de build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia pom.xml e baixa dependências (caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia todo o código-fonte e empacota a aplicação
COPY src ./src
RUN mvn package -DskipTests -B

# 2) Etapa de runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Ajuste o nome do JAR conforme seu pom.xml gera
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Se você precisar expor alguma porta
EXPOSE 8080

# Comando para rodar
ENTRYPOINT ["java", "-jar", "app.jar"]