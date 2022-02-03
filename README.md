# SCHEMA VALIDATOR

## Summary 
The schema-validator app is composed basically by 2 components:
- An Akka-HTTP server in charge of exposing the 3 endpoints the challenge is asking for:
  - GET  /schema/ID
  - POST /schema/ID
  - POST /validate/ID
- a Postgres instance in charge of persisting the schemas

## Instructions

In order to launch the application, you need to follow the next steps from your terminal:
1. Clone the repo
2. Build the application from the app folder you cloned into:
```shell
$ cd schema-validator
$ sbt compile assembly
```
3. Run docker-compose
```shell
$ cd dockerfiles
$ docker-compose up --build #you can use also '-d' if you don't want to see any log trace
```