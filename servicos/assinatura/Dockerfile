FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY servicos/pom.xml /workspace/servicos/pom.xml
COPY servicos/assinatura /workspace/servicos/assinatura
RUN mvn -f servicos/pom.xml -pl assinatura -am -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/servicos/assinatura/target/ /app/
EXPOSE 8084
ENTRYPOINT ["sh", "-c", "java -jar /app/*-SNAPSHOT.jar"]
