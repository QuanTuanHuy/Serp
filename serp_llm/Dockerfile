# Author: QuanTuanHuy
# Description: Part of Serp Project - Production Dockerfile

FROM python:3.12-slim

LABEL maintainer="QuanTuanHuy"
LABEL description="SERP LLM Service - AI Assistant for ERP System"

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    libpq-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --no-cache-dir poetry==1.8.0

COPY pyproject.toml poetry.lock* ./

RUN poetry config virtualenvs.create false

RUN poetry install --no-interaction --no-ansi --no-root --only main

COPY . .
EXPOSE 8089

CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8089", "--workers", "1"]
