"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Module Service
"""

from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from loguru import logger

from src.core.domain.entities import AIModuleEntity
from src.core.domain.enums import ModuleCode
from src.core.port.store import IAIModulePort


class AIModuleService:
    """Service for AI Module business logic"""
    
    def __init__(self, ai_module_port: IAIModulePort):
        self.ai_module_port = ai_module_port
    
    async def get_module_by_code(
        self, 
        db: AsyncSession, 
        module_code: ModuleCode
    ) -> Optional[AIModuleEntity]:
        """Get AI module by code with validation"""
        module = await self.ai_module_port.get_by_code(db, module_code.value)
        
        if not module:
            logger.warning(f"AI module not found: {module_code}")
            return None
        if not module.is_enabled():
            logger.warning(f"AI module is disabled: {module_code}")
            return None
        
        return module
    
    async def get_all_enabled_modules(self, db: AsyncSession) -> List[AIModuleEntity]:
        """Get all enabled AI modules"""
        return await self.ai_module_port.get_all(db, enabled_only=True)
    
    async def validate_module_access(
        self,
        db: AsyncSession,
        module_code: ModuleCode,
        tenant_id: int
    ) -> bool:
        """Validate if tenant has access to this module"""
        module = await self.get_module_by_code(db, module_code)
        
        if not module:
            return False
        
        # TODO: Add tenant subscription/permission check here
        # For now, all enabled modules are accessible
        return True
