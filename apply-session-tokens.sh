#!/bin/bash
set -euo pipefail
REPO="${1:-/Users/jacob/Developer/Tatakae/tatakae-api}"
cd "$REPO"

# Branch from current HEAD (keep SSE)
git checkout -B feature/session-tokens

if [ -f session-tokens-sync.tgz ]; then
  tar xzf session-tokens-sync.tgz
fi
if [ -f DELETE_FILES.txt ]; then
  while read -r line; do
    case "$line" in
      DELETE\ *) rm -f "${line#DELETE }" ;;
    esac
  done < DELETE_FILES.txt
  rm -f DELETE_FILES.txt session-tokens-sync.tgz
fi

# JWT_SECRET in .env (do not commit .env)
touch .env
if ! grep -q '^JWT_SECRET=' .env; then
  SECRET=$(openssl rand -hex 32)
  printf '\nJWT_SECRET=%s\nJWT_ACCESS_TTL_SECONDS=900\nJWT_REFRESH_TTL_SECONDS=5184000\n' "$SECRET" >> .env
  echo "Added JWT_SECRET to .env"
else
  echo "JWT_SECRET already in .env"
fi

# Stop previous API
pkill -f 'spring-boot:run' 2>/dev/null || true
pkill -f 'tatakae-api-1.0.0.jar' 2>/dev/null || true
sleep 1

# Ensure Postgres
if command -v docker >/dev/null 2>&1; then
  docker compose up -d 2>/dev/null || true
fi

# Load .env into environment for Spring placeholders
set -a
# shellcheck disable=SC1091
source .env
set +a

nohup ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=dev \
  > /tmp/tatakae-api.log 2>&1 &
echo "spring-boot starting pid $!"

for i in $(seq 1 90); do
  if curl -sf http://127.0.0.1:8080/healthcheck >/dev/null 2>&1; then
    echo "LIVE healthcheck 200 on :8080"
    exit 0
  fi
  sleep 2
done
echo "FAILED to become healthy; last log:"
tail -80 /tmp/tatakae-api.log || true
exit 1
