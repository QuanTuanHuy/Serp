"""
Author: QuanTuanHuy
Description: Part of Serp Project - Message Entity
"""

from typing import Optional, Dict, Any, List
from pydantic import Field

from src.core.domain.entities.base_entity import BaseEntity
from src.core.domain.enums import MessageRole, ContentType


class MessageEntity(BaseEntity):
    """Message entity - represents a message in a conversation"""
    
    conversation_id: int = Field(..., description="Conversation ID")
    
    role: MessageRole = Field(..., description="Message role (user/assistant/system/function)")
    content: str = Field(..., description="Message content")
    content_type: ContentType = Field(
        default=ContentType.TEXT,
        description="Content type (text/image/audio/file)"
    )
    
    attachments: List[Dict[str, Any]] = Field(
        default_factory=list,
        description="Attachments (images, files, etc.)"
    )
    
    function_call: Optional[Dict[str, Any]] = Field(
        None,
        description="Function call information if this message invoked a function"
    )
    
    tokens_used: int = Field(default=0, ge=0, description="Number of tokens used")
    model_used: Optional[str] = Field(None, description="LLM model used for this message")
    processing_time_ms: Optional[int] = Field(
        None,
        ge=0,
        description="Processing time in milliseconds"
    )
    
    sources: List[Dict[str, Any]] = Field(
        default_factory=list,
        description="RAG sources used for this response"
    )
    
    meta_data: Dict[str, Any] = Field(
        default_factory=dict,
        description="Additional metadata"
    )
    
    def is_user_message(self) -> bool:
        """Check if this is a user message"""
        return self.role == MessageRole.USER
    
    def is_assistant_message(self) -> bool:
        """Check if this is an assistant message"""
        return self.role == MessageRole.ASSISTANT
    
    def is_system_message(self) -> bool:
        """Check if this is a system message"""
        return self.role == MessageRole.SYSTEM
    
    def is_function_call(self) -> bool:
        """Check if this message includes function calling"""
        return self.function_call is not None
    
    def has_sources(self) -> bool:
        """Check if this message has RAG sources"""
        return len(self.sources) > 0
    
    def has_attachments(self) -> bool:
        """Check if this message has attachments"""
        return len(self.attachments) > 0
    
    def is_multimodal(self) -> bool:
        """Check if this is a multimodal message"""
        return self.content_type != ContentType.TEXT or self.has_attachments()
    
    def add_source(
        self,
        source_type: str,
        source_id: int,
        title: str,
        relevance_score: float,
        chunk_ids: Optional[List[int]] = None
    ):
        """Add a RAG source to this message"""
        self.sources.append({
            "type": source_type,
            "id": source_id,
            "title": title,
            "relevance_score": relevance_score,
            "chunk_ids": chunk_ids or []
        })
    
    def add_attachment(
        self,
        attachment_type: str,
        url: str,
        name: str,
        size: Optional[int] = None
    ):
        """Add an attachment to this message"""
        self.attachments.append({
            "type": attachment_type,
            "url": url,
            "name": name,
            "size": size
        })
    
    def to_llm_message(self) -> Dict[str, Any]:
        """Convert to LLM API format (OpenAI/Gemini compatible)"""
        msg = {
            "role": self.role.value,
            "content": self.content
        }
        
        if self.function_call:
            msg["function_call"] = self.function_call
        
        return msg
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "conversation_id": 123,
                "role": "assistant",
                "content": "Based on the data, your top customers are...",
                "content_type": "text",
                "tokens_used": 150,
                "model_used": "gemini-2.0-flash",
                "processing_time_ms": 1200,
                "sources": [
                    {
                        "type": "customer",
                        "id": 789,
                        "title": "ABC Corp",
                        "relevance_score": 0.95,
                        "chunk_ids": [1, 2, 3]
                    }
                ],
            }
        }
