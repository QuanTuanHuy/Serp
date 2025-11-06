"""
Author: QuanTuanHuy
Description: Part of Serp Project - Conversation Database Model
"""

from sqlalchemy import Column, BigInteger, String, DateTime, Index
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from src.infrastructure.db.database import Base


class ConversationModel(Base):
    """Conversation database model"""
    
    __tablename__ = "conversations"
    
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, nullable=False, index=True)
    tenant_id = Column(BigInteger, nullable=False, index=True)
    
    module_code = Column(String(50), nullable=False, index=True)
    
    context_type = Column(String(50), nullable=True, index=True)
    context_id = Column(BigInteger, nullable=True, index=True)
    
    title = Column(String(500), nullable=True)
    capability_code = Column(String(100), nullable=True)
    
    status = Column(String(20), default="active", nullable=False, index=True)  # 'active', 'archived'
    
    meta_data = Column(JSONB, default={}, nullable=False)
    
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False, index=True)
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)
    deleted_at = Column(DateTime(timezone=True), nullable=True)
    
    __table_args__ = (
        Index('idx_conv_tenant_user', 'tenant_id', 'user_id', 'created_at'),
        Index('idx_conv_module', 'module_code', 'tenant_id'),
        Index('idx_conv_context', 'context_type', 'context_id'),
    )
    
    def __repr__(self):
        return f"<ConversationModel(id={self.id}, module='{self.module_code}', user_id={self.user_id})>"
