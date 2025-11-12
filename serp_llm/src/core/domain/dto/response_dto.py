"""
Author: QuanTuanHuy
Description: Part of Serp Project - Standard Response DTOs
"""

from typing import TypeVar, Generic, Optional, Any
from pydantic import BaseModel, Field


T = TypeVar('T')


class GeneralResponse(BaseModel, Generic[T]):
    """
    Standard response format consistent with Java services.
    
    Example:
        {
            "status": "success",
            "code": 200,
            "message": "Operation completed successfully",
            "data": {...}
        }
    """
    status: str = Field(
        default="success",
        description="Response status: success or error"
    )
    code: int = Field(
        default=200,
        description="HTTP status code"
    )
    message: str = Field(
        default="",
        description="Response message"
    )
    data: Optional[T] = Field(
        default=None,
        description="Response data"
    )
    
    class Config:
        json_schema_extra = {
            "example": {
                "status": "success",
                "code": 200,
                "message": "Operation completed successfully",
                "data": {}
            }
        }


class ErrorResponse(BaseModel):
    """
    Standard error response format.
    
    Example:
        {
            "status": "error",
            "code": 400,
            "message": "Validation error",
            "data": null
        }
    """
    status: str = Field(
        default="error",
        description="Always 'error' for error responses"
    )
    code: int = Field(
        description="HTTP status code"
    )
    message: str = Field(
        description="Error message"
    )
    data: Optional[Any] = Field(
        default=None,
        description="Additional error details"
    )
    
    class Config:
        json_schema_extra = {
            "example": {
                "status": "error",
                "code": 400,
                "message": "Bad request",
                "data": None
            }
        }


class PaginatedData(BaseModel, Generic[T]):
    """
    Paginated data wrapper matching Java format.
    
    Format:
        {
            "totalItems": 100,
            "totalPages": 5,
            "currentPage": 1,
            "items": [...]
        }
    """
    totalItems: int = Field(description="Total number of items", alias="totalItems")
    totalPages: int = Field(description="Total number of pages", alias="totalPages")
    currentPage: int = Field(description="Current page number", alias="currentPage")
    items: list[T] = Field(description="List of items")
    
    class Config:
        populate_by_name = True
        json_schema_extra = {
            "example": {
                "totalItems": 100,
                "totalPages": 5,
                "currentPage": 1,
                "items": []
            }
        }


class PaginatedResponse(GeneralResponse[PaginatedData[T]], Generic[T]):
    """Standard paginated response"""
    pass
