FROM gradle:jdk17-alpine as builder

WORKDIR /app

COPY . .

RUN apk add --update npm
RUN ./gradlew clean build

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/backend/application/build/libs/application-0.0.1-SNAPSHOT.jar chatvault.jar

VOLUME /config
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "chatvault.jar", "--spring.config.additional-location=file:/config/application.properties"]