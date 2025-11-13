"""
Author: QuanTuanHuy
Description: Part of Serp Project - DTOs
"""

from src.core.domain.dto.chat_dto import ChatRequest, ChatResponse
from src.core.domain.dto.response_dto import (
    GeneralResponse,
    ErrorResponse,
    PaginatedData,
    PaginatedResponse,
)

__all__ = [
    "ChatRequest",
    "ChatResponse",
    "GeneralResponse",
    "ErrorResponse",
    "PaginatedData",
    "PaginatedResponse",
]
