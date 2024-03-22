FROM amazoncorretto:11-alpine-jdk
COPY target/*SNAPSHOT.jar gateway.jar
ENTRYPOINT ["java", "-jar", "/gateway.jar"]
