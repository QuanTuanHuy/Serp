#!/bin/bash
# Author: QuanTuanHuy, Description: Part of Serp Project

echo "Loading environment variables from .env file..."

if [ -f .env ]; then
  set -a
  source <(sed -e 's/^\s*export\s\+//g' -e 's/\r$//g' .env)
  set +a
fi

echo ""
echo "Starting Account Service in development mode..."
echo ""

# On Windows Git Bash, prefer mvnw.cmd to avoid curl/TLS issues in shell wrapper downloads.
if [[ "$(uname -s)" == MINGW* || "$(uname -s)" == CYGWIN* ]]; then
  ./mvnw.cmd spring-boot:run
else
  ./mvnw spring-boot:run
fi
