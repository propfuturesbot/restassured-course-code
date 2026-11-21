# The Students app on Java 21. eclipse-temurin publishes amd64 and arm64 images, so this builds natively on
# Intel/AMD PCs and on Apple Silicon:  docker build -t students-app apps/students-app
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY studentApp-java21.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
