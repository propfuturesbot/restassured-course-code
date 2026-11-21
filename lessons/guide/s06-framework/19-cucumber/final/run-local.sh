#!/usr/bin/env bash
# One command to a green suite: start both apps, mint a Campus Lab key, run the whole suite on both, stop.
#   ./run-local.sh                    STUDENTS_PORT / CAMPUS_PORT move the ports, KEEP_RUNNING=1 leaves the apps up
# Needs: Docker, Maven, JDK 21, python3. Folder layout: see README.md (students-app and campus-lab next to this one).
set -euo pipefail
cd "$(dirname "$0")"

# --- keep
# Start (and build, the first time) both containers. --wait returns only when every healthcheck is green,
# so the tests never race a server that is still starting.
export STUDENTS_PORT="${STUDENTS_PORT:-8080}" CAMPUS_PORT="${CAMPUS_PORT:-8000}"
export CAMPUS_JWT_SECRET="${CAMPUS_JWT_SECRET:-local-only-jwt-secret-0123456789abcdef}"
docker compose up -d --build --wait

# --- keep
# A fresh Campus Lab key for this run: POST /api/keys needs no login. The key and its Basic / OAuth
# credentials go into environment variables of THIS shell only - never into a file, never on the screen.
eval "$(curl -sf -X POST "http://localhost:$CAMPUS_PORT/api/keys" -H 'Content-Type: application/json' -d '{}' |
  python3 -c 'import json, shlex, sys
k = json.load(sys.stdin); b = k["basicAuth"]; o = k["oauthClient"]
for n, v in [("CAMPUS_API_KEY", k["apiKey"]), ("CAMPUS_BASIC_USER", b["username"]), ("CAMPUS_BASIC_PASSWORD", b["password"]),
             ("CAMPUS_CLIENT_ID", o["clientId"]), ("CAMPUS_CLIENT_SECRET", o["clientSecret"])]:
    print("export " + n + "=" + shlex.quote(v))')"

# --- keep
# The same suite, twice: the Students app, then Campus Lab. -Dbase.uri points each run at the local port.
# Keep going after a red run so both results are printed; the exit code says if anything failed.
status=0
mvn -B test -Denv=docker -Dbase.uri="http://localhost:$STUDENTS_PORT" || status=1
mvn -B test -Denv=campus -Dbase.uri="http://localhost:$CAMPUS_PORT" || status=1

# --- keep
# Stop and remove the containers (the Students app forgets its data; the next run gets a new key anyway).
[ "${KEEP_RUNNING:-0}" = "1" ] || docker compose down
echo "run-local: $([ $status = 0 ] && echo GREEN || echo RED) (Students app :$STUDENTS_PORT, Campus Lab :$CAMPUS_PORT)"
exit $status
