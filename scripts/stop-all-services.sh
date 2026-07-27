#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -d .local-dev/pids ]]; then
  echo "No .local-dev/pids directory"
  exit 0
fi

for pidfile in .local-dev/pids/*.pid; do
  [[ -f "$pidfile" ]] || continue
  pid=$(cat "$pidfile")
  name=$(basename "$pidfile" .pid)
  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid" && echo "stopped $name (pid $pid)"
  else
    echo "not running $name"
  fi
  rm -f "$pidfile"
done
