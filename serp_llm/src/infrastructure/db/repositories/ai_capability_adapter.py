"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Capability Repository Adapter
"""

from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete
from sqlalchemy.exc import IntegrityError
from loguru import logger

from src.core.domain.entities import AICapabilityEntity
from src.core.domain.enums import ModuleCode, CapabilityType
from src.core.port.store import IAICapabilityPort
from src.core.mapper import AICapabilityMapper
from src.infrastructure.db.models import AICapabilityModel


class AICapabilityAdapter(IAICapabilityPort):
    """Repository adapter for AI Capability operations"""
    
    async def create(self, db: AsyncSession, entity: AICapabilityEntity) -> AICapabilityEntity:
        """Create new AI capability"""
        try:
            model = AICapabilityMapper.to_model(entity)
            db.add(model)
            await db.flush()
            await db.refresh(model)
            
            logger.info(f"Created AI capability: {model.module_code}.{model.code}")
            return AICapabilityMapper.to_entity(model)
        except IntegrityError as e:
            await db.rollback()
            logger.error(f"Failed to create AI capability: {e}")
            raise ValueError(f"AI capability '{entity.module_code}.{entity.code}' already exists")
    
    async def get_by_id(self, db: AsyncSession, capability_id: int) -> Optional[AICapabilityEntity]:
        """Get AI capability by ID"""
        stmt = select(AICapabilityModel).where(AICapabilityModel.id == capability_id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        return AICapabilityMapper.to_entity(model)
    
    async def get_by_code(
        self, 
        db: AsyncSession, 
        module_code: ModuleCode, 
        code: str
    ) -> Optional[AICapabilityEntity]:
        """Get AI capability by module code and capability code"""
        stmt = select(AICapabilityModel).where(
            AICapabilityModel.module_code == module_code.value,
            AICapabilityModel.code == code
        )
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        return AICapabilityMapper.to_entity(model)
    
    async def get_by_module(
        self, 
        db: AsyncSession, 
        module_code: ModuleCode,
        enabled_only: bool = False
    ) -> List[AICapabilityEntity]:
        """Get all capabilities for a module"""
        stmt = select(AICapabilityModel).where(
            AICapabilityModel.module_code == module_code.value
        )
        if enabled_only:
            stmt = stmt.where(AICapabilityModel.enabled == True)
        
        result = await db.execute(stmt)
        models = result.scalars().all()
        return [AICapabilityMapper.to_entity(model) for model in models]
    
    async def get_by_type(
        self, 
        db: AsyncSession, 
        capability_type: CapabilityType
    ) -> List[AICapabilityEntity]:
        """Get all capabilities of a specific type"""
        stmt = select(AICapabilityModel).where(
            AICapabilityModel.capability_type == capability_type.value,
            AICapabilityModel.enabled == True
        )
        result = await db.execute(stmt)
        models = result.scalars().all()
        return [AICapabilityMapper.to_entity(model) for model in models]
    
    async def update(self, db: AsyncSession, entity: AICapabilityEntity) -> AICapabilityEntity:
        """Update AI capability"""
        stmt = select(AICapabilityModel).where(AICapabilityModel.id == entity.id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        
        if not model:
            raise ValueError(f"AI capability with id {entity.id} not found")
        
        updated_model = AICapabilityMapper.update_model(model, entity)
        await db.flush()
        await db.refresh(updated_model)
        
        logger.info(f"Updated AI capability: {updated_model.module_code}.{updated_model.code}")
        return AICapabilityMapper.to_entity(updated_model)
    
    async def delete(self, db: AsyncSession, capability_id: int) -> bool:
        """Delete AI capability"""
        stmt = delete(AICapabilityModel).where(AICapabilityModel.id == capability_id)
        result = await db.execute(stmt)
        
        if result.rowcount > 0:
            logger.info(f"Deleted AI capability with id: {capability_id}")
            return True
        return False
