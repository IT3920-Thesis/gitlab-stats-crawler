FROM gradle:7.3.3-jdk17 as build

WORKDIR /app

COPY ./build.gradle .
COPY settings.gradle .
COPY ./gradle.properties .
COPY ./src ./src

RUN gradle clean assemble

FROM openjdk:17-slim-bullseye
WORKDIR /app

COPY --from=build /app/build/libs/gitlabstatscrawler-*-all.jar app.jar

EXPOSE 8080

CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx256m", "-jar", "app.jar"]
