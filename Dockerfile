# =============================================================================
# CriptoPass MS Ticket Ticket - Dockerfile
# Multi-stage build para optimizar tamaño de imagen
# =============================================================================

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de Gradle primero para aprovechar cache de capas
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Dar permisos de ejecucion al gradlew
RUN chmod +x ./gradlew

# Descargar dependencias (se cachea si no cambian los archivos de build)
RUN ./gradlew dependencies --no-daemon || true

# Copiar codigo fuente
COPY src ./src
COPY docs ./docs

# Construir la aplicacion (sin tests para Docker de desarrollo)
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el JAR desde el stage de build
COPY --from=builder /app/build/libs/*.jar app.jar

# Cambiar propietario
RUN chown appuser:appgroup app.jar

# Cambiar a usuario no-root
USER appuser

# Exponer puerto del microservicio
EXPOSE 8084

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Ejecutar la aplicacion
ENTRYPOINT ["java", \
  "-XX:+UseZGC", \
  "-XX:+ZGenerational", \
  "-Dspring.profiles.active=docker", \
  "-jar", "app.jar"]
