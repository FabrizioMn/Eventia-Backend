# 1era etapa: Construcción
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app


# Copiamos el wrapper de maven y el archivo de configuracion
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Damos permisos de ejecución al wrapper y descargamos dependencias
COPY src src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests


# Etapa 2: Imagen de Producción
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el .jar generado
COPY --from=build /app/target/Eventia-0.0.1-SNAPSHOT.jar app.jar

# Nunca usar el perfil dev (H2, datos semilla y consola) en una imagen desplegada.
ENV SPRING_PROFILES_ACTIVE=prod

RUN addgroup -S eventia && adduser -S eventia -G eventia
USER eventia

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]