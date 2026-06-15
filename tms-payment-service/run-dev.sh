#!/usr/bin/env bash
# Author: QuanTuanHuy, Description: Part of Serp Project

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

export PAYMENT_SERVICE_PORT="${PAYMENT_SERVICE_PORT:-${SERVER_PORT:-8103}}"
export SERVER_PORT="${SERVER_PORT:-$PAYMENT_SERVICE_PORT}"
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-${DB_URL:-jdbc:postgresql://localhost:5432/payment_service}}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-${DB_USERNAME:-serp}}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-${DB_PASSWORD:-serp123}}"
export KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
export KAFKA_BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"
export SPRING_DATA_REDIS_HOST="${SPRING_DATA_REDIS_HOST:-localhost}"
export SPRING_DATA_REDIS_PORT="${SPRING_DATA_REDIS_PORT:-6379}"

echo ""
echo "Starting Payment Service in development mode..."
echo "PAYMENT_SERVICE_PORT=$PAYMENT_SERVICE_PORT"
echo "SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL"
echo "KEYCLOAK_URL=$KEYCLOAK_URL"
echo "KAFKA_BOOTSTRAP_SERVERS=$KAFKA_BOOTSTRAP_SERVERS"
echo "SPRING_DATA_REDIS_HOST=$SPRING_DATA_REDIS_HOST"
echo "SPRING_DATA_REDIS_PORT=$SPRING_DATA_REDIS_PORT"
echo ""

./mvnw spring-boot:run
