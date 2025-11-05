#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

( cd "$SCRIPT_DIR/backend" && mvn -q spring-boot:run ) &
BACK_PID=$!

( cd "$SCRIPT_DIR/frontend" && cp -n .env.example .env 2>/dev/null || true && npm install && npm run dev ) &
FRONT_PID=$!

trap "kill $BACK_PID $FRONT_PID 2>/dev/null || true" EXIT
wait
