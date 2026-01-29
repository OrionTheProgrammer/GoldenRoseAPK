#!/usr/bin/env python3
import json
import sys
from urllib.request import urlopen, Request
from urllib.error import URLError, HTTPError


WEAPONS_URL = "https://valorant-api.com/v1/weapons/skins"


def main() -> int:
    request = Request(WEAPONS_URL, headers={"User-Agent": "GoldenRoseAPK/1.0"})
    try:
        with urlopen(request, timeout=20) as response:
            payload = response.read()
    except HTTPError as exc:
        print(f"HTTP error: {exc.code} {exc.reason}", file=sys.stderr)
        return 1
    except URLError as exc:
        print(f"Network error: {exc.reason}", file=sys.stderr)
        return 1

    try:
        data = json.loads(payload)
    except json.JSONDecodeError as exc:
        print(f"Invalid JSON: {exc}", file=sys.stderr)
        return 1

    skins = data.get("data", [])
    print(f"Skins recibidas: {len(skins)}")
    for skin in skins[:5]:
        name = skin.get("displayName", "Sin nombre")
        uuid = skin.get("uuid", "sin-uuid")
        tier = skin.get("contentTierUuid", "sin-tier")
        print(f"- {name} | {uuid} | tier: {tier}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
