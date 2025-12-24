#!/bin/bash
# Author: QuanTuanHuy, Description: Part of Serp Project

if [ -f .env.prod ]; then
  set -a
  source <(sed -e 's/^\s*export\s\+//g' -e 's/\r$//g' .env.prod)
  set +a
fi

echo ""
echo "Starting CRM in production mode..."
echo ""

./mvnw spring-boot:run -Dspring-boot.run.profiles=prod