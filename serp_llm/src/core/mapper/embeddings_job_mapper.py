"""
Author: QuanTuanHuy
Description: Part of Serp Project - Embeddings Job Mapper
"""

from typing import Optional
from datetime import datetime

from src.core.domain.entities import EmbeddingsJobEntity
from src.core.domain.enums import ModuleCode, JobType, JobStatus, SourceType
from src.infrastructure.db.models import EmbeddingsJobModel


class EmbeddingsJobMapper:
    """Mapper between EmbeddingsJobEntity and EmbeddingsJobModel"""
    
    @staticmethod
    def to_entity(model: Optional[EmbeddingsJobModel]) -> Optional[EmbeddingsJobEntity]:
        """Convert database model to domain entity"""
        if model is None:
            return None
        
        return EmbeddingsJobEntity(
            id=model.id,
            tenant_id=model.tenant_id,
            module_code=ModuleCode(model.module_code),
            job_type=JobType(model.job_type),
            source_type=SourceType(model.source_type) if model.source_type else None,
            status=JobStatus(model.status),
            total_items=model.total_items,
            processed_items=model.processed_items,
            failed_items=model.failed_items,
            error_message=model.error_message,
            started_at=model.started_at,
            completed_at=model.completed_at,
            created_at=model.created_at,
        )
    
    @staticmethod
    def to_model(entity: EmbeddingsJobEntity) -> EmbeddingsJobModel:
        """Convert domain entity to database model"""
        return EmbeddingsJobModel(
            id=entity.id,
            tenant_id=entity.tenant_id,
            module_code=entity.module_code.value,
            job_type=entity.job_type.value,
            source_type=entity.source_type.value if entity.source_type else None,
            status=entity.status.value,
            total_items=entity.total_items,
            processed_items=entity.processed_items,
            failed_items=entity.failed_items,
            error_message=entity.error_message,
            started_at=entity.started_at,
            completed_at=entity.completed_at,
            created_at=entity.created_at,
        )
    
    @staticmethod
    def update_model(model: EmbeddingsJobModel, entity: EmbeddingsJobEntity) -> EmbeddingsJobModel:
        """Update model from entity (for updates)"""
        model.status = entity.status.value
        model.total_items = entity.total_items
        model.processed_items = entity.processed_items
        model.failed_items = entity.failed_items
        model.error_message = entity.error_message
        model.started_at = entity.started_at
        model.completed_at = entity.completed_at
        return model
