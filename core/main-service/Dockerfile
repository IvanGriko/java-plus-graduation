FROM amazoncorretto:21-alpine
LABEL authors="Ivan Griko"
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]