FROM node:22.0.0-alpine as frontend_builder

WORKDIR /app

COPY --link ./frontend/package.json ./frontend/package-lock.json ./
RUN npm install
COPY --link ./frontend .
RUN npm run generate
RUN npm prune

FROM amazoncorretto:21-alpine as backend_builder

WORKDIR /app

COPY --link gradle ./gradle
COPY --link build.gradle gradlew gradlew.bat settings.gradle ./
COPY --link backend ./backend

RUN ./gradlew clean build

FROM amazoncorretto:21-alpine

WORKDIR /app
COPY --from=frontend_builder /app/.output/public /app/public
COPY --from=backend_builder /app/backend/application/build/libs/application-1.15.0.jar chatvault.jar

VOLUME /config
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "chatvault.jar", "--spring.config.additional-location=file:/config/", "--spring.web.resources.static-locations=file:/app/public"]