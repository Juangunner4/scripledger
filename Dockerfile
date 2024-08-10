# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory
WORKDIR /work/

# Copy the Maven build files
COPY pom.xml /work/
COPY src /work/src

# Build the application using Maven
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM quay.io/quarkus/quarkus-distroless-image:1.0

# Copy the built application from the previous stage
COPY --from=build /work/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /work/target/quarkus-app/*.jar /deployments/
COPY --from=build /work/target/quarkus-app/app/ /deployments/app/
COPY --from=build /work/target/quarkus-app/quarkus/ /deployments/quarkus/

# Expose port 80
EXPOSE 80

# Specify the entrypoint for the container
ENTRYPOINT ["java", "-Dquarkus.http.port=80", "-Dquarkus.http.host=0.0.0.0", "-jar", "/deployments/quarkus-run.jar"]
