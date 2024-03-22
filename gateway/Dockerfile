FROM amazoncorretto:11-alpine-jdk
COPY target/*SNAPSHOT.jar gateway.jar
#ENV TZ=Europe/Moscow
ENTRYPOINT ["java", "-jar", "/gateway.jar"]
