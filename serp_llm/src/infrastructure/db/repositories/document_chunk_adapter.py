"""
Author: QuanTuanHuy
Description: Part of Serp Project - Document Chunk Repository Adapter
"""

from typing import Optional, List, Tuple
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete, text
from loguru import logger

from src.core.domain.entities import DocumentChunkEntity
from src.core.domain.enums import ModuleCode, SourceType
from src.core.port.store import IDocumentChunkPort
from src.core.mapper import DocumentChunkMapper
from src.infrastructure.db.models import DocumentChunkModel


class DocumentChunkAdapter(IDocumentChunkPort):
    """Repository adapter for Document Chunk operations with vector search"""
    
    async def create(self, db: AsyncSession, entity: DocumentChunkEntity) -> DocumentChunkEntity:
        """Create new document chunk"""
        model = DocumentChunkMapper.to_model(entity)
        db.add(model)
        await db.flush()
        await db.refresh(model)
        
        logger.info(f"Created document chunk: {model.id} for {model.source_type}/{model.source_id}")
        return DocumentChunkMapper.to_entity(model)
    
    async def create_batch(
        self, 
        db: AsyncSession, 
        entities: List[DocumentChunkEntity]
    ) -> List[DocumentChunkEntity]:
        """Batch create document chunks for performance"""
        models = [DocumentChunkMapper.to_model(entity) for entity in entities]
        db.add_all(models)
        await db.flush()
        
        for model in models:
            await db.refresh(model)
        
        logger.info(f"Created {len(models)} document chunks in batch")
        return [DocumentChunkMapper.to_entity(model) for model in models]
    
    async def get_by_id(self, db: AsyncSession, chunk_id: int) -> Optional[DocumentChunkEntity]:
        """Get document chunk by ID"""
        stmt = select(DocumentChunkModel).where(DocumentChunkModel.id == chunk_id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        return DocumentChunkMapper.to_entity(model)
    
    async def get_by_source(
        self,
        db: AsyncSession,
        tenant_id: int,
        module_code: ModuleCode,
        source_type: SourceType,
        source_id: int
    ) -> List[DocumentChunkEntity]:
        """Get all chunks for a specific source"""
        stmt = select(DocumentChunkModel).where(
            DocumentChunkModel.tenant_id == tenant_id,
            DocumentChunkModel.module_code == module_code.value,
            DocumentChunkModel.source_type == source_type.value,
            DocumentChunkModel.source_id == source_id
        )
        stmt = stmt.order_by(DocumentChunkModel.chunk_index)
        
        result = await db.execute(stmt)
        models = result.scalars().all()
        return [DocumentChunkMapper.to_entity(model) for model in models]
    
    async def search_similar(
        self,
        db: AsyncSession,
        tenant_id: int,
        module_code: ModuleCode,
        query_embedding: List[float],
        limit: int = 10,
        similarity_threshold: float = 0.7
    ) -> List[Tuple[DocumentChunkEntity, float]]:
        """
        Search for similar document chunks using vector similarity
        Returns list of (chunk, similarity_score) tuples
        """
        # Use pgvector's cosine distance operator (<=>)
        # Cosine distance: 0 = identical, 2 = opposite
        # We convert to similarity: 1 - (distance / 2) = [0, 1]
        query = text("""
            SELECT 
                id, tenant_id, module_code, source_type, source_id, 
                chunk_index, content, embedding, token_count, meta_data, 
                indexed_at, updated_at,
                1 - (embedding <=> :query_embedding) / 2 AS similarity
            FROM document_chunks
            WHERE tenant_id = :tenant_id
                AND module_code = :module_code
                AND embedding IS NOT NULL
                AND 1 - (embedding <=> :query_embedding) / 2 >= :threshold
            ORDER BY embedding <=> :query_embedding
            LIMIT :limit
        """)
        
        result = await db.execute(
            query,
            {
                "query_embedding": str(query_embedding),  # pgvector handles conversion
                "tenant_id": tenant_id,
                "module_code": module_code.value,
                "threshold": similarity_threshold,
                "limit": limit
            }
        )
        
        rows = result.fetchall()
        results = []
        
        for row in rows:
            model = DocumentChunkModel(
                id=row.id,
                tenant_id=row.tenant_id,
                module_code=row.module_code,
                source_type=row.source_type,
                source_id=row.source_id,
                chunk_index=row.chunk_index,
                content=row.content,
                embedding=row.embedding,
                token_count=row.token_count,
                meta_data=row.meta_data,
                indexed_at=row.indexed_at,
                updated_at=row.updated_at,
            )
            entity = DocumentChunkMapper.to_entity(model)
            similarity = float(row.similarity)
            results.append((entity, similarity))
        
        logger.debug(f"Found {len(results)} similar chunks for tenant {tenant_id}")
        return results
    
    async def delete_by_source(
        self,
        db: AsyncSession,
        tenant_id: int,
        module_code: ModuleCode,
        source_type: SourceType,
        source_id: int
    ) -> int:
        """Delete all chunks for a specific source"""
        stmt = delete(DocumentChunkModel).where(
            DocumentChunkModel.tenant_id == tenant_id,
            DocumentChunkModel.module_code == module_code.value,
            DocumentChunkModel.source_type == source_type.value,
            DocumentChunkModel.source_id == source_id
        )
        result = await db.execute(stmt)
        
        deleted_count = result.rowcount
        logger.info(f"Deleted {deleted_count} chunks for {source_type}/{source_id}")
        return deleted_count
    
    async def update(self, db: AsyncSession, entity: DocumentChunkEntity) -> DocumentChunkEntity:
        """Update document chunk (mainly for re-indexing)"""
        stmt = select(DocumentChunkModel).where(DocumentChunkModel.id == entity.id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        
        if not model:
            raise ValueError(f"Document chunk with id {entity.id} not found")
        
        model.content = entity.content
        model.embedding = entity.embedding
        model.token_count = entity.token_count
        model.meta_data = entity.meta_data
        model.indexed_at = entity.indexed_at
        
        await db.flush()
        await db.refresh(model)
        
        logger.info(f"Updated document chunk: {model.id}")
        return DocumentChunkMapper.to_entity(model)
