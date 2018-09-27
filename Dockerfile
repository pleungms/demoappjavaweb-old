FROM openjdk:8-jdk
MAINTAINER <pleungms@hotmail.com>

EXPOSE 4567

VOLUME /data

COPY build/libs/*.jar /app/service.jar

CMD ["java", "-jar", "/app/service.jar"]