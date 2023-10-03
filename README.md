# chatvault

Chat Vault is a Kotlin Spring Boot application designed to store backups of WhatsApp conversations from various sources, such as API imports, email, and directory monitoring, and provide easy access to these conversations through a frontend that resembles a chat application, like WhatsApp.

This project is still in development, and some features may not be fully implemented.

## Key Features
* Directory Monitoring: Configure specific directories to monitor. When new WhatsApp conversation files are detected in these directories, Chat Vault automatically archives them.
* Automated Email Backup: Set up an email in ChatVault and export your messages as attachments to that email. Chat Vault identifies this email and archives the messages and attachments automatically.
* Intuitive Frontend: To facilitate access to archived messages and their attachments, Chat Vault also includes a user-friendly frontend. Easily navigate conversations, search messages, and view attachments.

## Repository structure

This repository is divided into two main modules, managed by gradle. They are the modules: frontend and backend

### Run Frontend module

The front end module is a Vue/Nuxt application and it serves what will be rendered by the browser: html, css, javascript and static assets.
You can run it with gradle or directly with npm.
By default, gradle will download node binaries and use them when you run gradle tasks.

If you want gradle to run its own node installed on your computer, edit the download=true property to download=false in the gradle.build file inside the frontend module.

To install the package.json libraries through gradle, run 

`
./gradlew npmInstall
`

To run the frontend application through gradle, run

`
./gradlew npmRunDev
`

The frontend application will listen on port 8080 by default, unless you ran the backend application before (the backend listens on 8080), in which case the frontend will pick up a random port.
If you have run the backend, it will serve the static files and it is not necessary to run the frontend, but it is still possible to run the two on different ports and processes.
To run the conventional way, with npm commands, follow the Readme in the frontend directory.