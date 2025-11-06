"""
Author: QuanTuanHuy
Description: Part of Serp Project - Embeddings Job Entity
"""

from typing import Optional
from datetime import datetime, timezone
from pydantic import Field

from src.core.domain.entities.base_entity import BaseEntity
from src.core.domain.enums import ModuleCode, JobType, JobStatus, SourceType


class EmbeddingsJobEntity(BaseEntity):
    """Embeddings job entity - tracks background embedding generation tasks"""
    
    tenant_id: int = Field(..., description="Tenant ID")
    module_code: ModuleCode = Field(..., description="Module code (crm, ptm, sales)")
    
    job_type: JobType = Field(..., description="Job type (bootstrap, incremental, reindex)")
    source_type: Optional[SourceType] = Field(
        None,
        description="Source type to process (customer, task, etc.)"
    )
    
    status: JobStatus = Field(
        default=JobStatus.PENDING,
        description="Job status"
    )
    
    total_items: int = Field(default=0, ge=0, description="Total items to process")
    processed_items: int = Field(default=0, ge=0, description="Items processed so far")
    failed_items: int = Field(default=0, ge=0, description="Items that failed")
    
    error_message: Optional[str] = Field(None, description="Error message if job failed")
    
    started_at: Optional[datetime] = Field(None, description="When job started")
    completed_at: Optional[datetime] = Field(None, description="When job completed")
    
    def is_pending(self) -> bool:
        """Check if job is pending"""
        return self.status == JobStatus.PENDING
    
    def is_processing(self) -> bool:
        """Check if job is processing"""
        return self.status == JobStatus.PROCESSING
    
    def is_completed(self) -> bool:
        """Check if job is completed"""
        return self.status == JobStatus.COMPLETED
    
    def is_failed(self) -> bool:
        """Check if job failed"""
        return self.status == JobStatus.FAILED
    
    def get_progress_percentage(self) -> float:
        """Calculate progress percentage"""
        if self.total_items == 0:
            return 0.0
        return (self.processed_items / self.total_items) * 100
    
    def start(self):
        """Mark job as started"""
        self.status = JobStatus.PROCESSING
        self.started_at = datetime.now(timezone.utc)
    
    def complete(self):
        """Mark job as completed"""
        self.status = JobStatus.COMPLETED
        self.completed_at = datetime.now(timezone.utc)
    
    def fail(self, error_message: str):
        """Mark job as failed"""
        self.status = JobStatus.FAILED
        self.error_message = error_message
        self.completed_at = datetime.now(timezone.utc)
    
    def increment_processed(self):
        """Increment processed items count"""
        self.processed_items += 1
    
    def increment_failed(self):
        """Increment failed items count"""
        self.failed_items += 1
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "tenant_id": 456,
                "module_code": "crm",
                "job_type": "bootstrap",
                "source_type": "customer",
                "status": "processing",
                "total_items": 1000,
                "processed_items": 250,
                "failed_items": 5,
            }
        }
