FROM openjdk:11-alpine
VOLUME /tmp
COPY target/*.jar app/api-gateway.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]
