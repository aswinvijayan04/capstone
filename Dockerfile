# ---------- Stage 1: build the jar ----------
# Image that has Maven + JDK 21 pre-installed
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom.xml first and download dependencies.
# Docker caches each step, so this slow step is skipped on later builds
# unless pom.xml changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy the source code and build the jar.
# Tests already run in the pipeline, so we skip them here to keep the image build fast.
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: small runtime image ----------
# JRE = Java runtime only (no compiler), Alpine = tiny Linux
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: create a non-root user so the app does not run as root
RUN addgroup -S app && adduser -S app -G app

# Copy ONLY the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

USER app

# Documents the port the app listens on (does not publish it by itself)
EXPOSE 8080

# Command that starts the app when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]