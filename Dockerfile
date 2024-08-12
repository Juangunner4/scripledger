# Stage 1: Build the application with Maven
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /work
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:17-jdk-slim
WORKDIR /deployments
COPY --from=build /work/target/scripledger-1.0.0-SNAPSHOT-runner.jar /deployments/
EXPOSE 80
CMD ["java", "-jar", "/deployments/scripledger-1.0.0-SNAPSHOT-runner.jar"]
