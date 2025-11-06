"""
Author: QuanTuanHuy
Description: Part of Serp Project - Conversation Mapper
"""

from typing import Optional
from datetime import datetime

from src.core.domain.entities import ConversationEntity
from src.core.domain.enums import ModuleCode, ContextType, ConversationStatus
from src.infrastructure.db.models import ConversationModel


class ConversationMapper:
    """Mapper between ConversationEntity and ConversationModel"""
    
    @staticmethod
    def to_entity(model: Optional[ConversationModel]) -> Optional[ConversationEntity]:
        """Convert database model to domain entity"""
        if model is None:
            return None
        
        return ConversationEntity(
            id=model.id,
            user_id=model.user_id,
            tenant_id=model.tenant_id,
            module_code=ModuleCode(model.module_code),
            context_type=ContextType(model.context_type) if model.context_type else None,
            context_id=model.context_id,
            title=model.title,
            capability_code=model.capability_code,
            status=ConversationStatus(model.status),
            meta_data=model.meta_data or {},
            created_at=model.created_at,
            updated_at=model.updated_at,
            deleted_at=model.deleted_at,
        )
    
    @staticmethod
    def to_model(entity: ConversationEntity) -> ConversationModel:
        """Convert domain entity to database model"""
        return ConversationModel(
            id=entity.id,
            user_id=entity.user_id,
            tenant_id=entity.tenant_id,
            module_code=entity.module_code.value,
            context_type=entity.context_type.value if entity.context_type else None,
            context_id=entity.context_id,
            title=entity.title,
            capability_code=entity.capability_code,
            status=entity.status.value,
            meta_data=entity.meta_data,
            created_at=entity.created_at,
            updated_at=entity.updated_at,
            deleted_at=entity.deleted_at,
        )
    
    @staticmethod
    def update_model(model: ConversationModel, entity: ConversationEntity) -> ConversationModel:
        """Update model from entity (for updates)"""
        model.title = entity.title
        model.capability_code = entity.capability_code
        model.status = entity.status.value
        model.meta_data = entity.meta_data
        model.updated_at = datetime.utcnow()
        if entity.deleted_at:
            model.deleted_at = entity.deleted_at
        return model
