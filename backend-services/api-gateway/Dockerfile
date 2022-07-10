FROM openjdk:11
VOLUME /tmp
COPY target/*.jar ./
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "*.jar"]
