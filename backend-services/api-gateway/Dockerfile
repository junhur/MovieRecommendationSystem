FROM openjdk:11
VOLUME /tmp
COPY target/api-gateway.jar api-gateway.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/api-gateway.jar"]
