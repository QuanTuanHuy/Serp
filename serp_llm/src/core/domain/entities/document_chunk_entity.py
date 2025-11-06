"""
Author: QuanTuanHuy
Description: Part of Serp Project - Document Chunk Entity (for RAG)
"""

from typing import Optional, Dict, Any, List
from pydantic import Field

from src.core.domain.entities.base_entity import BaseEntity
from src.core.domain.enums import ModuleCode, SourceType


class DocumentChunkEntity(BaseEntity):
    """Document chunk entity - represents a text chunk with embedding for RAG"""
    
    tenant_id: int = Field(..., description="Tenant ID for multi-tenancy")
    
    module_code: ModuleCode = Field(..., description="Module code (crm, ptm, sales)")
    source_type: SourceType = Field(..., description="Source type (customer, task, etc.)")
    source_id: int = Field(..., description="ID in source service")
    
    chunk_index: int = Field(..., ge=0, description="Order of chunk (0, 1, 2...)")
    content: str = Field(..., description="Actual text content")
    embedding: Optional[List[float]] = Field(
        None,
        description="Vector embedding (768 dimensions for Gemini)"
    )
    token_count: Optional[int] = Field(None, ge=0, description="Token count of content")
    
    meta_data: Dict[str, Any] = Field(
        default_factory=dict,
        description="Additional metadata (title, url, timestamp, etc.)"
    )
    
    indexed_at: Optional[Any] = Field(None, description="When this chunk was indexed")
    
    def has_embedding(self) -> bool:
        """Check if chunk has embedding"""
        return self.embedding is not None and len(self.embedding) > 0
    
    def get_source_key(self) -> str:
        """Get source identifier key (e.g., 'customer:123')"""
        return f"{self.source_type.value}:{self.source_id}"
    
    def get_title(self) -> str:
        """Get title from metadata"""
        return self.meta_data.get("title", f"{self.source_type.value} {self.source_id}")
    
    def get_url(self) -> Optional[str]:
        """Get URL from metadata"""
        return self.meta_data.get("url")
    
    def to_search_result(self, similarity_score: float) -> Dict[str, Any]:
        """Convert to search result format with similarity score"""
        return {
            "id": self.id,
            "source_type": self.source_type.value,
            "source_id": self.source_id,
            "content": self.content,
            "similarity_score": similarity_score,
            "title": self.get_title(),
            "url": self.get_url(),
            "meta_data": self.meta_data,
        }
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": 1,
                "tenant_id": 456,
                "module_code": "crm",
                "source_type": "customer",
                "source_id": 789,
                "chunk_index": 0,
                "content": "ABC Corp is a high-value customer since 2023...",
                "token_count": 50,
                "meta_data": {
                    "title": "ABC Corp - Customer Profile",
                    "url": "/crm/customers/789",
                    "tags": ["enterprise", "high-value"]
                },
            }
        }
