#!/bin/bash

if [ -f .env.prod ]; then
  set -a
  source <(sed -e 's/^\s*export\s\+//g' -e 's/\r$//g' .env.prod)
  set +a
fi

go run src/main.go
