"""
Author: QuanTuanHuy
Description: Part of Serp Project - Conversation Repository Port
"""

from abc import ABC, abstractmethod
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession

from src.core.domain.entities import ConversationEntity
from src.core.domain.enums import ModuleCode, ContextType, ConversationStatus


class IConversationPort(ABC):
    """Conversation repository interface (port)"""
    
    @abstractmethod
    async def create(self, db: AsyncSession, entity: ConversationEntity) -> ConversationEntity:
        """Create a new conversation"""
        pass
    
    @abstractmethod
    async def get_by_id(self, db: AsyncSession, conversation_id: int) -> Optional[ConversationEntity]:
        """Get conversation by ID"""
        pass
    
    @abstractmethod
    async def get_by_user(
        self,
        db: AsyncSession,
        user_id: int,
        tenant_id: int,
        module_code: Optional[ModuleCode] = None,
        status: Optional[ConversationStatus] = None,
        limit: int = 50,
        offset: int = 0
    ) -> List[ConversationEntity]:
        """Get conversations by user, optionally filtered by module and status"""
        pass
    
    @abstractmethod
    async def get_by_context(
        self,
        db: AsyncSession,
        tenant_id: int,
        context_type: ContextType,
        context_id: int
    ) -> List[ConversationEntity]:
        """Get conversations by context (e.g., all conversations about a specific customer)"""
        pass
    
    @abstractmethod
    async def update(self, db: AsyncSession, entity: ConversationEntity) -> ConversationEntity:
        """Update conversation"""
        pass
    
    @abstractmethod
    async def soft_delete(self, db: AsyncSession, conversation_id: int) -> bool:
        """Soft delete conversation"""
        pass
    
    @abstractmethod
    async def archive(self, db: AsyncSession, conversation_id: int) -> bool:
        """Archive conversation"""
        pass
