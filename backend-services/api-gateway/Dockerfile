FROM openjdk:11-alpine
VOLUME /tmp
COPY target/*.jar api-gateway.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/api-gateway.jar"]
