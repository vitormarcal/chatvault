FROM node:25-alpine as frontend_builder

WORKDIR /app

COPY --link ./frontend/package.json ./frontend/package-lock.json ./
RUN npm install
COPY --link ./frontend .
RUN npm run generate
RUN npm prune

FROM amazoncorretto:21-alpine as backend_builder

WORKDIR /app

COPY ./backend/gradle ./backend/gradle
COPY ./backend/gradlew ./backend/gradlew
COPY ./backend/gradlew.bat ./backend/gradlew.bat
COPY ./backend/settings.gradle.kts ./backend/settings.gradle.kts
COPY ./backend/build.gradle.kts ./backend/build.gradle.kts

WORKDIR /app/backend
RUN ./gradlew build -x test --no-daemon || return 0

COPY ./backend/src ./src

RUN ./gradlew clean build --no-daemon -PskipIntegrationTests=true

FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=frontend_builder /app/.output/public /app/public
COPY --from=backend_builder /app/backend/build/libs/*.jar chatvault.jar

VOLUME /config
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "chatvault.jar", "--spring.config.additional-location=file:/config/", "--spring.web.resources.static-locations=file:/app/public"]