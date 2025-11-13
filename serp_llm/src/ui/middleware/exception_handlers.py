"""
Author: QuanTuanHuy
Description: Part of Serp Project - Global Exception Handlers
"""

from fastapi import Request, status
from fastapi.exceptions import RequestValidationError, HTTPException
from fastapi.responses import JSONResponse
from loguru import logger
from pydantic import ValidationError

from src.kernel.utils.response_utils import response_utils
from src.core.domain.dto import ErrorResponse
from src.core.domain.exceptions import (
    BaseAppException,
    NotFoundError,
    BadRequestError,
    UnauthorizedError,
    ForbiddenError,
    ConflictError,
)


async def app_exception_handler(request: Request, exc: BaseAppException) -> JSONResponse:
    """
    Handle custom application exceptions.
    
    Args:
        request: FastAPI request
        exc: BaseAppException
        
    Returns:
        JSONResponse with standard error format
    """
    logger.warning(
        f"App exception {exc.code} at {request.method} {request.url.path}: {exc.message}"
    )
    
    error_response = response_utils.error(
        message=exc.message,
        code=exc.code,
        data=None
    )
    
    return JSONResponse(
        status_code=exc.code,
        content=error_response.model_dump()
    )


async def http_exception_handler(request: Request, exc: HTTPException) -> JSONResponse:
    """
    Handle HTTP exceptions and return standard error response.
    
    Args:
        request: FastAPI request
        exc: HTTPException
        
    Returns:
        JSONResponse with standard error format
    """
    logger.warning(
        f"HTTP {exc.status_code} error at {request.method} {request.url.path}: {exc.detail}"
    )
    
    error_response = response_utils.error(
        message=str(exc.detail),
        code=exc.status_code,
        data=None
    )
    
    return JSONResponse(
        status_code=exc.status_code,
        content=error_response.model_dump()
    )


async def validation_exception_handler(
    request: Request, 
    exc: RequestValidationError
) -> JSONResponse:
    """
    Handle request validation errors and return standard error response.
    
    Args:
        request: FastAPI request
        exc: RequestValidationError
        
    Returns:
        JSONResponse with standard error format
    """
    errors = exc.errors()
    error_messages = []
    
    for error in errors:
        loc = " -> ".join(str(l) for l in error["loc"])
        msg = error["msg"]
        error_messages.append(f"{loc}: {msg}")
    
    error_detail = "; ".join(error_messages)
    
    logger.warning(
        f"Validation error at {request.method} {request.url.path}: {error_detail}"
    )
    
    error_response = response_utils.bad_request(
        message="Validation error",
        data={
            "errors": errors,
            "detail": error_detail
        }
    )
    
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content=error_response.model_dump()
    )


async def pydantic_validation_exception_handler(
    request: Request,
    exc: ValidationError
) -> JSONResponse:
    """
    Handle Pydantic validation errors.
    
    Args:
        request: FastAPI request
        exc: ValidationError
        
    Returns:
        JSONResponse with standard error format
    """
    errors = exc.errors()
    error_messages = []
    
    for error in errors:
        loc = " -> ".join(str(l) for l in error["loc"])
        msg = error["msg"]
        error_messages.append(f"{loc}: {msg}")
    
    error_detail = "; ".join(error_messages)
    
    logger.warning(
        f"Pydantic validation error at {request.method} {request.url.path}: {error_detail}"
    )
    
    error_response = response_utils.bad_request(
        message="Data validation error",
        data={
            "errors": errors,
            "detail": error_detail
        }
    )
    
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content=error_response.model_dump()
    )


async def general_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """
    Handle all uncaught exceptions and return standard error response.
    
    Args:
        request: FastAPI request
        exc: Exception
        
    Returns:
        JSONResponse with standard error format
    """
    logger.error(
        f"Unhandled exception at {request.method} {request.url.path}: "
        f"{type(exc).__name__}: {str(exc)}",
        exc_info=True
    )
    
    # In production, don't expose internal error details
    from src.config import settings
    if settings.is_production:
        error_message = "Internal server error"
        error_data = None
    else:
        error_message = f"{type(exc).__name__}: {str(exc)}"
        error_data = {
            "type": type(exc).__name__,
            "detail": str(exc)
        }
    
    error_response = response_utils.internal_error(
        message=error_message,
        data=error_data
    )
    
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content=error_response.model_dump()
    )


async def value_error_handler(request: Request, exc: ValueError) -> JSONResponse:
    """
    Handle ValueError exceptions.
    
    Args:
        request: FastAPI request
        exc: ValueError
        
    Returns:
        JSONResponse with standard error format
    """
    logger.warning(
        f"ValueError at {request.method} {request.url.path}: {str(exc)}"
    )
    
    error_response = response_utils.bad_request(
        message=str(exc),
        data=None
    )
    
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content=error_response.model_dump()
    )


async def permission_error_handler(request: Request, exc: PermissionError) -> JSONResponse:
    """
    Handle PermissionError exceptions.
    
    Args:
        request: FastAPI request
        exc: PermissionError
        
    Returns:
        JSONResponse with standard error format
    """
    logger.warning(
        f"PermissionError at {request.method} {request.url.path}: {str(exc)}"
    )
    
    error_response = response_utils.forbidden(
        message=str(exc) if str(exc) else "Permission denied",
        data=None
    )
    
    return JSONResponse(
        status_code=status.HTTP_403_FORBIDDEN,
        content=error_response.model_dump()
    )


async def not_found_error_handler(request: Request, exc: Exception) -> JSONResponse:
    """
    Handle NotFoundError exceptions (custom exception).
    
    Args:
        request: FastAPI request
        exc: NotFoundError
        
    Returns:
        JSONResponse with standard error format
    """
    logger.warning(
        f"NotFoundError at {request.method} {request.url.path}: {str(exc)}"
    )
    
    error_response = response_utils.not_found(
        message=str(exc) if str(exc) else "Resource not found",
        data=None
    )
    
    return JSONResponse(
        status_code=status.HTTP_404_NOT_FOUND,
        content=error_response.model_dump()
    )
