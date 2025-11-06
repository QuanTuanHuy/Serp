# Author: QuanTuanHuy
# Description: Part of Serp Project - Base Entity

from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field, ConfigDict


class BaseEntity(BaseModel):
    """Base entity with common fields"""
    
    model_config = ConfigDict(
        from_attributes=True,
        use_enum_values=True,
        populate_by_name=True,
    )
    
    id: Optional[int] = Field(default=None, description="Entity ID")
    created_at: Optional[datetime] = Field(default=None, description="Creation timestamp")
    updated_at: Optional[datetime] = Field(default=None, description="Update timestamp")
