#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "Loading environment variables from .env file..."

if [ -f .env ]; then
  set -a
  source <(sed -e 's/^\s*export\s\+//g' -e 's/\r$//g' .env)
  set +a
else
  echo "Warning: .env file not found"
fi

echo ""
echo "Starting TTCRS in development mode..."
echo ""

if [[ "$(uname -s)" == MINGW* || "$(uname -s)" == CYGWIN* ]]; then
  ./mvnw.cmd spring-boot:run
else
  ./mvnw spring-boot:run
fi
