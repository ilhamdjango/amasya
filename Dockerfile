# 1-ci stage: Build
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

# pom.xml zire_fk qovluğundan kopyala
COPY zire_fk/pom.xml .
RUN mvn dependency:go-offline

# src qovluğunu kopyala
COPY zire_fk/src ./src
RUN mvn clean package -DskipTests

# 2-ci stage: Runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/amasya-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
