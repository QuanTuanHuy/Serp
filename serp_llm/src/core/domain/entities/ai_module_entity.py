"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Module Entity
"""

from typing import Optional, Dict, Any
from pydantic import Field

from src.core.domain.entities.base_entity import BaseEntity


class AIModuleEntity(BaseEntity):
    """AI Module entity - represents a SERP module (CRM, PTM, Sales)"""
    
    code: str = Field(..., description="Module code (crm, ptm, sales)")
    name: str = Field(..., description="Module display name")
    description: Optional[str] = Field(None, description="Module description")
    icon: Optional[str] = Field(None, description="Module icon name")
    enabled: bool = Field(True, description="Whether module is enabled")
    config: Dict[str, Any] = Field(default_factory=dict, description="Module configuration")
    
    def is_enabled(self) -> bool:
        """Check if module is enabled"""
        return self.enabled
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "code": "crm",
                "name": "CRM",
                "description": "Customer Relationship Management",
                "icon": "users",
                "enabled": True,
                "config": {"max_conversations": 100},
            }
        }
