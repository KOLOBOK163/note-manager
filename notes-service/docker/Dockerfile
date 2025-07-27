FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build
COPY ../../ .
RUN mvn -pl notes-service -am package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && apt-get clean
COPY --from=builder /build/notes-service/target/*.jar app.jar

ARG SERVER_PORT=8081
ENV SERVER_PORT=${SERVER_PORT}
ENV JAVA_OPTS="-XX:+UseZGC -Xmx512m -Dfile.encoding=UTF-8"

HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

EXPOSE ${SERVER_PORT}
ENTRYPOINT sh -c "exec java $JAVA_OPTS -jar app.jar"