"""
Author: QuanTuanHuy
Description: Part of Serp Project - Document Chunk Repository Port
"""

from abc import ABC, abstractmethod
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession

from src.core.domain.entities import DocumentChunkEntity
from src.core.domain.enums import ModuleCode, SourceType


class IDocumentChunkPort(ABC):
    """Document chunk repository interface (port)"""
    
    @abstractmethod
    async def create(self, db: AsyncSession, entity: DocumentChunkEntity) -> DocumentChunkEntity:
        """Create a new document chunk"""
        pass
    
    @abstractmethod
    async def create_batch(
        self,
        db: AsyncSession,
        entities: List[DocumentChunkEntity]
    ) -> List[DocumentChunkEntity]:
        """Create multiple document chunks in batch"""
        pass
    
    @abstractmethod
    async def get_by_id(self, db: AsyncSession, chunk_id: int) -> Optional[DocumentChunkEntity]:
        """Get document chunk by ID"""
        pass
    
    @abstractmethod
    async def get_by_source(
        self,
        db: AsyncSession,
        tenant_id: int,
        module_code: ModuleCode,
        source_type: SourceType,
        source_id: int
    ) -> List[DocumentChunkEntity]:
        """Get all chunks for a specific source"""
        pass
    
    @abstractmethod
    async def search_similar(
        self,
        db: AsyncSession,
        tenant_id: int,
        module_code: ModuleCode,
        query_embedding: List[float],
        limit: int = 5,
        similarity_threshold: float = 0.7
    ) -> List[tuple[DocumentChunkEntity, float]]:
        """
        Search for similar chunks using vector similarity
        Returns list of (chunk, similarity_score) tuples
        """
        pass
    
    @abstractmethod
    async def delete_by_source(
        self,
        db: AsyncSession,
        tenant_id: int,
        module_code: ModuleCode,
        source_type: SourceType,
        source_id: int
    ) -> int:
        """Delete all chunks for a specific source, returns count of deleted chunks"""
        pass
    
    @abstractmethod
    async def update(self, db: AsyncSession, entity: DocumentChunkEntity) -> DocumentChunkEntity:
        """Update document chunk"""
        pass
