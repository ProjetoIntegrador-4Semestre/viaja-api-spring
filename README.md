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

## Place contract

`GET /places/search?q=Rio` searches place names, addresses and city names and returns a simple list. `GET /places/{id}` returns the same fields plus HATEOAS links. Identifiers are strings in this read contract. `price` is an integer amount in cents, not a price level; `description` is included. The paginated `/places` collection and write endpoints retain the Local entity contract.

Android debug builds can use `-PapiBaseUrl=http://10.0.2.2:18080/` to reach the local API from the emulator. The hosted API must receive this contract update before the default hosted Android configuration is used.

The general RuntimeException handler still maps exceptions to 404. AuthenticationException is handled separately as 401; remaining error cases require a resource-by-resource audit.

The recovery scope, checkpoints and verified Android baseline are tracked in the Android repository's `docs/recovery-plan.md`.

## Saved places

All `/users/{userId}/saved-locals` operations require that the JWT user owns the path's userId; another user's identifier returns 403. Lists retain HAL pagination with `savedLocals` in `_embedded`. Save with `POST` and a body containing `localId`; the server assigns the saved-record ID, owner and timestamp. Creation returns 201 with a resolvable Location. Missing places and duplicate saves return 400. `GET` and `DELETE` on `/{localId}` use the place's identifier; a missing association returns 404, and successful removal returns 204. The smoke test creates and removes a demonstration association and checks that the original seed association remains intact.

## Trips and itinerary

Trip collections are paginated and scoped to the JWT user. Read/write/delete operations reject another user's trip or itinerary with 403. Create with `name`, `cityId`, and mode `tourism` or `work`; ownership is derived from the JWT, and a supplied conflicting userId is rejected. Dates may be omitted; if endDate is present it requires startDate and cannot precede it. Budget is a nonnegative integer amount in cents, limited to 2147483647. Creation returns the Trip entity with HATEOAS links, 201 and Location. Updates replace optional dates/budget and may set planned/active/completed/cancelled status.

Itinerary entries use tripId/localId and positive day/orderIndex. The place must belong to the trip's city; duplicate association is rejected. Per-trip lists are sorted by day and order. Trip deletion removes stops, notes, memos and trip media in the same transaction. These ownership checks do not yet cover the separate note/memo/user controllers.
