FROM openjdk:17-slim-bullseye
WORKDIR /app

COPY build/libs/gitlabstatscrawler-*-all.jar app.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "app.jar"]
