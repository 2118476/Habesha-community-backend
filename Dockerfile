# syntax=docker/dockerfile:1

########## BUILD STAGE ##########
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only files needed to resolve deps first (better caching)
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Now copy the rest and build
COPY src ./src
RUN mvn -q -DskipTests package

########## RUNTIME STAGE ##########
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# (Optional) non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Render sets PORT; Spring Boot reads it via application.properties
ENV PORT=8080
ENV JAVA_OPTS=""

# Copy the fat jar from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
