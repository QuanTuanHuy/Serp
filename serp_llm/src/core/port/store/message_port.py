"""
Author: QuanTuanHuy
Description: Part of Serp Project - Message Repository Port
"""

from abc import ABC, abstractmethod
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession

from src.core.domain.entities import MessageEntity
from src.core.domain.enums import MessageRole


class IMessagePort(ABC):
    """Message repository interface (port)"""
    
    @abstractmethod
    async def create(self, db: AsyncSession, entity: MessageEntity) -> MessageEntity:
        """Create a new message"""
        pass
    
    @abstractmethod
    async def get_by_id(self, db: AsyncSession, message_id: int) -> Optional[MessageEntity]:
        """Get message by ID"""
        pass
    
    @abstractmethod
    async def get_by_conversation(
        self,
        db: AsyncSession,
        conversation_id: int,
        limit: int = 100,
        offset: int = 0
    ) -> List[MessageEntity]:
        """Get messages by conversation ID, ordered by creation time"""
        pass
    
    @abstractmethod
    async def get_conversation_history(
        self,
        db: AsyncSession,
        conversation_id: int,
        max_messages: int = 20
    ) -> List[MessageEntity]:
        """Get recent conversation history for LLM context"""
        pass
    
    @abstractmethod
    async def update(self, db: AsyncSession, entity: MessageEntity) -> MessageEntity:
        """Update message"""
        pass
    
    @abstractmethod
    async def delete(self, db: AsyncSession, message_id: int) -> bool:
        """Delete message"""
        pass
    
    @abstractmethod
    async def delete_by_conversation(self, db: AsyncSession, conversation_id: int) -> int:
        """Delete all messages in a conversation, returns count of deleted messages"""
        pass
