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

    status, _, _ = request(base_url, "/auth/login")
    assert status == 405, f"GET login returned {status}, expected 405"
    status, _, _ = request(base_url, "/auth/login", {})
    assert status == 400, f"Missing login fields returned {status}, expected 400"
    for rejected_token in (None, "Bearer invalid"):
        status, _, _ = request(base_url, "/trips", token=rejected_token)
        assert status == 401, f"Missing or invalid token returned {status}, expected 401"
    status, _, _ = request(base_url, "/cities", token="Bearer invalid")
    assert status == 200, f"Invalid token blocked the public catalog with {status}"
    print("PASS: login method, required fields and authentication boundary")

    status, _, _ = request(base_url, "/auth/login", {
        "email": "rafael@email.com", "senha": "invalid-password"
    })
    assert status == 401, f"Invalid credentials returned {status}, expected 401"
    print("PASS: invalid credentials return 401")

    status, headers, _ = request(base_url, "/auth/login", {
        "email": "rafael@email.com", "senha": "senha123"
    }, token="Bearer invalid")
    assert status == 200
    token = headers.get("Authorization", "")
    assert token.startswith("Bearer ")
    status, _, _ = request(base_url, "/trips", token=token)
    assert status == 200
    status, _, _ = request(base_url, "/api/auth/login", {}, token=token)
    assert status == 404, f"Unknown route returned {status}, expected 404"
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
    stop_path = f"/trip-locals/{trip_id}/1"
    schedule_path = stop_path + "/schedule"
    status, _, body = request(base_url, stop_path, token=token)
    assert status == 200 and json.loads(body)["scheduledTime"] is None and not json.loads(body)["completed"]
    schedule = {"scheduledTime": "09:05", "completed": True}
    for invalid in ({"scheduledTime": "24:00", "completed": True}, {"scheduledTime": "09:05"}):
        status, _, _ = request(base_url, schedule_path, invalid, token, "PUT")
        assert status == 400, status
    status, _, _ = request(base_url, "/trip-locals/2/3/schedule", schedule, token, "PUT")
    assert status == 403, status
    status, _, _ = request(base_url, schedule_path, schedule, method="PUT")
    assert status == 401, status
    status, _, body = request(base_url, schedule_path, schedule, token, "PUT")
    assert status == 200, (status, body)
    assert json.loads(body)["scheduledTime"] == "09:05" and json.loads(body)["completed"]
    status, _, _ = request(base_url, stop_path, {"day": 1, "orderIndex": 2}, token, "PUT")
    assert status == 200
    status, _, body = request(base_url, stop_path, token=token)
    assert status == 200 and json.loads(body)["scheduledTime"] == "09:05" and json.loads(body)["completed"]
    status, _, body = request(base_url, schedule_path, {"completed": False}, token, "PUT")
    assert status == 200 and json.loads(body)["scheduledTime"] is None and not json.loads(body)["completed"]
    print("PASS: stop schedule validation, ownership, persistence, legacy update and reopening")

    note_path = f"/trips/{trip_id}/notes"
    memo_list = f"/trip-memos/trip-locals/{trip_id}/1/memos"
    status, headers, body = request(base_url, note_path, {"id": 1, "tripId": 2, "note": "Owner note"}, token)
    assert status == 201, (status, body)
    note = json.loads(body)
    note_id = note["id"]
    assert note_id != 1 and note["tripId"] == int(trip_id)
    assert headers["Location"].endswith(f"{note_path}/{note_id}")
    status, _, body = request(base_url, note_path, token=token)
    assert status == 200
    note_entries = next(iter(json.loads(body)["_embedded"].values()))
    assert note_entries[0]["note"] == "Owner note"
    for method in ("GET", "PUT", "DELETE"):
        payload = {"note": "Wrong trip"} if method == "PUT" else None
        status, _, _ = request(base_url, f"/trips/1/notes/{note_id}", payload, token, method)
        assert status == 404, (method, status)

    status, _, body = request(base_url, "/trip-memos", {**stop, "id": 1, "note": "Owner memo"}, token)
    assert status == 201, (status, body)
    memo_id = json.loads(body)["id"]
    assert memo_id != 1
    status, _, body = request(base_url, memo_list, token=token)
    assert status == 200 and json.loads(body)[0]["note"] == "Owner memo"
    status, headers, _ = request(base_url, "/auth/login", {"email": "maria@email.com", "senha": "senha123"})
    assert status == 200
    other_token = headers["Authorization"]
    for method, path, payload in [
        ("GET", note_path, None),
        ("POST", note_path, {"note": "Forbidden"}),
        ("GET", f"{note_path}/{note_id}", None),
        ("PUT", f"{note_path}/{note_id}", {"note": "Forbidden"}),
        ("DELETE", f"{note_path}/{note_id}", None),
        ("GET", memo_list, None),
        ("POST", "/trip-memos", {**stop, "note": "Forbidden"}),
        ("GET", f"/trip-memos/{memo_id}", None),
        ("PUT", f"/trip-memos/{memo_id}", {"note": "Forbidden"}),
        ("DELETE", f"/trip-memos/{memo_id}", None),
    ]:
        status, _, _ = request(base_url, path, payload, other_token, method)
        assert status == 403, (method, path, status)
    status, _, body = request(base_url, "/trip-memos", token=other_token)
    assert status == 200 and json.loads(body)["page"]["totalElements"] == 0
    status, _, _ = request(base_url, f"{note_path}/{note_id}", {"note": "Edited"}, token, "PUT")
    assert status == 200
    status, _, _ = request(base_url, f"/trip-memos/{memo_id}", {"note": "Edited"}, token, "PUT")
    assert status == 200
    status, _, _ = request(base_url, f"{note_path}/{note_id}", token=token, method="DELETE")
    assert status == 204
    status, _, _ = request(base_url, f"/trip-memos/{memo_id}", token=token, method="DELETE")
    assert status == 204
    print("PASS: note and memo ownership, wrong-trip IDs, scoped lists and protected writes")

    status, _, _ = request(base_url, "/trip-locals/" + trip_id + "/1", token=token, method="DELETE")
    assert status == 204
    status, _, _ = request(base_url, "/trip-locals", stop, token)
    assert status == 201
    status, _, _ = request(base_url, "/trips/" + trip_id, token=token, method="DELETE")
    assert status == 204
    status, _, _ = request(base_url, "/trips/" + trip_id, token=token)
    assert status == 404
    print("PASS: trip ownership, validation, creation/update and itinerary mutations")

    status, _, body = request(base_url, "/users", token=token)
    assert status == 200
    users = json.loads(body)
    assert users["page"]["totalElements"] == 1
    assert "passwordHash" not in body.decode()
    status, _, body = request(base_url, "/users/1", token=token)
    assert status == 200 and json.loads(body)["email"] == "rafael@email.com"
    assert "passwordHash" not in body.decode()
    for method, payload in [("GET", None), ("PUT", {"name": "Unauthorized"}), ("DELETE", None)]:
        status, _, _ = request(base_url, "/users/2", payload, token, method)
        assert status == 403
    status, headers, _ = request(base_url, "/users", {"id": 2, "name": "Unauthorized"}, token)
    assert status == 405 and "GET" in headers.get("Allow", "")
    print("PASS: account privacy, password hash omission and cross-user rejection")


if __name__ == "__main__":
    main()
