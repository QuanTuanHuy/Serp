"""
Author: QuanTuanHuy
Description: Part of Serp Project - Document Chunk Database Model
"""

from sqlalchemy import Column, BigInteger, String, Text, Integer, DateTime, Index, UniqueConstraint
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from src.infrastructure.db.database import Base


class DocumentChunkModel(Base):
    """
    Document chunk database model with embeddings for RAG
    
    Note: Embeddings stored as JSONB array for simplicity.
    Will migrate to dedicated vector database (Pinecone/Qdrant) later.
    """
    
    __tablename__ = "document_chunks"
    
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    tenant_id = Column(BigInteger, nullable=False, index=True)

    module_code = Column(String(50), nullable=False, index=True)
    source_type = Column(String(50), nullable=False, index=True)
    source_id = Column(BigInteger, nullable=False, index=True)

    chunk_index = Column(Integer, nullable=False)
    content = Column(Text, nullable=False)
    embedding = Column(JSONB, nullable=True)  # Stored as JSON array: [0.1, 0.2, ...]
    token_count = Column(Integer, nullable=True)
    
    meta_data = Column(JSONB, default={}, nullable=False)
    
    indexed_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)
    
    __table_args__ = (
        UniqueConstraint('tenant_id', 'module_code', 'source_type', 'source_id', 'chunk_index', 
                        name='uq_chunk_source_index'),
        Index('idx_chunks_tenant_module', 'tenant_id', 'module_code'),
        Index('idx_chunks_source', 'source_type', 'source_id'),
    )
    
    def __repr__(self):
        return f"<DocumentChunkModel(id={self.id}, module='{self.module_code}', source='{self.source_type}:{self.source_id}', chunk={self.chunk_index})>"
