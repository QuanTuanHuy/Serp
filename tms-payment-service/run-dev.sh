#!/usr/bin/env bash
# Author: NguyenTheAnh, Description: Part of Serp Project

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

# --- Cấu hình Port & Server ---
export PAYMENT_SERVICE_PORT="${PAYMENT_SERVICE_PORT:-${SERVER_PORT:-8103}}"
export SERVER_PORT="${SERVER_PORT:-$PAYMENT_SERVICE_PORT}"

# --- Cấu hình Database ---
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-${DB_URL:-jdbc:postgresql://localhost:5432/payment_service}}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-${DB_USERNAME:-serp}}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-${DB_PASSWORD:-serp123}}"

# --- Cấu hình Microservices & Hạ tầng ---
export KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
export KAFKA_BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"
export SPRING_DATA_REDIS_HOST="${SPRING_DATA_REDIS_HOST:-localhost}"
export SPRING_DATA_REDIS_PORT="${SPRING_DATA_REDIS_PORT:-6379}"

# --- Cấu hình Webhook & Integrations (Bổ sung fallback) ---
export TMS_ORDER_PAYMENT_CONFIRMED_WEBHOOK_URL="${TMS_ORDER_PAYMENT_CONFIRMED_WEBHOOK_URL:-http://localhost:8105/api/v1/internal/payment-webhooks/orders/payment-confirmed}"
export TMS_ORDER_PAYMENT_WEBHOOK_SECRET="${TMS_ORDER_PAYMENT_WEBHOOK_SECRET:-change-me}"
export ZALOPAY_CALLBACK_URL="${ZALOPAY_CALLBACK_URL:-http://localhost:8080/payment/api/v1/payments/zalopay/callback}"
export ZALOPAY_REDIRECT_URL="${ZALOPAY_REDIRECT_URL:-http://localhost:3000/payment/result}"

echo ""
echo "Starting Payment Service in development mode..."
echo "PAYMENT_SERVICE_PORT        = $PAYMENT_SERVICE_PORT"
echo "SPRING_DATASOURCE_URL       = $SPRING_DATASOURCE_URL"
echo "SPRING_DATASOURCE_USERNAME   = $SPRING_DATASOURCE_USERNAME"
echo "KEYCLOAK_URL                = $KEYCLOAK_URL"
echo "KAFKA_BOOTSTRAP_SERVERS     = $KAFKA_BOOTSTRAP_SERVERS"
echo "SPRING_DATA_REDIS_HOST      = $SPRING_DATA_REDIS_HOST"
echo "SPRING_DATA_REDIS_PORT      = $SPRING_DATA_REDIS_PORT"
echo "ZALOPAY_CALLBACK_URL        = $ZALOPAY_CALLBACK_URL"
echo "TMS_WEBHOOK_URL             = $TMS_ORDER_PAYMENT_CONFIRMED_WEBHOOK_URL"
echo ""

./mvnw spring-boot:run