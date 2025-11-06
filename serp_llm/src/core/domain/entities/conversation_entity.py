# Author: QuanTuanHuy
# Description: Part of Serp Project - Conversation Entity

from typing import Optional
from pydantic import Field

from src.core.domain.entities.base_entity import BaseEntity
from src.core.domain.enums import ContextType, ModelType


class ConversationEntity(BaseEntity):
    """Conversation domain entity"""
    
    user_id: int = Field(..., description="User ID who owns the conversation")
    tenant_id: int = Field(..., description="Tenant ID")
    title: Optional[str] = Field(default=None, description="Conversation title")
    context_type: Optional[ContextType] = Field(
        default=ContextType.GENERAL,
        description="Type of context"
    )
    context_id: Optional[int] = Field(
        default=None,
        description="ID of related entity (customer, task, etc.)"
    )
    model_type: Optional[ModelType] = Field(
        default=None,
        description="LLM model used in conversation"
    )
    metadata: dict = Field(default_factory=dict, description="Additional metadata")
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "user_id": 123,
                "tenant_id": 456,
                "title": "Customer Analysis",
                "context_type": "crm",
                "context_id": 789,
                "model_type": "gpt-4-turbo",
                "metadata": {"tags": ["analysis", "customer"]},
            }
        }
