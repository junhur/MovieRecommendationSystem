FROM openjdk:11
VOLUME /tmp
COPY target/*.jar ./
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "*.jar"]
