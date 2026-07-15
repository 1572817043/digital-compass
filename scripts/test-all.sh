#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_URL="${BACKEND_URL:-http://localhost:8081}"
FRONTEND_URL="${E2E_BASE_URL:-http://127.0.0.1:5175}"
BACKEND_PID=""

cleanup() {
  if [[ -n "$BACKEND_PID" ]]; then
    kill "$BACKEND_PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

wait_backend() {
  for _ in {1..60}; do
    if curl -fsS "$BACKEND_URL/api/products" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  return 1
}

cd "$ROOT_DIR/backend"
./mvnw test

if ! curl -fsS "$BACKEND_URL/api/products" >/dev/null 2>&1; then
  ./mvnw spring-boot:run >/tmp/digicompass-e2e-backend.log 2>&1 &
  BACKEND_PID="$!"
  wait_backend || {
    tail -n 120 /tmp/digicompass-e2e-backend.log || true
    exit 1
  }
fi

cd "$ROOT_DIR/frontend"
npm run build
E2E_BASE_URL="$FRONTEND_URL" npm run test:e2e
