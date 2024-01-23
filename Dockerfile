FROM node:21.0.0-alpine3.18 as frontend_builder

WORKDIR /app

COPY ./frontend/package.json ./frontend/package-lock.json ./
RUN npm install
COPY ./frontend .
RUN npm run generate

FROM amazoncorretto:21-alpine as backend_builder

WORKDIR /app

COPY gradle ./gradle
COPY build.gradle gradlew gradlew.bat settings.gradle ./
COPY backend ./backend

RUN ./gradlew clean build

FROM amazoncorretto:21-alpine

WORKDIR /app
COPY --from=frontend_builder /app/.output/public /app/public
COPY --from=backend_builder /app/backend/application/build/libs/application-1.11.1.jar chatvault.jar

VOLUME /config
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "chatvault.jar", "--spring.config.additional-location=file:/config/", "--spring.web.resources.static-locations=file:/app/public"]