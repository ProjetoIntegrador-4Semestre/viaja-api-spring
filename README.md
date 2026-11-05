# Viajá API

Java/Spring Boot API for the Viajá Android application. The classroom reference is `/Users/rafa/IdeaProjects/corp-financial-api`.

## Local execution

Use Java 17 or newer and the included Maven wrapper:

```sh
./mvnw verify
java -jar target/api-viaja-0.0.1-SNAPSHOT.jar --server.port=18080
```

Stop the running application before rebuilding its executable JAR.

The application starts an in-memory H2 database and LoadDatabase inserts demonstration records. Data is reset when the application restarts. The seed includes three cities, three places and demonstration users. It is not a production persistence configuration.

- Swagger UI: `http://localhost:18080/swagger-ui/index.html`
- OpenAPI: `http://localhost:18080/v3/api-docs`

## Baseline verification

With a fresh local instance running on port 18080:

```sh
python3 scripts/smoke_test.py
```

An alternative local address can be provided through `--base-url`. Run this verification against a fresh demonstration database: its assertions expect the initial record counts.

The verification checks seeded city pagination and its next-page link, a missing city's 404 response, seeded place search, OpenAPI availability, invalid login returning 401, and a seeded user's JWT access to trips. It does not print tokens. These checks do not establish complete CRUD correctness, authorization isolation or Android integration.

Login currently accepts `email` and `senha`. The demonstration account is `rafael@email.com` with password `senha123`. Successful login returns the JWT in the `Authorization` response header; send that value in subsequent protected requests.

## Current integration limitations

`GET /places/search` returns a simple list of PlaceResponse objects. `GET /places/{id}` returns a Local entity with HATEOAS links. These payloads need reconciliation before replacing Android's mock-based place flow. Search currently matches place name/address, rather than city identity.

The general RuntimeException handler still maps exceptions to 404. AuthenticationException is handled separately as 401; remaining error cases require a resource-by-resource audit.

The recovery scope, checkpoints and verified Android baseline are tracked in the Android repository's `docs/recovery-plan.md`.
