"""
Author: QuanTuanHuy
Description: Part of Serp Project - Embeddings Job Database Model
"""

from sqlalchemy import Column, BigInteger, String, Integer, DateTime, Text, Index
from sqlalchemy.sql import func
from src.infrastructure.db.database import Base


class EmbeddingsJobModel(Base):
    """Embeddings job database model - tracks background embedding tasks"""
    
    __tablename__ = "embeddings_jobs"
    
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    tenant_id = Column(BigInteger, nullable=False, index=True)
    module_code = Column(String(50), nullable=False, index=True)

    job_type = Column(String(20), nullable=False)
    source_type = Column(String(50), nullable=True)

    status = Column(String(20), default="pending", nullable=False, index=True)

    total_items = Column(Integer, default=0, nullable=False)
    processed_items = Column(Integer, default=0, nullable=False)
    failed_items = Column(Integer, default=0, nullable=False)
    
    error_message = Column(Text, nullable=True)
    
    started_at = Column(DateTime(timezone=True), nullable=True)
    completed_at = Column(DateTime(timezone=True), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False, index=True)
    
    __table_args__ = (
        Index('idx_jobs_module_status', 'module_code', 'status'),
        Index('idx_jobs_tenant', 'tenant_id', 'created_at'),
    )
    
    def __repr__(self):
        return f"<EmbeddingsJobModel(id={self.id}, module='{self.module_code}', type='{self.job_type}', status='{self.status}')>"
