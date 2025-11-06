"""
Author: QuanTuanHuy
Description: Part of Serp Project - Job Status Enum
"""

from enum import Enum


class JobStatus(str, Enum):
    """Embedding job status"""
    
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"
