"""
Author: QuanTuanHuy
Description: Part of Serp Project - Message Mapper
"""

from typing import Optional

from src.core.domain.entities import MessageEntity
from src.core.domain.enums import MessageRole, ContentType
from src.infrastructure.db.models import MessageModel


class MessageMapper:
    """Mapper between MessageEntity and MessageModel"""
    
    @staticmethod
    def to_entity(model: Optional[MessageModel]) -> Optional[MessageEntity]:
        """Convert database model to domain entity"""
        if model is None:
            return None
        
        return MessageEntity(
            id=model.id,
            conversation_id=model.conversation_id,
            role=MessageRole(model.role),
            content=model.content,
            content_type=ContentType(model.content_type),
            attachments=model.attachments or [],
            function_call=model.function_call,
            tokens_used=model.tokens_used,
            model_used=model.model_used,
            processing_time_ms=model.processing_time_ms,
            sources=model.sources or [],
            meta_data=model.meta_data or {},
            created_at=model.created_at,
        )
    
    @staticmethod
    def to_model(entity: MessageEntity) -> MessageModel:
        """Convert domain entity to database model"""
        return MessageModel(
            id=entity.id,
            conversation_id=entity.conversation_id,
            role=entity.role.value,
            content=entity.content,
            content_type=entity.content_type.value,
            attachments=entity.attachments,
            function_call=entity.function_call,
            tokens_used=entity.tokens_used,
            model_used=entity.model_used,
            processing_time_ms=entity.processing_time_ms,
            sources=entity.sources,
            meta_data=entity.meta_data,
            created_at=entity.created_at,
        )
