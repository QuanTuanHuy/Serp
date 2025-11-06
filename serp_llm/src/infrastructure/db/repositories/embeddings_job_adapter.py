"""
Author: QuanTuanHuy
Description: Part of Serp Project - Embeddings Job Repository Adapter
"""

from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete
from loguru import logger

from src.core.domain.entities import EmbeddingsJobEntity
from src.core.domain.enums import ModuleCode, JobStatus
from src.core.port.store import IEmbeddingsJobPort
from src.core.mapper import EmbeddingsJobMapper
from src.infrastructure.db.models import EmbeddingsJobModel


class EmbeddingsJobAdapter(IEmbeddingsJobPort):
    """Repository adapter for Embeddings Job operations"""
    
    async def create(self, db: AsyncSession, entity: EmbeddingsJobEntity) -> EmbeddingsJobEntity:
        """Create new embeddings job"""
        model = EmbeddingsJobMapper.to_model(entity)
        db.add(model)
        await db.flush()
        await db.refresh(model)
        
        logger.info(f"Created embeddings job: {model.id} for module {model.module_code}")
        return EmbeddingsJobMapper.to_entity(model)
    
    async def get_by_id(self, db: AsyncSession, job_id: int) -> Optional[EmbeddingsJobEntity]:
        """Get embeddings job by ID"""
        stmt = select(EmbeddingsJobModel).where(EmbeddingsJobModel.id == job_id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        return EmbeddingsJobMapper.to_entity(model)
    
    async def get_by_status(
        self,
        db: AsyncSession,
        status: JobStatus,
        module_code: Optional[ModuleCode] = None
    ) -> List[EmbeddingsJobEntity]:
        """Get jobs by status, optionally filtered by module"""
        stmt = select(EmbeddingsJobModel).where(
            EmbeddingsJobModel.status == status.value
        )
        
        if module_code:
            stmt = stmt.where(EmbeddingsJobModel.module_code == module_code.value)
        
        stmt = stmt.order_by(EmbeddingsJobModel.created_at.desc())
        
        result = await db.execute(stmt)
        models = result.scalars().all()
        return [EmbeddingsJobMapper.to_entity(model) for model in models]
    
    async def get_by_tenant(
        self,
        db: AsyncSession,
        tenant_id: int,
        limit: int = 50,
        offset: int = 0
    ) -> List[EmbeddingsJobEntity]:
        """Get jobs for a tenant with pagination"""
        stmt = select(EmbeddingsJobModel).where(
            EmbeddingsJobModel.tenant_id == tenant_id
        )
        stmt = stmt.order_by(EmbeddingsJobModel.created_at.desc())
        stmt = stmt.limit(limit).offset(offset)
        
        result = await db.execute(stmt)
        models = result.scalars().all()
        return [EmbeddingsJobMapper.to_entity(model) for model in models]
    
    async def update(self, db: AsyncSession, entity: EmbeddingsJobEntity) -> EmbeddingsJobEntity:
        """Update embeddings job"""
        stmt = select(EmbeddingsJobModel).where(EmbeddingsJobModel.id == entity.id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        
        if not model:
            raise ValueError(f"Embeddings job with id {entity.id} not found")
        
        updated_model = EmbeddingsJobMapper.update_model(model, entity)
        await db.flush()
        await db.refresh(updated_model)
        
        logger.info(f"Updated embeddings job: {updated_model.id} - status: {updated_model.status}")
        return EmbeddingsJobMapper.to_entity(updated_model)
    
    async def delete(self, db: AsyncSession, job_id: int) -> bool:
        """Delete embeddings job"""
        stmt = delete(EmbeddingsJobModel).where(EmbeddingsJobModel.id == job_id)
        result = await db.execute(stmt)
        
        if result.rowcount > 0:
            logger.info(f"Deleted embeddings job: {job_id}")
            return True
        return False
