"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Capability Repository Port
"""

from abc import ABC, abstractmethod
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession

from src.core.domain.entities import AICapabilityEntity
from src.core.domain.enums import ModuleCode, CapabilityType


class IAICapabilityPort(ABC):
    """AI Capability repository interface (port)"""
    
    @abstractmethod
    async def create(self, db: AsyncSession, entity: AICapabilityEntity) -> AICapabilityEntity:
        """Create a new AI capability"""
        pass
    
    @abstractmethod
    async def get_by_id(self, db: AsyncSession, capability_id: int) -> Optional[AICapabilityEntity]:
        """Get AI capability by ID"""
        pass
    
    @abstractmethod
    async def get_by_code(
        self, 
        db: AsyncSession, 
        module_code: ModuleCode, 
        code: str
    ) -> Optional[AICapabilityEntity]:
        """Get AI capability by module code and capability code"""
        pass
    
    @abstractmethod
    async def get_by_module(
        self, 
        db: AsyncSession, 
        module_code: ModuleCode,
        enabled_only: bool = False
    ) -> List[AICapabilityEntity]:
        """Get all capabilities for a module"""
        pass
    
    @abstractmethod
    async def get_by_type(
        self,
        db: AsyncSession,
        capability_type: CapabilityType,
        module_code: Optional[ModuleCode] = None
    ) -> List[AICapabilityEntity]:
        """Get capabilities by type, optionally filtered by module"""
        pass
    
    @abstractmethod
    async def update(self, db: AsyncSession, entity: AICapabilityEntity) -> AICapabilityEntity:
        """Update AI capability"""
        pass
    
    @abstractmethod
    async def delete(self, db: AsyncSession, capability_id: int) -> bool:
        """Delete AI capability"""
        pass
