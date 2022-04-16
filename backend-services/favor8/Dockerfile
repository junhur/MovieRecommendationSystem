FROM openjdk:11
VOLUME /tmp
COPY target/*.jar favor8.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/favor8.jar"]
