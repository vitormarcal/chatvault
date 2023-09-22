# chatvault

Chat Vault is a Kotlin Spring Boot application designed to store backups of WhatsApp conversations from various sources, such as API imports, email, and directory monitoring, and provide easy access to these conversations through a frontend that resembles a chat application, like WhatsApp.

This project is still in development, and some features may not be fully implemented.

## Key Features
* Directory Monitoring: Configure specific directories to monitor. When new WhatsApp conversation files are detected in these directories, Chat Vault automatically archives them.
* Automated Email Backup: Set up an email in ChatVault and export your messages as attachments to that email. Chat Vault identifies this email and archives the messages and attachments automatically.
* Intuitive Frontend: To facilitate access to archived messages and their attachments, Chat Vault also includes a user-friendly frontend. Easily navigate conversations, search messages, and view attachments.
