"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Module Repository Adapter
"""

from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update, delete
from sqlalchemy.exc import IntegrityError
from loguru import logger

from src.core.domain.entities import AIModuleEntity
from src.core.port.store import IAIModulePort
from src.core.mapper import AIModuleMapper
from src.infrastructure.db.models import AIModuleModel


class AIModuleAdapter(IAIModulePort):
    """Repository adapter for AI Module operations"""
    
    async def create(self, db: AsyncSession, entity: AIModuleEntity) -> AIModuleEntity:
        """Create new AI module"""
        try:
            model = AIModuleMapper.to_model(entity)
            db.add(model)
            await db.flush()
            await db.refresh(model)
            
            logger.info(f"Created AI module: {model.code}")
            return AIModuleMapper.to_entity(model)
        except IntegrityError as e:
            await db.rollback()
            logger.error(f"Failed to create AI module: {e}")
            raise ValueError(f"AI module with code '{entity.code}' already exists")
    
    async def get_by_id(self, db: AsyncSession, module_id: int) -> Optional[AIModuleEntity]:
        """Get AI module by ID"""
        stmt = select(AIModuleModel).where(AIModuleModel.id == module_id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        return AIModuleMapper.to_entity(model)
    
    async def get_by_code(self, db: AsyncSession, code: str) -> Optional[AIModuleEntity]:
        """Get AI module by code"""
        stmt = select(AIModuleModel).where(AIModuleModel.code == code)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        return AIModuleMapper.to_entity(model)
    
    async def get_all(self, db: AsyncSession, enabled_only: bool = False) -> List[AIModuleEntity]:
        """Get all AI modules"""
        stmt = select(AIModuleModel)
        if enabled_only:
            stmt = stmt.where(AIModuleModel.enabled == True)
        
        result = await db.execute(stmt)
        models = result.scalars().all()
        return [AIModuleMapper.to_entity(model) for model in models]
    
    async def update(self, db: AsyncSession, entity: AIModuleEntity) -> AIModuleEntity:
        """Update AI module"""
        stmt = select(AIModuleModel).where(AIModuleModel.id == entity.id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        
        if not model:
            raise ValueError(f"AI module with id {entity.id} not found")
        
        updated_model = AIModuleMapper.update_model(model, entity)
        await db.flush()
        await db.refresh(updated_model)
        
        logger.info(f"Updated AI module: {updated_model.code}")
        return AIModuleMapper.to_entity(updated_model)
    
    async def delete(self, db: AsyncSession, module_id: int) -> bool:
        """Delete AI module"""
        stmt = delete(AIModuleModel).where(AIModuleModel.id == module_id)
        result = await db.execute(stmt)
        
        if result.rowcount > 0:
            logger.info(f"Deleted AI module with id: {module_id}")
            return True
        return False
