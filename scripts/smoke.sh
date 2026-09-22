#!/usr/bin/env bash
# Quick walkthrough of the main flow against a running `docker compose up` stack.
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "== create a notification =="
CREATE=$(curl -s -X POST "$BASE_URL/api/v1/notifications" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: smoke-test-1' \
  -d '{"channel":"EMAIL","recipient":"ada@example.com","subject":"Hi","body":"Smoke test"}')
echo "$CREATE"
ID=$(echo "$CREATE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

echo
echo "== retry with the same Idempotency-Key (should return the same id, 200) =="
curl -s -i -X POST "$BASE_URL/api/v1/notifications" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: smoke-test-1' \
  -d '{"channel":"EMAIL","recipient":"ada@example.com","subject":"Hi","body":"Smoke test"}' \
  | head -1

echo
echo "== check status a few seconds later (worker should have picked it up) =="
sleep 3
curl -s "$BASE_URL/api/v1/notifications/$ID"

echo
echo
echo "== delivery attempt history =="
curl -s "$BASE_URL/api/v1/notifications/$ID/attempts"

echo
echo
echo "== a request with an invalid recipient (permanent failure, no retries) =="
curl -s -X POST "$BASE_URL/api/v1/notifications" \
  -H 'Content-Type: application/json' \
  -d '{"channel":"EMAIL","recipient":"not-an-email","subject":"Hi","body":"will fail"}'
