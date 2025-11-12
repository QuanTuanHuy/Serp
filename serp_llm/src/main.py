# Author: QuanTuanHuy
# Description: Part of Serp Project - Main FastAPI Application

import sys
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger
from pydantic import ValidationError

from src.config import settings
from src.infrastructure.db import init_db, close_db
from src.ui.api.v1.router import api_router, api_v1_router
from src.ui.middleware import (
    LoggingMiddleware,
    JWTAuthMiddleware,
    app_exception_handler,
    http_exception_handler,
    validation_exception_handler,
    pydantic_validation_exception_handler,
    general_exception_handler,
    value_error_handler,
    permission_error_handler,
    not_found_error_handler,
)
from src.core.domain.exceptions import (
    BaseAppException,
    NotFoundError,
)


# Configure loguru logger
logger.remove()
if settings.log_format == "json":
    logger.add(
        sys.stdout,
        format="{time:YYYY-MM-DD HH:mm:ss} | {level} | {message}",
        level=settings.log_level,
        serialize=True,  # JSON output
    )
else:
    logger.add(
        sys.stdout,
        format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>",
        level=settings.log_level,
    )


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application lifespan - startup and shutdown events
    """
    logger.info("Starting SERP LLM Service...")
    logger.info(f"Environment: {settings.environment}")
    logger.info(f"Debug mode: {settings.debug}")
    
    try:
        await init_db()
        logger.info("Database initialized successfully")
    except Exception as e:
        logger.error(f"Failed to initialize database: {e}")
        raise
    
    logger.info(f"SERP LLM Service started on {settings.host}:{settings.port}")
    
    yield
    
    logger.info("Shutting down SERP LLM Service...")
    await close_db()
    logger.info("SERP LLM Service stopped")


app = FastAPI(
    title="SERP LLM Service",
    description="AI Assistant Service for SERP ERP System",
    version="0.1.0",
    docs_url="/docs" if settings.debug else None,  # Disable docs in production
    redoc_url="/redoc" if settings.debug else None,
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"] if settings.is_development else [],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.add_middleware(JWTAuthMiddleware)

app.add_middleware(LoggingMiddleware)

# Register exception handlers
app.add_exception_handler(BaseAppException, app_exception_handler)
app.add_exception_handler(NotFoundError, not_found_error_handler)
app.add_exception_handler(HTTPException, http_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(ValidationError, pydantic_validation_exception_handler)
app.add_exception_handler(ValueError, value_error_handler)
app.add_exception_handler(PermissionError, permission_error_handler)
app.add_exception_handler(Exception, general_exception_handler)

# Register routers
app.include_router(api_router)
app.include_router(api_v1_router)


if __name__ == "__main__":
    import uvicorn
    
    uvicorn.run(
        "src.main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.is_development,
        log_level=settings.log_level.lower(),
    )
