"""
Author: QuanTuanHuy
Description: Part of Serp Project - Middleware
"""

from src.ui.middleware.logging_middleware import LoggingMiddleware
from src.ui.middleware.jwt_middleware import JWTAuthMiddleware, RoleBasedAccessMiddleware
from src.ui.middleware.exception_handlers import (
    app_exception_handler,
    http_exception_handler,
    validation_exception_handler,
    pydantic_validation_exception_handler,
    general_exception_handler,
    value_error_handler,
    permission_error_handler,
    not_found_error_handler,
)

__all__ = [
    "LoggingMiddleware",
    "JWTAuthMiddleware",
    "RoleBasedAccessMiddleware",
    "app_exception_handler",
    "http_exception_handler",
    "validation_exception_handler",
    "pydantic_validation_exception_handler",
    "general_exception_handler",
    "value_error_handler",
    "permission_error_handler",
    "not_found_error_handler",
]
