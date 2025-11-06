"""
Author: QuanTuanHuy
Description: Part of Serp Project - Conversation Repository Adapter
"""

from typing import Optional, List
from datetime import datetime, timezone
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update
from loguru import logger

from src.core.domain.entities import ConversationEntity
from src.core.domain.enums import ModuleCode, ContextType, ConversationStatus
from src.core.port.store import IConversationPort
from src.core.mapper import ConversationMapper
from src.infrastructure.db.models import ConversationModel


class ConversationAdapter(IConversationPort):
    """Repository adapter for Conversation operations"""
    
    async def create(self, db: AsyncSession, entity: ConversationEntity) -> ConversationEntity:
        """Create new conversation"""
        model = ConversationMapper.to_model(entity)
        db.add(model)
        await db.flush()
        await db.refresh(model)
        
        logger.info(f"Created conversation: {model.id} for user {model.user_id}")
        return ConversationMapper.to_entity(model)
    
    async def get_by_id(self, db: AsyncSession, conversation_id: int) -> Optional[ConversationEntity]:
        """Get conversation by ID"""
        stmt = select(ConversationModel).where(
            ConversationModel.id == conversation_id,
            ConversationModel.deleted_at.is_(None)
        )
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        return ConversationMapper.to_entity(model)
    
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
        """Get conversations for a user with pagination"""
        stmt = select(ConversationModel).where(
            ConversationModel.user_id == user_id,
            ConversationModel.tenant_id == tenant_id,
            ConversationModel.deleted_at.is_(None)
        )
        
        if module_code:
            stmt = stmt.where(ConversationModel.module_code == module_code.value)
        if status:
            stmt = stmt.where(ConversationModel.status == status.value)
        
        stmt = stmt.order_by(ConversationModel.updated_at.desc())
        stmt = stmt.limit(limit).offset(offset)
        
        result = await db.execute(stmt)
        models = result.scalars().all()
        return [ConversationMapper.to_entity(model) for model in models]
    
    async def get_by_context(
        self,
        db: AsyncSession,
        tenant_id: int,
        context_type: ContextType,
        context_id: int
    ) -> List[ConversationEntity]:
        """Get conversations by context (e.g., all conversations about a specific customer)"""
        stmt = select(ConversationModel).where(
            ConversationModel.tenant_id == tenant_id,
            ConversationModel.context_type == context_type.value,
            ConversationModel.context_id == context_id,
            ConversationModel.deleted_at.is_(None)
        )
        stmt = stmt.order_by(ConversationModel.created_at.desc())
        
        result = await db.execute(stmt)
        models = result.scalars().all()
        return [ConversationMapper.to_entity(model) for model in models]
    
    async def update(self, db: AsyncSession, entity: ConversationEntity) -> ConversationEntity:
        """Update conversation"""
        stmt = select(ConversationModel).where(ConversationModel.id == entity.id)
        result = await db.execute(stmt)
        model = result.scalar_one_or_none()
        
        if not model:
            raise ValueError(f"Conversation with id {entity.id} not found")
        
        updated_model = ConversationMapper.update_model(model, entity)
        await db.flush()
        await db.refresh(updated_model)
        
        logger.info(f"Updated conversation: {updated_model.id}")
        return ConversationMapper.to_entity(updated_model)
    
    async def soft_delete(self, db: AsyncSession, conversation_id: int) -> bool:
        """Soft delete conversation"""
        stmt = (
            update(ConversationModel)
            .where(ConversationModel.id == conversation_id)
            .values(deleted_at=datetime.now(timezone.utc))
        )
        result = await db.execute(stmt)
        
        if result.rowcount > 0:
            logger.info(f"Soft deleted conversation: {conversation_id}")
            return True
        return False
    
    async def archive(self, db: AsyncSession, conversation_id: int) -> bool:
        """Archive conversation"""
        stmt = (
            update(ConversationModel)
            .where(ConversationModel.id == conversation_id)
            .values(status=ConversationStatus.ARCHIVED.value)
        )
        result = await db.execute(stmt)
        
        if result.rowcount > 0:
            logger.info(f"Archived conversation: {conversation_id}")
            return True
        return False
