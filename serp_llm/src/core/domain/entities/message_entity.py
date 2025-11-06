# Author: QuanTuanHuy
# Description: Part of Serp Project - Message Entity

from pydantic import Field

from src.core.domain.entities.base_entity import BaseEntity
from src.core.domain.enums import MessageRole


class MessageEntity(BaseEntity):
    """Message domain entity"""
    
    conversation_id: int = Field(..., description="Conversation ID")
    role: MessageRole = Field(..., description="Message role (user/assistant/system)")
    content: str = Field(..., description="Message content")
    tokens_used: int = Field(default=0, description="Number of tokens used")
    metadata: dict = Field(default_factory=dict, description="Additional metadata")
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "conversation_id": 123,
                "role": "user",
                "content": "Analyze my top customers",
                "tokens_used": 50,
                "metadata": {},
            }
        }
