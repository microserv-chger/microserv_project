FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17.0.11_9-jre
WORKDIR /app
COPY --from=build /workspace/target/widgetapi-0.0.1-SNAPSHOT.jar application.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/application.jar"]

