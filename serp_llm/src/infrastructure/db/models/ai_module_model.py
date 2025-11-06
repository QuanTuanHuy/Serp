"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Module Database Model
"""

from sqlalchemy import Column, BigInteger, String, Boolean, DateTime, Text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from src.infrastructure.db.database import Base


class AIModuleModel(Base):
    """AI Module database model"""
    
    __tablename__ = "ai_modules"
    
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    code = Column(String(50), unique=True, nullable=False, index=True)
    name = Column(String(100), nullable=False)
    description = Column(Text, nullable=True)
    icon = Column(String(50), nullable=True)
    enabled = Column(Boolean, default=True, nullable=False)
    config = Column(JSONB, default={}, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)
    
    def __repr__(self):
        return f"<AIModuleModel(id={self.id}, code='{self.code}', name='{self.name}', enabled={self.enabled})>"
