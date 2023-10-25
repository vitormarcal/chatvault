FROM node:21.0.0-alpine3.18 as frontend_builder

WORKDIR /app

COPY ./frontend/package.json ./frontend/package-lock.json ./
RUN npm install
COPY ./frontend .
RUN npm run generate

FROM gradle:jdk17-alpine as builder

WORKDIR /app

COPY . .

RUN ./gradlew clean build

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=frontend_builder /app/.output/public /app/public
COPY --from=builder /app/backend/application/build/libs/application-0.0.1-SNAPSHOT.jar chatvault.jar

VOLUME /config
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "chatvault.jar", "--spring.config.additional-location=file:/config/application.properties", "--spring.web.resources.static-locations=file:/app/public"]