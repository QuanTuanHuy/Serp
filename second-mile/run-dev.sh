#!/usr/bin/env bash
# Author: Nguyen The Anh, Description: Part of Serp Project

set -e

echo "Loading environment variables from .env file..."

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  source <(sed -e 's/^\s*export\s\+//g' -e 's/\r$//g' .env)
  set +a
else
  echo "Warning: .env file not found; using application defaults."
fi

export SERVER_PORT="${SERVER_PORT:-8102}"
export DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/second-mile}"
export DB_USERNAME="${DB_USERNAME:-serp}"
export DB_PASSWORD="${DB_PASSWORD:-serp123}"
export KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
export KAFKA_BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"

echo ""
echo "Starting Second-Mile Service in development mode..."
echo "SERVER_PORT=$SERVER_PORT"
echo "DB_URL=$DB_URL"
echo "KEYCLOAK_URL=$KEYCLOAK_URL"
echo "KAFKA_BOOTSTRAP_SERVERS=$KAFKA_BOOTSTRAP_SERVERS"
echo ""

./mvnw spring-boot:run
