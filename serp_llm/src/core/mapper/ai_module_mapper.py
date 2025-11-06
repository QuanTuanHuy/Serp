"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Module Mapper
"""

from typing import Optional
from datetime import datetime

from src.core.domain.entities import AIModuleEntity
from src.infrastructure.db.models import AIModuleModel


class AIModuleMapper:
    """Mapper between AIModuleEntity and AIModuleModel"""
    
    @staticmethod
    def to_entity(model: Optional[AIModuleModel]) -> Optional[AIModuleEntity]:
        """Convert database model to domain entity"""
        if model is None:
            return None
        
        return AIModuleEntity(
            id=model.id,
            code=model.code,
            name=model.name,
            description=model.description,
            icon=model.icon,
            enabled=model.enabled,
            config=model.config or {},
            created_at=model.created_at,
            updated_at=model.updated_at,
        )
    
    @staticmethod
    def to_model(entity: AIModuleEntity) -> AIModuleModel:
        """Convert domain entity to database model"""
        return AIModuleModel(
            id=entity.id,
            code=entity.code,
            name=entity.name,
            description=entity.description,
            icon=entity.icon,
            enabled=entity.enabled,
            config=entity.config,
            created_at=entity.created_at,
            updated_at=entity.updated_at,
        )
    
    @staticmethod
    def update_model(model: AIModuleModel, entity: AIModuleEntity) -> AIModuleModel:
        """Update model from entity (for updates)"""
        model.code = entity.code
        model.name = entity.name
        model.description = entity.description
        model.icon = entity.icon
        model.enabled = entity.enabled
        model.config = entity.config
        model.updated_at = datetime.utcnow()
        return model
