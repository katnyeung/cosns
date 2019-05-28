FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD cosns-0.0.1-SNAPSHOT.jar cosns.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/cosns.jar"]