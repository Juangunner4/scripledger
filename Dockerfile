# Stage 1: Build the application
FROM quay.io/quarkus/ubi-quarkus-mandrel:22.3.0.0-Final-java17 AS build

# Set the working directory
WORKDIR /work/

# Copy the Maven build files
COPY pom.xml /work/
COPY src /work/src

# Build the application using Maven
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final image
FROM quay.io/quarkus/quarkus-distroless-image:2.13.3.Final

# Copy the built application from the previous stage
COPY --from=build /work/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /work/target/quarkus-app/*.jar /deployments/
COPY --from=build /work/target/quarkus-app/app/ /deployments/app/
COPY --from=build /work/target/quarkus-app/quarkus/ /deployments/quarkus/

# Specify the entrypoint for the container
ENTRYPOINT ["java", "-Dquarkus.http.host=0.0.0.0", "-jar", "/deployments/quarkus-run.jar"]
