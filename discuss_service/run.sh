#!/bin/bash
# Author: QuanTuanHuy, Description: Part of Serp Project

echo "Loading environment variables from .env file..."

if [ -f .env.prod ]; then
  set -a
  source <(sed -e 's/^\s*export\s\+//g' -e 's/\r$//g' .env.prod)
  set +a
fi

echo ""
echo "Starting Discuss Service..."
echo ""

# export SPRING_PROFILES_ACTIVE=prod

./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
