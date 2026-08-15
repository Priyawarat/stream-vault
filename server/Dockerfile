# ===========================
# Stage 1 - Build
# ===========================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven Wrapper
COPY .mvn .mvn
COPY mvnw .

# Make Maven Wrapper executable
RUN chmod +x mvnw

# Copy Maven configuration
COPY pom.xml .

# Download dependencies first
# This layer will be cached unless pom.xml changes
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests


# ===========================
# Stage 2 - Runtime
# ===========================
FROM eclipse-temurin:21-jre

# Install FFmpeg (includes ffprobe)
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/* \

WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Create upload directory
RUN mkdir -p /app/uploads && \
    chown -R spring:spring /app

# Copy built JAR
COPY --from=builder /app/target/*.jar app.jar

# Run as non-root user
USER spring

# Dataset Service port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
