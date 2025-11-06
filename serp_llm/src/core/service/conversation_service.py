"""
Author: QuanTuanHuy
Description: Part of Serp Project - Conversation Service
"""

from typing import Optional, List
from datetime import datetime, timezone
from sqlalchemy.ext.asyncio import AsyncSession
from loguru import logger

from src.core.domain.entities import ConversationEntity
from src.core.domain.enums import ModuleCode, ContextType, ConversationStatus
from src.core.port.store import IConversationPort


class ConversationService:
    """Service for Conversation business logic"""
    
    def __init__(self, conversation_port: IConversationPort):
        self.conversation_port = conversation_port
    
    async def create_conversation(
        self,
        db: AsyncSession,
        user_id: int,
        tenant_id: int,
        module_code: ModuleCode,
        capability_code: str,
        title: Optional[str] = None,
        context_type: Optional[ContextType] = None,
        context_id: Optional[int] = None,
    ) -> ConversationEntity:
        """Create new conversation with validation"""
        
        if not title:
            context_str = f" about {context_type.value}" if context_type else ""
            title = f"{module_code.value.upper()} Chat{context_str}"
        
        conversation = ConversationEntity(
            user_id=user_id,
            tenant_id=tenant_id,
            module_code=module_code,
            context_type=context_type,
            context_id=context_id,
            title=title,
            capability_code=capability_code,
            status=ConversationStatus.ACTIVE,
            meta_data={},
            created_at=datetime.now(timezone.utc),
            updated_at=datetime.now(timezone.utc),
        )
        
        created = await self.conversation_port.create(db, conversation)
        logger.info(f"Created conversation {created.id} for user {user_id}")
        
        return created
    
    async def get_or_create_conversation(
        self,
        db: AsyncSession,
        user_id: int,
        tenant_id: int,
        module_code: ModuleCode,
        capability_code: str,
        conversation_id: Optional[int] = None,
        context_type: Optional[ContextType] = None,
        context_id: Optional[int] = None,
    ) -> ConversationEntity:
        """Get existing conversation or create new one"""
        
        if conversation_id:
            conversation = await self.conversation_port.get_by_id(db, conversation_id)
            if conversation and conversation.user_id == user_id:
                return conversation
            
            logger.warning(f"Conversation {conversation_id} not found or access denied")
        
        return await self.create_conversation(
            db=db,
            user_id=user_id,
            tenant_id=tenant_id,
            module_code=module_code,
            capability_code=capability_code,
            context_type=context_type,
            context_id=context_id,
        )
    
    async def get_user_conversations(
        self,
        db: AsyncSession,
        user_id: int,
        tenant_id: int,
        module_code: Optional[ModuleCode] = None,
        limit: int = 50,
        offset: int = 0,
    ) -> List[ConversationEntity]:
        """Get user's conversations with pagination"""
        return await self.conversation_port.get_by_user(
            db=db,
            user_id=user_id,
            tenant_id=tenant_id,
            module_code=module_code,
            status=ConversationStatus.ACTIVE,
            limit=limit,
            offset=offset,
        )
    
    async def update_conversation_title(
        self,
        db: AsyncSession,
        conversation_id: int,
        title: str,
    ) -> ConversationEntity:
        """Update conversation title"""
        conversation = await self.conversation_port.get_by_id(db, conversation_id)
        
        if not conversation:
            raise ValueError(f"Conversation {conversation_id} not found")
        
        conversation.title = title
        conversation.updated_at = datetime.now(timezone.utc)
        
        return await self.conversation_port.update(db, conversation)
    
    async def archive_conversation(
        self,
        db: AsyncSession,
        conversation_id: int,
    ) -> bool:
        """Archive conversation"""
        return await self.conversation_port.archive(db, conversation_id)
