"""
Author: QuanTuanHuy
Description: Part of Serp Project - AI Capability Mapper
"""

from typing import Optional
from datetime import datetime

from src.core.domain.entities import AICapabilityEntity
from src.core.domain.enums import ModuleCode, CapabilityType
from src.infrastructure.db.models import AICapabilityModel


class AICapabilityMapper:
    """Mapper between AICapabilityEntity and AICapabilityModel"""
    
    @staticmethod
    def to_entity(model: Optional[AICapabilityModel]) -> Optional[AICapabilityEntity]:
        """Convert database model to domain entity"""
        if model is None:
            return None
        
        return AICapabilityEntity(
            id=model.id,
            module_code=ModuleCode(model.module_code),
            code=model.code,
            name=model.name,
            description=model.description,
            capability_type=CapabilityType(model.capability_type),
            system_prompt=model.system_prompt,
            prompt_template=model.prompt_template,
            available_functions=model.available_functions or [],
            default_model=model.default_model,
            default_temperature=model.default_temperature,
            default_max_tokens=model.default_max_tokens,
            required_permission=model.required_permission,
            enabled=model.enabled,
            meta_data=model.meta_data or {},
            created_at=model.created_at,
            updated_at=model.updated_at,
        )
    
    @staticmethod
    def to_model(entity: AICapabilityEntity) -> AICapabilityModel:
        """Convert domain entity to database model"""
        return AICapabilityModel(
            id=entity.id,
            module_code=entity.module_code.value,
            code=entity.code,
            name=entity.name,
            description=entity.description,
            capability_type=entity.capability_type.value,
            system_prompt=entity.system_prompt,
            prompt_template=entity.prompt_template,
            available_functions=entity.available_functions,
            default_model=entity.default_model,
            default_temperature=entity.default_temperature,
            default_max_tokens=entity.default_max_tokens,
            required_permission=entity.required_permission,
            enabled=entity.enabled,
            meta_data=entity.meta_data,
            created_at=entity.created_at,
            updated_at=entity.updated_at,
        )
    
    @staticmethod
    def update_model(model: AICapabilityModel, entity: AICapabilityEntity) -> AICapabilityModel:
        """Update model from entity (for updates)"""
        model.module_code = entity.module_code.value
        model.code = entity.code
        model.name = entity.name
        model.description = entity.description
        model.capability_type = entity.capability_type.value
        model.system_prompt = entity.system_prompt
        model.prompt_template = entity.prompt_template
        model.available_functions = entity.available_functions
        model.default_model = entity.default_model
        model.default_temperature = entity.default_temperature
        model.default_max_tokens = entity.default_max_tokens
        model.required_permission = entity.required_permission
        model.enabled = entity.enabled
        model.meta_data = entity.meta_data
        model.updated_at = datetime.utcnow()
        return model
