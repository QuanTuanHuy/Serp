"""
Author: QuanTuanHuy
Description: Part of Serp Project - Conversation Entity
"""

from typing import Optional, Dict, Any
from datetime import datetime, timezone
from pydantic import Field

from src.core.domain.entities.base_entity import BaseEntity
from src.core.domain.enums import ModuleCode, ContextType, ConversationStatus


class ConversationEntity(BaseEntity):
    """Conversation entity - represents a chat conversation"""
    
    user_id: int = Field(..., description="User ID who owns the conversation")
    tenant_id: int = Field(..., description="Tenant ID for multi-tenancy")
    
    module_code: ModuleCode = Field(..., description="Module code (crm, ptm, sales)")
    
    context_type: Optional[ContextType] = Field(
        None,
        description="Type of context (customer, task, etc.)"
    )
    context_id: Optional[int] = Field(
        None,
        description="ID of related entity in source service"
    )
    
    title: Optional[str] = Field(None, max_length=500, description="Conversation title")
    capability_code: Optional[str] = Field(
        None,
        max_length=100,
        description="Which AI feature (e.g., 'crm.chat', 'ptm.suggest_subtasks')"
    )
    
    status: ConversationStatus = Field(
        default=ConversationStatus.ACTIVE,
        description="Conversation status"
    )

    meta_data: Dict[str, Any] = Field(
        default_factory=dict,
        description="Module-specific metadata"
    )
    
    deleted_at: Optional[datetime] = Field(None, description="Soft delete timestamp")
    
    def is_active(self) -> bool:
        """Check if conversation is active"""
        return self.status == ConversationStatus.ACTIVE and self.deleted_at is None
    
    def is_archived(self) -> bool:
        """Check if conversation is archived"""
        return self.status == ConversationStatus.ARCHIVED
    
    def is_deleted(self) -> bool:
        """Check if conversation is soft deleted"""
        return self.deleted_at is not None
    
    def has_context(self) -> bool:
        """Check if conversation has context entity"""
        return self.context_type is not None and self.context_id is not None
    
    def get_context_key(self) -> Optional[str]:
        """Get context identifier key (e.g., 'customer:123')"""
        if self.has_context():
            return f"{self.context_type.value}:{self.context_id}"
        return None
    
    def archive(self):
        """Archive this conversation"""
        self.status = ConversationStatus.ARCHIVED
        self.updated_at = datetime.now(timezone.utc)
    
    def soft_delete(self):
        """Soft delete this conversation"""
        self.deleted_at = datetime.now(timezone.utc)
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "user_id": 123,
                "tenant_id": 456,
                "module_code": "crm",
                "context_type": "customer",
                "context_id": 789,
                "title": "Customer Analysis - ABC Corp",
                "capability_code": "crm.chat",
                "status": "active",
                "meta_data": {"customer_name": "ABC Corp", "priority": "high"},
            }
        }
