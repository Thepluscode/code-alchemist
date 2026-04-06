FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve -B 2>/dev/null || true
COPY src src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8091
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8091/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
