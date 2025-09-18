# 1-ci stage: Build
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

# pom.xml və dependency-ləri yüklə
COPY zire_fk/pom.xml .
RUN mvn dependency:go-offline

# Kodları kopyala və build et
COPY zire_fk/src ./src
RUN mvn clean package -DskipTests

# 2-ci stage: Runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Build stage-dən jar faylını kopyala
COPY --from=build /app/target/*.jar app.jar

# Spring Boot tətbiqini işə sal
ENTRYPOINT ["java","-jar","app.jar"]
