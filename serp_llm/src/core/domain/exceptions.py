"""
Author: QuanTuanHuy
Description: Part of Serp Project - Custom Exceptions
"""


class BaseAppException(Exception):
    """Base exception for application-specific errors"""
    
    def __init__(self, message: str, code: int = 500):
        self.message = message
        self.code = code
        super().__init__(self.message)


class NotFoundError(BaseAppException):
    """Resource not found exception"""
    
    def __init__(self, message: str = "Resource not found"):
        super().__init__(message, code=404)


class BadRequestError(BaseAppException):
    """Bad request exception"""
    
    def __init__(self, message: str = "Bad request"):
        super().__init__(message, code=400)


class UnauthorizedError(BaseAppException):
    """Unauthorized access exception"""
    
    def __init__(self, message: str = "Unauthorized"):
        super().__init__(message, code=401)


class ForbiddenError(BaseAppException):
    """Forbidden access exception"""
    
    def __init__(self, message: str = "Forbidden"):
        super().__init__(message, code=403)


class ConflictError(BaseAppException):
    """Resource conflict exception"""
    
    def __init__(self, message: str = "Conflict"):
        super().__init__(message, code=409)


class ValidationError(BaseAppException):
    """Validation error exception"""
    
    def __init__(self, message: str = "Validation error"):
        super().__init__(message, code=422)


class InternalServerError(BaseAppException):
    """Internal server error exception"""
    
    def __init__(self, message: str = "Internal server error"):
        super().__init__(message, code=500)
