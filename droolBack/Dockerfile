# Dockerfile para API Spring Boot
# Etapa 1: Build con Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Configurar directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven primero (para cache de dependencias)
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests -B

# Etapa 2: Runtime con JRE optimizado
FROM eclipse-temurin:21-jre-alpine

# Instalar herramientas necesarias
RUN apk add --no-cache curl wget tzdata

# Configurar zona horaria
ENV TZ=America/Lima
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Crear usuario no root para seguridad
RUN addgroup -g 1000 -S drools && \
    adduser -u 1000 -S drools -G drools

# Configurar directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Crear directorios necesarios
RUN mkdir -p /app/logs /app/rules /app/temp && \
    chown -R drools:drools /app

# Configurar variables de entorno para JVM
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"
ENV SPRING_PROFILES_ACTIVE=docker

# Cambiar a usuario no root
USER drools

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]