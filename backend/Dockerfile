# Use Java 21 for better compatibility with Spring Boot 3.x
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy jar file (built locally)
COPY target/*.jar app.jar

# Expose port
EXPOSE 8081

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]