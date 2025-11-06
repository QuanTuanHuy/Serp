"""
Author: QuanTuanHuy
Description: Part of Serp Project - Document Chunk Mapper
"""

from typing import Optional

from src.core.domain.entities import DocumentChunkEntity
from src.core.domain.enums import ModuleCode, SourceType
from src.infrastructure.db.models import DocumentChunkModel


class DocumentChunkMapper:
    """Mapper between DocumentChunkEntity and DocumentChunkModel"""
    
    @staticmethod
    def to_entity(model: Optional[DocumentChunkModel]) -> Optional[DocumentChunkEntity]:
        """Convert database model to domain entity"""
        if model is None:
            return None
        
        # Convert pgvector to list of floats
        embedding = None
        if model.embedding is not None:
            # pgvector stores as numpy array or list
            embedding = list(model.embedding) if hasattr(model.embedding, '__iter__') else None
        
        return DocumentChunkEntity(
            id=model.id,
            tenant_id=model.tenant_id,
            module_code=ModuleCode(model.module_code),
            source_type=SourceType(model.source_type),
            source_id=model.source_id,
            chunk_index=model.chunk_index,
            content=model.content,
            embedding=embedding,
            token_count=model.token_count,
            meta_data=model.meta_data or {},
            indexed_at=model.indexed_at,
            created_at=model.updated_at,  # Map updated_at to created_at for entity
        )
    
    @staticmethod
    def to_model(entity: DocumentChunkEntity) -> DocumentChunkModel:
        """Convert domain entity to database model"""
        return DocumentChunkModel(
            id=entity.id,
            tenant_id=entity.tenant_id,
            module_code=entity.module_code.value,
            source_type=entity.source_type.value,
            source_id=entity.source_id,
            chunk_index=entity.chunk_index,
            content=entity.content,
            embedding=entity.embedding,  # pgvector will handle the conversion
            token_count=entity.token_count,
            meta_data=entity.meta_data,
            indexed_at=entity.indexed_at,
            updated_at=entity.created_at if entity.created_at else None,
        )
