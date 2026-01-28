# chatvault

Chat Vault is a Kotlin Spring Boot application designed to store backups of WhatsApp conversations from various sources, such as API imports, email, and directory monitoring, and provide easy access to these conversations through a frontend that resembles a chat application, like WhatsApp.

This project is still in development, and some features may not be fully implemented.

![A screenshot of the ChatVault interface displays a blur effect applied to the message text. On the left, there is an area with the list of chats, each accompanied by its corresponding profile picture. In the center, the open chat shows the visible content of the messages. On the right, the chat's image gallery is visible, showing the thumbnails of the images available in the gallery.](docs/layout/chatvault-blur-enabled-v1.20.0.png)

## Key Features
* Directory importing: Place files exported from whatsapp in specific directories to be imported into the application.
* Automated Email Backup: Set up an email in ChatVault and export your messages as attachments to that email. Chat Vault identifies this email and archives the messages and attachments automatically.
* Intuitive Frontend: To facilitate access to archived messages and their attachments, Chat Vault also includes a user-friendly frontend. Easily navigate conversations, search messages, and view attachments.

## How to export via the Whatsapp app

See [docs/whatsapp-export.md](docs/whatsapp-export.md) for the official FAQ link and import options.

## Repository structure

This repository is divided into two main modules. They are the modules: frontend (javascript, vue, nuxt) and backend (kotlin, java, spring boot, gradle)

### Run Frontend module

The front end module is a Vue/Nuxt application and it serves what will be rendered by the browser: html, css, javascript and static assets.
To run the conventional way, with npm commands, follow the Readme in the frontend directory.

You can run it with npm:

`npm run dev`

The frontend application will listen on port 3000 by default, unless you ran the backend application before (the backend listens on 8080), in which case the frontend will pick up a random port.

### Run Backend module

You can run the backend application without an IDE:

`./gradlew run`

The backend application will listen to 8080 port by default.

### Docker

Note that downloading container images might require you to authenticate to the GitHub Container Registry [steps here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authenticating-to-the-container-registry).
You can use compose.yml to create a database and build the frontend and backend project locally.

`docker-compose -f compose.yml`

There are docker image packages on github. You can download the latest image with:

`docker pull ghcr.io/vitormarcal/chatvault:latest`

See [docs/docker-release.md](docs/docker-release.md) for the image build and release workflow.

### Volumes

See [docs/configuration.md](docs/configuration.md) for volume details.

### Environment variables

See [docs/configuration.md](docs/configuration.md) for the full environment variable table and notes.
