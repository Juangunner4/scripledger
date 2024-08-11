# Stage 1: Build the application
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /work/

# Copy the Maven build files
COPY pom.xml /work/
COPY src /work/src

# Build the application using Maven
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:17-jdk-slim

# Copy the built application from the previous stage
COPY --from=build /work/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /work/target/quarkus-app/*.jar /deployments/
COPY --from=build /work/target/quarkus-app/app/ /deployments/app/
COPY --from=build /work/target/quarkus-app/quarkus/ /deployments/quarkus/

# Expose port 80
EXPOSE 80

# Specify the entrypoint for the container
ENTRYPOINT ["java", "-Dquarkus.http.host=0.0.0.0", "-Dquarkus.http.port=80", "-jar", "/deployments/quarkus-run.jar"]