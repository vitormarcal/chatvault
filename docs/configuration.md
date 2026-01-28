# Configuration

## Volumes

The app requires storing chat files on the file system.

- `chatvault.bucket.root`: Volume used to store your files. Do not delete this.
- `chatvault.bucket.import`: Temporary volume used to store chat files to be parsed and then moved to `bucket.root`.
- `chatvault.bucket.export`: Temporary volume used to store a chat that is about to be downloaded.

## Environment variables

For Docker, the variables must be in upper case and where there is "." it must be "_":
`some.environment.variable` becomes `SOME_ENVIRONMENT_VARIABLE`.

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

If `chatvault.msgparser.dateformat` is not defined, the application will not be able to resolve ambiguities in certain situations.
