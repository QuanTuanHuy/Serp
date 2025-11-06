#!/bin/bash
# Author: QuanTuanHuy
# Description: Part of Serp Project - Development Run Script

# Exit on error
set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting SERP LLM Service in Development Mode...${NC}"

# Check if .env exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env file not found. Creating from .env.example...${NC}"
    cp .env.example .env
    echo -e "${YELLOW}Please update .env with your actual values!${NC}"
fi

# Load environment variables
set -a
source .env
set +a

# Check if poetry is installed
if ! command -v poetry &> /dev/null; then
    echo -e "${YELLOW}Poetry not found. Installing dependencies with pip...${NC}"
    pip install -r requirements.txt 2>/dev/null || echo "No requirements.txt found. Please install poetry first."
    exit 1
fi

# Install dependencies if needed
echo -e "${GREEN}Checking dependencies...${NC}"
poetry install

# Run database migrations
echo -e "${GREEN}Running database migrations...${NC}"
poetry run alembic upgrade head || echo -e "${YELLOW}No migrations to run or alembic not configured yet${NC}"

# Start the application with auto-reload
echo -e "${GREEN}Starting FastAPI server on ${HOST}:${PORT}...${NC}"

# Convert LOG_LEVEL to lowercase for uvicorn CLI
LOG_LEVEL_LOWER=$(echo "${LOG_LEVEL:-info}" | tr '[:upper:]' '[:lower:]')

poetry run uvicorn src.main:app \
    --host ${HOST:-0.0.0.0} \
    --port ${PORT:-8089} \
    --reload \
    --log-level ${LOG_LEVEL_LOWER}
