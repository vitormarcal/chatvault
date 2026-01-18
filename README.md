# chatvault

Chat Vault is a Kotlin Spring Boot application designed to store backups of WhatsApp conversations from various sources, such as API imports, email, and directory monitoring, and provide easy access to these conversations through a frontend that resembles a chat application, like WhatsApp.

This project is still in development, and some features may not be fully implemented.

![A screenshot of the ChatVault interface displays a blur effect applied to the message text. On the left, there is an area with the list of chats, each accompanied by its corresponding profile picture. In the center, the open chat shows the visible content of the messages. On the right, the chat's image gallery is visible, showing the thumbnails of the images available in the gallery.](doc/chatvault-blur-enabled.png)

## Key Features
* Directory importing: Place files exported from whatsapp in specific directories to be imported into the application.
* Automated Email Backup: Set up an email in ChatVault and export your messages as attachments to that email. Chat Vault identifies this email and archives the messages and attachments automatically.
* Intuitive Frontend: To facilitate access to archived messages and their attachments, Chat Vault also includes a user-friendly frontend. Easily navigate conversations, search messages, and view attachments.

## How to export via the Whatsapp app

Please read the [official Whatsapp FAQ](https://faq.whatsapp.com/1180414079177245/?cms_platform=android).

With the imported file, to ingest it into ChatVault you can:

* map a shared folder between the ChatVault import directory and your cell phone;
* send by email for automatic import into ChatVault;
* zip the imported file and upload it to the ChatVault interface.

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

#### Docker image build and release workflow

This project uses a Makefile-based workflow to build and publish Docker images in a safe and explicit way.

The goal is to clearly separate **dev/test images** from **stable release images**, and to avoid accidentally publishing unstable versions as `latest`.

##### Dev / test images

Dev and test images must always use an explicit version tag, such as:

```
1.2.3-beta.1
```

To build and publish a dev/test image:

```bash
make dev VERSION=1.2.3-beta.1
```

What this does:
- Builds `ghcr.io/vitormarcal/chatvault:1.2.3-beta.1`
- Pushes only that tag
- Does **not** update `latest`

Dev images are never published as `latest`.

---

##### Release images

Stable releases must use a clean semantic version, such as:

```
1.2.3
```

To build and publish a release image:

```bash
make release VERSION=1.2.3
```

What this does:
- Validates the version string
- Builds `ghcr.io/vitormarcal/chatvault:1.2.3`
- Tags the same image as `latest`
- Pushes both `1.2.3` and `latest`

This is the **only** supported way to update the `latest` tag.

---

##### Safety checks

The release process includes built-in safety checks and will fail if:

- `VERSION=latest`
- `VERSION=dev`
- The version contains pre-release identifiers:
    - `-alpha`
    - `-beta`
    - `-rc`
    - `-snapshot`

This prevents unstable versions from being accidentally released as `latest`.

---

##### Help and usage

Running `make` without arguments shows a built-in help message describing all available commands:

```bash
make
```

If you are unsure which command to use:
- **Testing or experimentation**: use `make dev`
- **Publishing a stable version**: use `make release`

### Volumes

The app requires storing chat files in the file system. For Docker usage, please refer to the Environment Variables section.

- `chatvault.bucket.root`: This is the volume used to store your files. Do not delete this.
- `chatvault.bucket.import`: This volume is used temporarily to store chat files that are to be parsed by the app and then moved to bucket.root.
- `chatvault.bucket.export`: This volume is used temporarily to store a chat that is to be downloaded.

### Environment variables

For docker, the variables must be in upper case and where there is "." it must be "_":
`some.environment.variable` is like `SOME_ENVIRONMENT_VARIABLE` in docker

| Environment variables                     | obs                          | example                                            |
|-------------------------------------------|------------------------------|----------------------------------------------------|
| Database                                  | required                     |                                                    |
| spring.datasource.url                     | required                     | jdbc:postgresql://database_host:5432/database_name |
| spring.datasource.username                | required                     | user                                               |
| spring.datasource.password                | required                     | secret                                             |
| --------------------------                | --------------------------   | ---------                                          |
| Email import                              | feat not required            |                                                    |
| chatvault.email.enabled                   | not required                 | true                                               |
| chatvault.email.host                      | required to feat             | imap.server.com                                    |
| chatvault.email.password                  | required to feat             | secret                                             |
| chatvault.email.port                      | required to feat             | 993                                                |
| chatvault.email.username                  | required to feat             | someuser                                           |
| chatvault.email.debug                     | not required                 | true                                               |
| --------------------------                |                              | --------------------------                         |
| File system                               | not required                 |                                                    |
| chatvault.bucket.root                     | not required                 | /opt/chatvault/archive                             |
| chatvault.bucket.import                   | not required                 | /opt/chatvault/import                              |
| chatvault.bucket.export                   | not required                 | /opt/chatvault/export                              |
| --------------------------                |                              | --------------------------                         |
| chatvault.host                            | not required                 | https://somehost.com ,http://localhost:3000        |
| spring.servlet.multipart.max-file-size    | not required                 | 500MB                                              |
| spring.servlet.multipart.max-request-size | not required                 | 500MB                                              |
| chatvault.msgparser.dateformat            | not required but recommended | dd/MM/yyyy HH:mm                                   |

* If not defined chatvault.msgparser.dateformat, the application will not be able to resolve ambiguities in certain situations.