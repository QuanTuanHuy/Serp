"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Module Repository Port
"""

from abc import ABC, abstractmethod
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession

from src.core.domain.entities import AIModuleEntity


class IAIModulePort(ABC):
    """AI Module repository interface (port)"""
    
    @abstractmethod
    async def create(self, db: AsyncSession, entity: AIModuleEntity) -> AIModuleEntity:
        """Create a new AI module"""
        pass
    
    @abstractmethod
    async def get_by_id(self, db: AsyncSession, module_id: int) -> Optional[AIModuleEntity]:
        """Get AI module by ID"""
        pass
    
    @abstractmethod
    async def get_by_code(self, db: AsyncSession, code: str) -> Optional[AIModuleEntity]:
        """Get AI module by code (crm, ptm, sales)"""
        pass
    
    @abstractmethod
    async def get_all(self, db: AsyncSession, enabled_only: bool = False) -> List[AIModuleEntity]:
        """Get all AI modules"""
        pass
    
    @abstractmethod
    async def update(self, db: AsyncSession, entity: AIModuleEntity) -> AIModuleEntity:
        """Update AI module"""
        pass
    
    @abstractmethod
    async def delete(self, db: AsyncSession, module_id: int) -> bool:
        """Delete AI module"""
        pass
