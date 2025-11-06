"""
Author: QuanTuanHuy
Description: Part of Serp Project - Message Database Model
"""

from sqlalchemy import Column, BigInteger, String, Text, Integer, DateTime, ForeignKey, Index
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from src.infrastructure.db.database import Base


class MessageModel(Base):
    """Message database model"""
    
    __tablename__ = "messages"
    
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    conversation_id = Column(BigInteger, ForeignKey("conversations.id", ondelete="CASCADE"), nullable=False, index=True)
    
    role = Column(String(20), nullable=False, index=True)
    content = Column(Text, nullable=False)
    content_type = Column(String(20), default="text", nullable=False)
    
    attachments = Column(JSONB, default=[], nullable=False)
    
    function_call = Column(JSONB, nullable=True)
    
    tokens_used = Column(Integer, default=0, nullable=False)
    model_used = Column(String(50), nullable=True)
    processing_time_ms = Column(Integer, nullable=True)
    
    sources = Column(JSONB, default=[], nullable=False)
    
    meta_data = Column(JSONB, default={}, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False, index=True)
    
    __table_args__ = (
        Index('idx_msg_conversation', 'conversation_id', 'created_at'),
    )
    
    def __repr__(self):
        return f"<MessageModel(id={self.id}, conversation_id={self.conversation_id}, role='{self.role}')>"
