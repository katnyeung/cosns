FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD cosns.jar cosns.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/cosns.jar"]