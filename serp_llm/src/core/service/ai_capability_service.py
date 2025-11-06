"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Capability Service
"""

from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from loguru import logger

from src.core.domain.entities import AICapabilityEntity
from src.core.domain.enums import ModuleCode, CapabilityType
from src.core.port.store import IAICapabilityPort


class AICapabilityService:
    """Service for AI Capability business logic"""
    
    def __init__(self, ai_capability_port: IAICapabilityPort):
        self.ai_capability_port = ai_capability_port
    
    async def get_capability(
        self,
        db: AsyncSession,
        module_code: ModuleCode,
        capability_code: str
    ) -> Optional[AICapabilityEntity]:
        """Get capability by module and code with validation"""
        capability = await self.ai_capability_port.get_by_code(db, module_code, capability_code)
        
        if not capability:
            logger.warning(f"Capability not found: {module_code}.{capability_code}")
            return None
        if not capability.is_enabled():
            logger.warning(f"Capability is disabled: {module_code}.{capability_code}")
            return None
        
        return capability
    
    async def get_chat_capability(
        self,
        db: AsyncSession,
        module_code: ModuleCode
    ) -> Optional[AICapabilityEntity]:
        """Get default chat capability for a module"""
        capability = await self.get_capability(db, module_code, "chat")        
        if capability and capability.is_chat_type():
            return capability
        
        # Fallback
        capabilities = await self.ai_capability_port.get_by_type(db, CapabilityType.CHAT)
        for cap in capabilities:
            if cap.module_code == module_code and cap.is_enabled():
                return cap
        
        logger.error(f"No chat capability found for module: {module_code}")
        return None
    
    def build_system_message(self, capability: AICapabilityEntity, context: dict = None) -> str:
        """Build system message from capability prompts and context"""
        system_prompt = capability.system_prompt or ""
        
        if capability.prompt_template and context:
            try:
                template_filled = capability.prompt_template.format(**context)
                system_prompt = f"{system_prompt}\n\n{template_filled}"
            except KeyError as e:
                logger.warning(f"Missing context key in template: {e}")
        
        return system_prompt.strip()
    
    def get_model_config(self, capability: AICapabilityEntity) -> dict:
        """Get LLM model configuration from capability"""
        return {
            "model": capability.default_model,
            "temperature": capability.default_temperature,
            "max_tokens": capability.default_max_tokens,
        }
