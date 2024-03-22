FROM amazoncorretto:11-alpine-jdk
COPY target/*SNAPSHOT.jar server.jar
#ENV TZ=Europe/Moscow
ENTRYPOINT ["java", "-jar", "/server.jar"]
