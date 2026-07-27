# Multi-stage build for any Spring Boot module in the monorepo.
# Usage: docker build --build-arg SERVICE=account_service -t account-service .

ARG SERVICE=account_service

FROM eclipse-temurin:17-jdk-alpine AS build
ARG SERVICE
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -q -pl "${SERVICE}" -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
ARG SERVICE
RUN apk add --no-cache curl
COPY --from=build /app/${SERVICE}/target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
