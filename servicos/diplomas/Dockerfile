FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY servicos /workspace/servicos
RUN mvn -f servicos/pom.xml -pl diplomas -am -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/servicos/diplomas/target/*-SNAPSHOT.jar /app/app.jar
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
