#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p .local-dev/logs .local-dev/pids

SERVICES=(account_service products_service cart_service order_service payment_service courier_service delivery_service)

for svc in "${SERVICES[@]}"; do
  if [[ -f ".local-dev/pids/${svc}.pid" ]] && kill -0 "$(cat ".local-dev/pids/${svc}.pid")" 2>/dev/null; then
    echo "skip $svc (already running pid $(cat .local-dev/pids/${svc}.pid))"
    continue
  fi
  echo "starting $svc ..."
  nohup ./mvnw -q -pl "$svc" spring-boot:run > ".local-dev/logs/${svc}.log" 2>&1 &
  echo $! > ".local-dev/pids/${svc}.pid"
done

echo "All services starting. Logs: .local-dev/logs/"
echo "Wait ~30s then open Swagger on ports 8081-8087"
