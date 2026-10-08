# Florvvia backend — builds + runs anywhere (Render/Railway/Fly/your own server)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/florvvia-backend-*.jar app.jar
EXPOSE 8080
# Render/Railway inject PORT; application.properties already reads ${PORT:8080}
CMD ["java", "-jar", "app.jar"]
