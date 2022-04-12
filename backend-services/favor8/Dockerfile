FROM openjdk:11-alpine
VOLUME /tmp
COPY target/*.jar app/favor8.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/favor8.jar"]
