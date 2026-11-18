# Build stage
FROM gradle:9.9.1-jdk25 AS build
WORKDIR /workspace
COPY build.gradle settings.gradle gradlew ./
COPY gradle/wrapper gradle/wrapper
COPY src src
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app
RUN useradd --system --uid 1001 spring
# Extract layered jar for better Docker layer caching
COPY --from=build /workspace/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "extracted/dependencies/boot.jar"]
