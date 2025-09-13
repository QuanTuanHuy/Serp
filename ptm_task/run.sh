#!/bin/bash
# Author: QuanTuanHuy, Description: Part of Serp Project

echo "Loading environment variables from .env file..."

if [ -f .env ]; then
    while IFS='=' read -r key value || [ -n "$key" ]; do
        if [[ -n "$key" && ! "$key" =~ ^[[:space:]]*# ]]; then
            export "$key"="$(echo "$value" | sed 's/[[:space:]]*$//')"
            echo "Set $key=$value"
        fi
    done < .env
else
    echo "Warning: .env file not found!"
fi

echo ""
echo "Building PTM Task Service..."
echo ""

# Build the Go application
go build -o bin/ptm_task src/main.go

if [ $? -eq 0 ]; then
    echo ""
    echo "Starting PTM Task Service on port 8083..."
    echo ""
    
    # Run the compiled binary
    ./bin/ptm_task
else
    echo "Build failed!"
    exit 1
fi