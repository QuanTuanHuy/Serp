"""
Author: QuanTuanHuy
Description: Part of Serp Project - Chat Use Case DTOs
"""

from typing import Optional, List
from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    """Request for chat completion"""
    
    message: str = Field(..., min_length=1, description="User message")
    conversation_id: Optional[int] = Field(None, description="Existing conversation ID (optional)")
    
    module_code: str = Field(..., description="Module code (crm, ptm, sales)")
    context_type: Optional[str] = Field(None, description="Context type (customer, task, etc.)")
    context_id: Optional[int] = Field(None, description="Context ID")
    
    capability_code: Optional[str] = Field("chat", description="Capability code")
    temperature: Optional[float] = Field(None, ge=0, le=2, description="Temperature override")
    max_tokens: Optional[int] = Field(None, ge=1, description="Max tokens override")
    stream: bool = Field(False, description="Stream response")
    
    attachments: List[dict] = Field(default_factory=list, description="File attachments")
    
    class Config:
        json_schema_extra = {
            "example": {
                "message": "Summarize this customer's recent activities",
                "module_code": "crm",
                "context_type": "customer",
                "context_id": 123,
                "capability_code": "chat",
                "stream": False
            }
        }


class ChatResponse(BaseModel):
    """Response for chat completion"""
    
    conversation_id: int = Field(..., description="Conversation ID")
    message_id: int = Field(..., description="Assistant message ID")
    content: str = Field(..., description="Assistant response")
    
    model_used: str = Field(..., description="LLM model used")
    tokens_used: int = Field(..., description="Total tokens consumed")
    processing_time_ms: int = Field(..., description="Processing time in milliseconds")
    
    sources: List[dict] = Field(default_factory=list, description="RAG sources used")
    
    class Config:
        json_schema_extra = {
            "example": {
                "conversation_id": 456,
                "message_id": 789,
                "content": "Based on the recent data, this customer has...",
                "model_used": "gemini-2.0-flash",
                "tokens_used": 350,
                "processing_time_ms": 1250,
                "sources": []
            }
        }
