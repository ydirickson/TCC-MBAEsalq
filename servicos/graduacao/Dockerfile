FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY servicos /workspace/servicos
RUN mvn -f servicos/pom.xml -pl graduacao -am -DskipTests package

FROM eclipse-temurin:21-jre

RUN addgroup --system spring && adduser --system --ingroup spring spring

WORKDIR /app
COPY --from=build /workspace/servicos/graduacao/target/*-SNAPSHOT.jar /app/app.jar
RUN chown -R spring:spring /app

EXPOSE 8081
USER spring
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
