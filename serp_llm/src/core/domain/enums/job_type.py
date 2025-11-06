"""
Author: QuanTuanHuy
Description: Part of Serp Project - Job Type Enum
"""

from enum import Enum


class JobType(str, Enum):
    """Embedding job types"""
    
    BOOTSTRAP = "bootstrap"
    INCREMENTAL = "incremental"
    REINDEX = "reindex"
