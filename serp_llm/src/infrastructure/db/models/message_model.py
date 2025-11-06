# Author: QuanTuanHuy
# Description: Part of Serp Project - Message Database Model

from typing import TYPE_CHECKING, Optional
from sqlalchemy import BigInteger, String, Text, Integer, JSON, ForeignKey, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship

from src.infrastructure.db.base import Base
from src.infrastructure.db.models.base_model import BaseModel

if TYPE_CHECKING:
    from src.infrastructure.db.models.conversation_model import ConversationModel


class MessageModel(Base, BaseModel):
    """Message model for database persistence"""
    
    __tablename__ = "messages"
    
    conversation_id: Mapped[int] = mapped_column(
        BigInteger,
        ForeignKey("conversations.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    
    role: Mapped[str] = mapped_column(String(20), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    tokens_used: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    
    meta_data: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False)
    
    conversation: Mapped["ConversationModel"] = relationship(
        "ConversationModel",
        back_populates="messages"
    )
    
    __table_args__ = (
        Index("idx_conversation_id", "conversation_id"),
    )
    
    def __repr__(self) -> str:
        return f"<MessageModel(id={self.id}, conversation_id={self.conversation_id}, role={self.role})>"
