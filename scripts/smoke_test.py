import argparse
import json
from urllib.error import HTTPError
from urllib.request import Request, urlopen


def request(base_url, path, body=None, token=None):
    headers = {"Accept": "application/json"}
    data = None
    if body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body).encode()
    if token:
        headers["Authorization"] = token
    try:
        with urlopen(Request(base_url + path, data=data, headers=headers), timeout=15) as response:
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


if __name__ == "__main__":
    main()
