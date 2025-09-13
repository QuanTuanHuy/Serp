#!/bin/bash
# Author: QuanTuanHuy, Description: Part of Serp Project

echo "Loading environment variables from .env file..."

# Load environment variables from .env file
if [ -f .env ]; then
    # Export each line as environment variable
    while IFS='=' read -r key value || [ -n "$key" ]; do
        # Skip empty lines and comments
        if [[ -n "$key" && ! "$key" =~ ^[[:space:]]*# ]]; then
            # Remove any trailing whitespace and export
            export "$key"="$(echo "$value" | sed 's/[[:space:]]*$//')"
            echo "Set $key=$value"
        fi
    done < .env
else
    echo "Warning: .env file not found!"
fi

echo ""
echo "Starting Account Service in development mode..."
echo ""

./mvnw spring-boot:run