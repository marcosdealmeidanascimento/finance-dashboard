# syntax=docker/dockerfile:1
# Builds from a clean tree with JDK 21, so stale IDE-compiled classes in target/ never reach the image.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# The real application.properties is excluded by .dockerignore; the template only references env vars,
# so no secret is ever baked into the image.
RUN cp src/main/resources/application.properties.example src/main/resources/application.properties
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/dashboard-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
