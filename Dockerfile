# syntax=docker/dockerfile:1

# ---------- Этап 1: сборка ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Сначала копируем только pom.xml — слой с зависимостями кешируется
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:resolve

# Затем исходники и сборка исполняемого jar
COPY src ./src
RUN mvn -B -q -DskipTests clean package

# ---------- Этап 2: запуск ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
