# Author: QuanTuanHuy
# Description: Part of Serp Project - Conversation Database Model

from typing import TYPE_CHECKING, Optional
from sqlalchemy import BigInteger, String, JSON, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship

from src.infrastructure.db.base import Base
from src.infrastructure.db.models.base_model import BaseModel

if TYPE_CHECKING:
    from src.infrastructure.db.models.message_model import MessageModel


class ConversationModel(Base, BaseModel):
    """Conversation model for database persistence"""
    
    __tablename__ = "conversations"
    
    user_id: Mapped[int] = mapped_column(BigInteger, nullable=False, index=True)
    tenant_id: Mapped[int] = mapped_column(BigInteger, nullable=False, index=True)
    
    title: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    context_type: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    context_id: Mapped[Optional[int]] = mapped_column(BigInteger, nullable=True)
    model_type: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    
    meta_data: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False)
    
    messages: Mapped[list["MessageModel"]] = relationship(
        "MessageModel",
        back_populates="conversation",
        cascade="all, delete-orphan",
        lazy="selectin"
    )
    
    __table_args__ = (
        Index("idx_user_tenant", "user_id", "tenant_id"),
        Index("idx_context", "context_type", "context_id"),
    )
    
    def __repr__(self) -> str:
        return f"<ConversationModel(id={self.id}, user_id={self.user_id}, title={self.title})>"
