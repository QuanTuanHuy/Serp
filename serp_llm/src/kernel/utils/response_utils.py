"""
Author: QuanTuanHuy
Description: Part of Serp Project - Response Utilities
"""

from typing import TypeVar, Optional, Any
from fastapi import status

from src.core.domain.dto.response_dto import (
    GeneralResponse,
    ErrorResponse,
    PaginatedData,
    PaginatedResponse,
)


T = TypeVar('T')


class ResponseUtils:
    """
    Utility class for creating standard responses.
    Consistent with Java services ResponseUtils.
    """
    
    @staticmethod
    def success(
        data: Optional[T] = None,
        message: str = "Success",
        code: int = status.HTTP_200_OK
    ) -> GeneralResponse[T]:
        """
        Create a success response.
        
        Args:
            data: Response data
            message: Success message
            code: HTTP status code (default: 200)
            
        Returns:
            GeneralResponse with success status
        """
        return GeneralResponse(
            status="success",
            code=code,
            message=message,
            data=data
        )
    
    @staticmethod
    def error(
        message: str,
        code: int = status.HTTP_400_BAD_REQUEST,
        data: Optional[Any] = None
    ) -> ErrorResponse:
        """
        Create an error response.
        
        Args:
            message: Error message
            code: HTTP status code (default: 400)
            data: Additional error details
            
        Returns:
            ErrorResponse
        """
        return ErrorResponse(
            status="error",
            code=code,
            message=message,
            data=data
        )
    
    @staticmethod
    def created(
        data: Optional[T] = None,
        message: str = "Created successfully"
    ) -> GeneralResponse[T]:
        """
        Create a 201 Created response.
        
        Args:
            data: Created resource data
            message: Success message
            
        Returns:
            GeneralResponse with 201 status
        """
        return ResponseUtils.success(
            data=data,
            message=message,
            code=status.HTTP_201_CREATED
        )
    
    @staticmethod
    def updated(
        data: Optional[T] = None,
        message: str = "Updated successfully"
    ) -> GeneralResponse[T]:
        """
        Create a 200 OK response for updates.
        
        Args:
            data: Updated resource data
            message: Success message
            
        Returns:
            GeneralResponse with 200 status
        """
        return ResponseUtils.success(
            data=data,
            message=message,
            code=status.HTTP_200_OK
        )
    
    @staticmethod
    def deleted(
        message: str = "Deleted successfully"
    ) -> GeneralResponse[None]:
        """
        Create a 200 OK response for deletions.
        
        Args:
            message: Success message
            
        Returns:
            GeneralResponse with 200 status and no data
        """
        return ResponseUtils.success(
            data=None,
            message=message,
            code=status.HTTP_200_OK
        )
    
    @staticmethod
    def no_content(
        message: str = "No content"
    ) -> GeneralResponse[None]:
        """
        Create a 204 No Content response.
        
        Args:
            message: Message
            
        Returns:
            GeneralResponse with 204 status
        """
        return ResponseUtils.success(
            data=None,
            message=message,
            code=status.HTTP_204_NO_CONTENT
        )
    
    @staticmethod
    def paginated(
        items: list[T],
        total: int,
        page: int = 1,
        page_size: int = 20,
        message: str = "Success"
    ) -> PaginatedResponse[T]:
        """
        Create a paginated response matching Java format.
        
        Args:
            items: List of items for current page
            total: Total number of items
            page: Current page number (1-based)
            page_size: Number of items per page
            message: Success message
            
        Returns:
            PaginatedResponse with pagination data
        """
        import math
        total_pages = math.ceil(total / page_size) if page_size > 0 else 0
        
        paginated_data = PaginatedData(
            totalItems=total,
            totalPages=total_pages,
            currentPage=page,
            items=items
        )
        
        return PaginatedResponse(
            status="success",
            code=status.HTTP_200_OK,
            message=message,
            data=paginated_data
        )
    
    @staticmethod
    def bad_request(
        message: str = "Bad request",
        data: Optional[Any] = None
    ) -> ErrorResponse:
        """Create a 400 Bad Request error response"""
        return ResponseUtils.error(
            message=message,
            code=status.HTTP_400_BAD_REQUEST,
            data=data
        )
    
    @staticmethod
    def unauthorized(
        message: str = "Unauthorized",
        data: Optional[Any] = None
    ) -> ErrorResponse:
        """Create a 401 Unauthorized error response"""
        return ResponseUtils.error(
            message=message,
            code=status.HTTP_401_UNAUTHORIZED,
            data=data
        )
    
    @staticmethod
    def forbidden(
        message: str = "Forbidden",
        data: Optional[Any] = None
    ) -> ErrorResponse:
        """Create a 403 Forbidden error response"""
        return ResponseUtils.error(
            message=message,
            code=status.HTTP_403_FORBIDDEN,
            data=data
        )
    
    @staticmethod
    def not_found(
        message: str = "Resource not found",
        data: Optional[Any] = None
    ) -> ErrorResponse:
        """Create a 404 Not Found error response"""
        return ResponseUtils.error(
            message=message,
            code=status.HTTP_404_NOT_FOUND,
            data=data
        )
    
    @staticmethod
    def conflict(
        message: str = "Conflict",
        data: Optional[Any] = None
    ) -> ErrorResponse:
        """Create a 409 Conflict error response"""
        return ResponseUtils.error(
            message=message,
            code=status.HTTP_409_CONFLICT,
            data=data
        )
    
    @staticmethod
    def internal_error(
        message: str = "Internal server error",
        data: Optional[Any] = None
    ) -> ErrorResponse:
        """Create a 500 Internal Server Error response"""
        return ResponseUtils.error(
            message=message,
            code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            data=data
        )


# Global instance
response_utils = ResponseUtils()
