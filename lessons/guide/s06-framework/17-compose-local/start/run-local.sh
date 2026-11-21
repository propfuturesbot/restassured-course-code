#!/usr/bin/env bash
# One command to a green suite: start both apps, mint a Campus Lab key, run the whole suite on both, stop.
#   ./run-local.sh                    STUDENTS_PORT / CAMPUS_PORT move the ports, KEEP_RUNNING=1 leaves the apps up
# Needs: Docker, Maven, JDK 21, python3. Folder layout: see README.md (students-app and campus-lab next to this one).
set -euo pipefail
cd "$(dirname "$0")"

# ---
# Start (and build, the first time) both containers. --wait returns only when every healthcheck is green,
# so the tests never race a server that is still starting.


# ---
# A fresh Campus Lab key for this run: POST /api/keys needs no login. The key and its Basic / OAuth
# credentials go into environment variables of THIS shell only - never into a file, never on the screen.


# ---
# The same suite, twice: the Students app, then Campus Lab. -Dbase.uri points each run at the local port.
# Keep going after a red run so both results are printed; the exit code says if anything failed.


# ---
# Stop and remove the containers (the Students app forgets its data; the next run gets a new key anyway).
