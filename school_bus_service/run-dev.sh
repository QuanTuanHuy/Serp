#!/bin/bash
# Author: Codex, Description: Development runner for SERP School Bus Service

set -e

echo "Loading environment variables from .env file..."

if [ -f .env ]; then
  set -a
  source <(sed -e 's/^\s*export\s\+//g' -e 's/\r$//g' .env)
  set +a
fi

APP_PORT="${SERVER_PORT:-8094}"
DB_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/serp_school_bus}"

echo ""
echo "Starting School Bus Service in development mode..."
echo "Port: ${APP_PORT}"
echo "Datasource: ${DB_URL}"
echo ""

if [ -x "./mvnw" ]; then
  ./mvnw spring-boot:run
else
  mvn spring-boot:run
fi
