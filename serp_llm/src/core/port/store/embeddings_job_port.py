"""
Author: QuanTuanHuy
Description: Part of Serp Project - Embeddings Job Repository Port
"""

from abc import ABC, abstractmethod
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession

from src.core.domain.entities import EmbeddingsJobEntity
from src.core.domain.enums import ModuleCode, JobType, JobStatus


class IEmbeddingsJobPort(ABC):
    """Embeddings job repository interface (port)"""
    
    @abstractmethod
    async def create(self, db: AsyncSession, entity: EmbeddingsJobEntity) -> EmbeddingsJobEntity:
        """Create a new embeddings job"""
        pass
    
    @abstractmethod
    async def get_by_id(self, db: AsyncSession, job_id: int) -> Optional[EmbeddingsJobEntity]:
        """Get embeddings job by ID"""
        pass
    
    @abstractmethod
    async def get_by_status(
        self,
        db: AsyncSession,
        status: JobStatus,
        module_code: Optional[ModuleCode] = None,
        limit: int = 50
    ) -> List[EmbeddingsJobEntity]:
        """Get jobs by status, optionally filtered by module"""
        pass
    
    @abstractmethod
    async def get_by_tenant(
        self,
        db: AsyncSession,
        tenant_id: int,
        module_code: Optional[ModuleCode] = None,
        limit: int = 50
    ) -> List[EmbeddingsJobEntity]:
        """Get jobs by tenant, optionally filtered by module"""
        pass
    
    @abstractmethod
    async def update(self, db: AsyncSession, entity: EmbeddingsJobEntity) -> EmbeddingsJobEntity:
        """Update embeddings job"""
        pass
    
    @abstractmethod
    async def delete(self, db: AsyncSession, job_id: int) -> bool:
        """Delete embeddings job"""
        pass
