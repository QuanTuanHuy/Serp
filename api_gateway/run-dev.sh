#!/bin/bash

if [ -f .env ]; then
  set -a
  source <(sed -e 's/^\s*export\s\+//g' -e 's/\r$//g' .env)
  set +a
fi

CGO_ENABLED=0 go run src/main.go
