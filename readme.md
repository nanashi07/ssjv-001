# Synchronous - asynchronous process conversion

This demo simply shows how synchronous request to be handled with background asynchronous process, and response from asynchronous result.

## Flow

![flow](data/00c33254-dce0-4457-971c-bf8d5e38e2e6.png)

## Test

```shell
curl -XPOST \
  -H 'Content-Type: application/json' \
  -d '{"type":"test","command":"create","submitter":"Bruce"}' \
  http://localhost:8080/ticket/create
```