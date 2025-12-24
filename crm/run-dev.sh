#!/bin/bash
# Author: QuanTuanHuy, Description: Part of Serp Project

if [ -f .env ]; then
  set -a
  source <(sed -e 's/^\s*export\s\+//g' -e 's/\r$//g' .env)
  set +a
fi

echo ""
echo "Starting CRM in development mode..."
echo ""

./mvnw spring-boot:run