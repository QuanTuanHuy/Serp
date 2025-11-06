"""
Author: QuanTuanHuy
Description: Part of Serp Project - Message Service
"""

from typing import List
from datetime import datetime, timezone
from sqlalchemy.ext.asyncio import AsyncSession
from loguru import logger

from src.core.domain.entities import MessageEntity
from src.core.domain.enums import MessageRole, ContentType
from src.core.port.store import IMessagePort


class MessageService:
    """Service for Message business logic"""
    
    def __init__(self, message_port: IMessagePort):
        self.message_port = message_port
    
    async def create_user_message(
        self,
        db: AsyncSession,
        conversation_id: int,
        content: str,
        attachments: List[dict] = None,
    ) -> MessageEntity:
        """Create user message"""
        message = MessageEntity(
            conversation_id=conversation_id,
            role=MessageRole.USER,
            content=content,
            content_type=ContentType.TEXT,
            attachments=attachments or [],
            meta_data={},
            created_at=datetime.now(timezone.utc),
        )
        
        created = await self.message_port.create(db, message)
        logger.debug(f"Created user message {created.id} in conversation {conversation_id}")
        
        return created
    
    async def create_assistant_message(
        self,
        db: AsyncSession,
        conversation_id: int,
        content: str,
        model_used: str,
        tokens_used: int,
        processing_time_ms: int,
        sources: List[dict] = None,
        function_call: dict = None,
    ) -> MessageEntity:
        """Create assistant message"""
        message = MessageEntity(
            conversation_id=conversation_id,
            role=MessageRole.ASSISTANT,
            content=content,
            content_type=ContentType.TEXT,
            model_used=model_used,
            tokens_used=tokens_used,
            processing_time_ms=processing_time_ms,
            sources=sources or [],
            function_call=function_call,
            meta_data={},
            created_at=datetime.now(timezone.utc),
        )
        
        created = await self.message_port.create(db, message)
        logger.debug(f"Created assistant message {created.id} in conversation {conversation_id}")
        
        return created
    
    async def get_conversation_history(
        self,
        db: AsyncSession,
        conversation_id: int,
        max_messages: int = 20,
    ) -> List[MessageEntity]:
        """Get conversation history for LLM context"""
        messages = await self.message_port.get_conversation_history(
            db=db,
            conversation_id=conversation_id,
            max_messages=max_messages,
        )
        
        logger.debug(f"Retrieved {len(messages)} messages for conversation {conversation_id}")
        return messages
    
    def format_messages_for_llm(self, messages: List[MessageEntity]) -> List[dict]:
        """Format messages for LLM API (OpenAI compatible format)"""
        formatted = []
        
        for msg in messages:
            formatted.append(msg.to_llm_message())
        
        return formatted
    
    async def get_conversation_messages(
        self,
        db: AsyncSession,
        conversation_id: int,
        limit: int = 100,
        offset: int = 0,
    ) -> List[MessageEntity]:
        """Get messages with pagination (for UI display)"""
        return await self.message_port.get_by_conversation(
            db=db,
            conversation_id=conversation_id,
            limit=limit,
            offset=offset,
        )
