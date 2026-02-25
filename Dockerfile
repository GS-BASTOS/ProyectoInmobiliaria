# ── STAGE 1: Build con Maven ────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia primero el pom para aprovechar caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente y compila
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── STAGE 2: Imagen final ligera ────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia el jar generado
COPY --from=build /app/target/*.jar app.jar

# Crea carpeta de uploads
RUN mkdir -p uploads/property-media

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
