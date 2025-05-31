FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew ./
COPY gradle gradle/
COPY build.gradle.kts settings.gradle.kts ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (this layer will be cached if dependencies don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src/

# Build the application
RUN ./gradlew build --no-daemon -x test

# Expose the port your app runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "build/libs/client-auth-service-0.0.1-SNAPSHOT.jar"]