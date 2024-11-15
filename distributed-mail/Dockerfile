FROM openjdk:21-jdk-slim

WORKDIR /app

COPY /build/libs/*.jar /app/distirbuted-mail-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java"]
CMD ["-jar", "distirbuted-mail-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080
