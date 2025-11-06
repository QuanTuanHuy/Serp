"""
Author: QuanTuanHuy
Description: Part of Serp Project - Conversation Status Enum
"""

from enum import Enum


class ConversationStatus(str, Enum):
    """Conversation status"""
    
    ACTIVE = "active"
    ARCHIVED = "archived"
