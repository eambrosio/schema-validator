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
4. Once the server is running, you can perform the proper request pointing to `localhost:8080`, such as:
```shell
$ curl --location --request POST 'http://localhost:8080/schema/test-10' \                                                                                                                                  09:43:00  
--header 'Content-Type: application/json' \
--data-raw '{"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"source":{"type":"string"},"destination":{"type":"string"},"timeout":{"type":"integer","minimum":0,"maximum":32767},"chunks":{"type":"object","properties":{"size":{"type":"integer"},"number":{"type":"integer"}},"required":["size"]}},"required":["source","destination"]}'


$ curl --location --request GET 'http://localhost:8080/schema/test-10' \
--header 'Content-Type: application/json'


$ curl --location --request POST 'http://localhost:8080/validate/test-10' \                                                                                                                                09:45:41  
--header 'Content-Type: application/json' \
--data-raw '{
  "source": "/home/alice/image.iso",
  "destination": "/mnt/storage",
  "timeout": 1,  
  "chunks": {
    "size": 1024,
    "number": null 
  }
}'

```