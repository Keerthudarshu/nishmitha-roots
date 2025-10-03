# Multi-stage build for Render deployment
FROM maven:3.9.4-openjdk-21-slim AS builder

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy jar file from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p uploads

# Expose port (Render will map this automatically)
EXPOSE 8081

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]