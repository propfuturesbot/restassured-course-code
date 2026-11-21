#!/usr/bin/env bash
# Start the Students app on JDK 17 or newer. Port 8080 by default: ./run.sh 8085 to use another one.
cd "$(dirname "$0")"
java -jar studentApp-java21.jar --server.port="${1:-8080}"
