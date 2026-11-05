import argparse
import json
from urllib.error import HTTPError
from urllib.request import Request, urlopen


def request(base_url, path, body=None, token=None, method=None):
    headers = {"Accept": "application/json"}
    data = None
    if body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body).encode()
    if token:
        headers["Authorization"] = token
    try:
        with urlopen(Request(base_url + path, data=data, headers=headers, method=method), timeout=15) as response:
            return response.status, response.headers, response.read()
    except HTTPError as error:
        return error.code, error.headers, error.read()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:18080")
    args = parser.parse_args()
    base_url = args.base_url.rstrip("/")

    status, _, body = request(base_url, "/cities?page=0&size=2&sort=id,asc")
    assert status == 200, f"City listing returned {status}"
    page = json.loads(body)
    assert page["page"]["size"] == 2
    assert page["page"]["totalElements"] == 3
    assert page["page"]["totalPages"] == 2
    assert "next" in page["_links"]
    next_url = page["_links"]["next"]["href"]
    next_path = next_url.removeprefix(base_url)
    assert next_path.startswith("/"), "Pagination link uses an unexpected server"
    status, _, body = request(base_url, next_path)
    assert status == 200
    assert json.loads(body)["page"]["number"] == 1
    print("PASS: seeded cities and HATEOAS pagination")

    status, _, _ = request(base_url, "/cities/999999")
    assert status == 404
    print("PASS: missing city returns 404")

    status, _, body = request(base_url, "/places/search")
    assert status == 200
    assert len(json.loads(body)) == 3
    print("PASS: seeded places are searchable")

    status, _, body = request(base_url, "/places/search?q=Rio")
    assert status == 200
    places = json.loads(body)
    assert len(places) == 2
    for place in places:
        status, _, body = request(base_url, "/places/" + place["id"])
        assert status == 200
        detail = json.loads(body)
        assert all(detail[key] == value for key, value in place.items())
        assert "price" in detail and "priceLevel" not in detail
        assert detail["description"]
        assert "self" in detail["_links"]
    status, _, body = request(base_url, "/places/search?q=not-a-place")
    assert status == 200 and json.loads(body) == []
    status, _, _ = request(base_url, "/places/999999")
    assert status == 404
    print("PASS: city search, consistent place details, empty search and missing place")

    status, _, body = request(base_url, "/v3/api-docs")
    assert status == 200
    assert "/auth/login" in json.loads(body)["paths"]
    print("PASS: OpenAPI document is available")

    status, _, _ = request(base_url, "/auth/login", {
        "email": "rafael@email.com", "senha": "invalid-password"
    })
    assert status == 401, f"Invalid credentials returned {status}, expected 401"
    print("PASS: invalid credentials return 401")

    status, headers, _ = request(base_url, "/auth/login", {
        "email": "rafael@email.com", "senha": "senha123"
    })
    assert status == 200
    token = headers.get("Authorization", "")
    assert token.startswith("Bearer ")
    status, _, _ = request(base_url, "/trips", token=token)
    assert status == 200
    print("PASS: seeded user can authenticate and access trips")

    for method, path, body in [
        ("GET", "/users/2/saved-locals", None),
        ("GET", "/users/2/saved-locals/3", None),
        ("POST", "/users/2/saved-locals", {"localId": 3}),
        ("DELETE", "/users/2/saved-locals/3", None),
    ]:
        status, _, _ = request(base_url, path, body, token, method)
        assert status == 403, f"Cross-user {method} returned {status}"
    status, _, _ = request(base_url, "/users/1/saved-locals", {"localId": 999999}, token)
    assert status == 400
    status, headers, body = request(base_url, "/users/1/saved-locals", {"id": 1, "userId": 2, "localId": 3}, token)
    assert status == 201
    saved = json.loads(body)
    assert saved["userId"] == 1 and saved["localId"] == 3 and saved["id"] != 1
    assert headers["Location"].endswith("/users/1/saved-locals/3")
    status, _, _ = request(base_url, "/users/1/saved-locals/3", token=token)
    assert status == 200
    status, _, _ = request(base_url, "/users/1/saved-locals", {"localId": 3}, token)
    assert status == 400
    status, _, body = request(base_url, "/users/1/saved-locals?size=1", token=token)
    assert status == 200
    first_page = json.loads(body)
    assert first_page["page"]["totalElements"] == 2
    assert len(first_page["_embedded"]["savedLocals"]) == 1
    status, _, body = request(base_url, "/users/1/saved-locals?size=1&page=1", token=token)
    assert status == 200
    second_page = json.loads(body)
    assert second_page["_embedded"]["savedLocals"][0]["localId"] == 3
    status, _, body = request(base_url, "/users/1/saved-locals/3", token=token, method="DELETE")
    assert status == 204 and not body
    status, _, _ = request(base_url, "/users/1/saved-locals/3", token=token)
    assert status == 404
    status, _, _ = request(base_url, "/users/1/saved-locals/1", token=token)
    assert status == 200
    print("PASS: saved ownership, creation, links, duplicate rejection, pagination and deletion")

    status, _, body = request(base_url, "/trips", token=token)
    assert status == 200
    assert all(trip["userId"] == 1 for trip in json.loads(body)["_embedded"]["trips"])
    trip_body = {"name": "Smoke trip", "cityId": 1, "mode": "tourism"}
    for method, path, payload in [
        ("GET", "/trips/2", None), ("GET", "/trips/user/2", None),
        ("POST", "/trips", {**trip_body, "userId": 2}),
        ("PUT", "/trips/2", trip_body), ("DELETE", "/trips/2", None),
        ("GET", "/trip-locals/trip/2", None), ("GET", "/trip-locals/2/3", None),
        ("POST", "/trip-locals", {"tripId": 2, "localId": 3, "day": 1, "orderIndex": 1}),
        ("PUT", "/trip-locals/2/3", {"day": 1, "orderIndex": 1}),
        ("DELETE", "/trip-locals/2/3", None),
    ]:
        status, _, _ = request(base_url, path, payload, token, method)
        assert status == 403, f"Cross-user {method} {path} returned {status}"
    for invalid in [{"cityId": 999999}, {"name": " "}, {"mode": "invalid"},
                    {"startDate": "invalid"}, {"endDate": "2026-12-01"},
                    {"budgetCents": -1}, {"budgetCents": 2147483648}]:
        status, _, _ = request(base_url, "/trips", {**trip_body, **invalid}, token)
        assert status == 400, f"Invalid trip {invalid} returned {status}"
    status, headers, body = request(base_url, "/trips", trip_body, token)
    assert status == 201
    trip = json.loads(body)
    trip_id = str(trip["id"])
    assert trip["userId"] == 1 and trip["cityId"] == 1 and trip["startDate"] is None
    assert headers["Location"].endswith("/trips/" + trip_id)
    status, _, _ = request(base_url, "/trips/" + trip_id, {**trip_body, "name": "Updated trip", "budgetCents": 12345}, token, "PUT")
    assert status == 200
    stop = {"tripId": int(trip_id), "localId": 1, "day": 1, "orderIndex": 1}
    status, _, _ = request(base_url, "/trip-locals", {**stop, "localId": 3}, token)
    assert status == 400
    status, _, _ = request(base_url, "/trip-locals", {**stop, "day": 0}, token)
    assert status == 400
    status, _, _ = request(base_url, "/trip-locals", stop, token)
    assert status == 201
    status, _, _ = request(base_url, "/trip-locals", stop, token)
    assert status == 400
    status, _, body = request(base_url, "/trip-locals/trip/" + trip_id, token=token)
    assert status == 200 and json.loads(body)[0]["localId"] == 1
    status, _, _ = request(base_url, "/trip-locals/" + trip_id + "/1", {"day": 2, "orderIndex": 1}, token, "PUT")
    assert status == 200
    status, _, _ = request(base_url, "/trip-locals/" + trip_id + "/1", token=token, method="DELETE")
    assert status == 204
    status, _, _ = request(base_url, "/trip-locals", stop, token)
    assert status == 201
    status, _, _ = request(base_url, "/trips/" + trip_id, token=token, method="DELETE")
    assert status == 204
    status, _, _ = request(base_url, "/trips/" + trip_id, token=token)
    assert status == 404
    print("PASS: trip ownership, validation, creation/update and itinerary mutations")


if __name__ == "__main__":
    main()
