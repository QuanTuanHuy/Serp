"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Capability Entity
"""

from typing import Optional, Dict, Any, List
from pydantic import Field

from src.core.domain.entities.base_entity import BaseEntity
from src.core.domain.enums import ModuleCode, CapabilityType


class AICapabilityEntity(BaseEntity):
    """AI Capability entity - represents an AI feature (chat, summarize, etc.)"""
    
    module_code: ModuleCode = Field(..., description="Module code (crm, ptm, sales)")
    code: str = Field(..., description="Capability code (e.g., 'chat', 'summarize')")
    name: str = Field(..., description="Capability display name")
    description: Optional[str] = Field(None, description="Capability description")
    capability_type: CapabilityType = Field(..., description="Type of capability")
    
    system_prompt: Optional[str] = Field(None, description="System prompt for this capability")
    prompt_template: Optional[str] = Field(None, description="Prompt template with variables")
    
    available_functions: List[Dict[str, Any]] = Field(
        default_factory=list,
        description="Available functions for this capability"
    )
    
    default_model: str = Field(
        default="gemini-2.0-flash",
        description="Default LLM model to use"
    )
    default_temperature: float = Field(
        default=0.7,
        ge=0.0,
        le=2.0,
        description="Default temperature for generation"
    )
    default_max_tokens: int = Field(
        default=4096,
        gt=0,
        description="Default max tokens for generation"
    )
    
    required_permission: Optional[str] = Field(
        None,
        description="Required permission to use this capability"
    )
    
    enabled: bool = Field(True, description="Whether capability is enabled")
    meta_data: Dict[str, Any] = Field(
        default_factory=dict,
        description="Additional metadata"
    )
    
    def is_enabled(self) -> bool:
        """Check if capability is enabled"""
        return self.enabled
    
    def is_chat_type(self) -> bool:
        """Check if this is a chat capability"""
        return self.capability_type == CapabilityType.CHAT
    
    def is_inline_assist(self) -> bool:
        """Check if this is an inline assist capability"""
        return self.capability_type == CapabilityType.INLINE_ASSIST
    
    def has_functions(self) -> bool:
        """Check if capability has function calling enabled"""
        return len(self.available_functions) > 0
    
    def get_full_code(self) -> str:
        """Get full capability code (module.capability)"""
        return f"{self.module_code}.{self.code}"
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "module_code": "crm",
                "code": "chat",
                "name": "CRM Chat Assistant",
                "capability_type": "chat",
                "system_prompt": "You are a CRM assistant...",
                "default_model": "gemini-2.0-flash",
                "default_temperature": 0.7,
                "enabled": True,
            }
        }
