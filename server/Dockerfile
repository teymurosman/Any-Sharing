FROM amazoncorretto:11-alpine-jdk
COPY target/*SNAPSHOT.jar server.jar
ENTRYPOINT ["java", "-jar", "/server.jar"]
