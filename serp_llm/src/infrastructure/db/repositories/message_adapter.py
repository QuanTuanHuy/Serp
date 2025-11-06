"""
Author: QuanTuanHuy
Description: Part of Serp Project - Message Repository Adapter
"""

from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete
from loguru import logger

from src.core.domain.entities import MessageEntity
from src.core.port.store import IMessagePort
from src.core.mapper import MessageMapper
from src.infrastructure.db.models import MessageModel


class MessageAdapter(IMessagePort):
    """Repository adapter for Message operations"""
    
    async def create(self, db: AsyncSession, entity: MessageEntity) -> MessageEntity:
        """Create new message"""
        model = MessageMapper.to_model(entity)
        db.add(model)
        await db.flush()
        await db.refresh(model)
        
        logger.info(f"Created message: {model.id} in conversation {model.conversation_id}")
        return MessageMapper.to_entity(model)
    
    async def get_by_id(self, db: AsyncSession, message_id: int) -> Optional[MessageEntity]:
        """Get message by ID"""
        stmt = select(MessageModel).where(MessageModel.id == message_id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        return MessageMapper.to_entity(model)
    
    async def get_by_conversation(
        self,
        db: AsyncSession,
        conversation_id: int,
        limit: int = 100,
        offset: int = 0
    ) -> List[MessageEntity]:
        """Get messages in a conversation with pagination"""
        stmt = select(MessageModel).where(
            MessageModel.conversation_id == conversation_id
        )
        stmt = stmt.order_by(MessageModel.created_at.asc())
        stmt = stmt.limit(limit).offset(offset)
        
        result = await db.execute(stmt)
        models = result.scalars().all()
        return [MessageMapper.to_entity(model) for model in models]
    
    async def get_conversation_history(
        self,
        db: AsyncSession,
        conversation_id: int,
        max_messages: Optional[int] = None
    ) -> List[MessageEntity]:
        """Get conversation history for LLM context (most recent messages)"""
        stmt = select(MessageModel).where(
            MessageModel.conversation_id == conversation_id
        )
        stmt = stmt.order_by(MessageModel.created_at.desc())
        
        if max_messages:
            stmt = stmt.limit(max_messages)
        
        result = await db.execute(stmt)
        models = result.scalars().all()
        
        messages = [MessageMapper.to_entity(model) for model in reversed(models)]
        
        logger.debug(f"Retrieved {len(messages)} messages for conversation {conversation_id}")
        return messages
    
    async def update(self, db: AsyncSession, entity: MessageEntity) -> MessageEntity:
        """Update message (rarely used - messages are typically immutable)"""
        stmt = select(MessageModel).where(MessageModel.id == entity.id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        
        if not model:
            raise ValueError(f"Message with id {entity.id} not found")
        
        model.meta_data = entity.meta_data
        await db.flush()
        await db.refresh(model)
        
        logger.info(f"Updated message: {model.id}")
        return MessageMapper.to_entity(model)
    
    async def delete(self, db: AsyncSession, message_id: int) -> bool:
        """Delete message"""
        stmt = delete(MessageModel).where(MessageModel.id == message_id)
        result = await db.execute(stmt)
        
        if result.rowcount > 0:
            logger.info(f"Deleted message: {message_id}")
            return True
        return False
    
    async def delete_by_conversation(self, db: AsyncSession, conversation_id: int) -> int:
        """Delete all messages in a conversation"""
        stmt = delete(MessageModel).where(MessageModel.conversation_id == conversation_id)
        result = await db.execute(stmt)
        
        deleted_count = result.rowcount
        logger.info(f"Deleted {deleted_count} messages from conversation {conversation_id}")
        return deleted_count
