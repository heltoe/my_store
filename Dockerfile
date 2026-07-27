# syntax=docker/dockerfile:1
# Multi-stage build for any Spring Boot module in the monorepo.
# Usage: docker build --build-arg SERVICE=account_service -t account-service .

ARG SERVICE=account_service

FROM eclipse-temurin:17-jdk-jammy AS build
ARG SERVICE
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY common_lib/pom.xml common_lib/pom.xml
COPY bom_dependencies/pom.xml bom_dependencies/pom.xml
COPY account_service/pom.xml account_service/pom.xml
COPY courier_service/pom.xml courier_service/pom.xml
COPY products_service/pom.xml products_service/pom.xml
COPY cart_service/pom.xml cart_service/pom.xml
COPY delivery_service/pom.xml delivery_service/pom.xml
COPY order_service/pom.xml order_service/pom.xml
COPY payment_service/pom.xml payment_service/pom.xml
COPY api_gateway/pom.xml api_gateway/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && \
    for attempt in 1 2 3; do \
      ./mvnw -B -q -pl "${SERVICE}" -am dependency:go-offline -DskipTests && break; \
      echo "Maven dependency download attempt ${attempt} failed, retrying..."; \
      sleep 15; \
      if [ "${attempt}" -eq 3 ]; then exit 1; fi; \
    done

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    for attempt in 1 2 3; do \
      ./mvnw -B -q -pl "${SERVICE}" -am package -DskipTests && break; \
      echo "Maven build attempt ${attempt} failed, retrying..."; \
      sleep 15; \
      if [ "${attempt}" -eq 3 ]; then exit 1; fi; \
    done

FROM eclipse-temurin:17-jre-jammy
ARG SERVICE
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/${SERVICE}/target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
