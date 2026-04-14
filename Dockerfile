FROM alpine:3.19 as agent-downloader
RUN apk add --no-cache curl
RUN curl -L https://repo1.maven.org/maven2/io/sentry/sentry-opentelemetry-agent/8.33.0/sentry-agent.jar -o sentry-agent.jar

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir -p /tmp/uploads && chown -R spring:spring /tmp/uploads

USER spring:spring
WORKDIR /app

COPY --from=agent-downloader /sentry-agent.jar /app/sentry-agent.jar
COPY --from=build /app/target/scheduling-nails-pro-0.0.1-SNAPSHOT.jar /app/app.jar

ENV SENTRY_AUTO_INIT=false \
    OTEL_TRACES_EXPORTER=none \
    OTEL_METRICS_EXPORTER=none \
    OTEL_LOGS_EXPORTER=none \
    JAVA_TOOL_OPTIONS="-javaagent:sentry-agent.jar -XX:+UseParallelGC -XX:MaxRAMPercentage=75.0"

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]