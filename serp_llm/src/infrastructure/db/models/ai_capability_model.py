"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Capability Database Model
"""

from sqlalchemy import Column, BigInteger, String, Boolean, DateTime, Text, Float, Integer, ForeignKey
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from src.infrastructure.db.database import Base


class AICapabilityModel(Base):
    """AI Capability database model"""
    
    __tablename__ = "ai_capabilities"
    
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    module_code = Column(String(50), ForeignKey("ai_modules.code", ondelete="CASCADE"), nullable=False, index=True)
    code = Column(String(100), nullable=False)
    name = Column(String(200), nullable=False)
    description = Column(Text, nullable=True)
    capability_type = Column(String(50), nullable=False, index=True)
    
    system_prompt = Column(Text, nullable=True)
    prompt_template = Column(Text, nullable=True)
    
    available_functions = Column(JSONB, default=[], nullable=False)
    
    default_model = Column(String(50), default="gemini-2.0-flash", nullable=False)
    default_temperature = Column(Float, default=0.7, nullable=False)
    default_max_tokens = Column(Integer, default=4096, nullable=False)
    
    required_permission = Column(String(100), nullable=True)
    
    enabled = Column(Boolean, default=True, nullable=False)
    meta_data = Column(JSONB, default={}, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)
    
    def __repr__(self):
        return f"<AICapabilityModel(id={self.id}, code='{self.code}', type='{self.capability_type}')>"
