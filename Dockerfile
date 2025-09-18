# Base image
FROM eclipse-temurin:17-jdk-alpine

# İş qovluğu
WORKDIR /app

# Maven build nəticəsini kopyala
COPY target/amasya-0.0.1-SNAPSHOT.jar app.jar

# JAR-ı işə sal
ENTRYPOINT ["java","-jar","app.jar"]
