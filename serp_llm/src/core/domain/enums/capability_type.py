"""
Author: QuanTuanHuy
Description: Part of Serp Project - Capability Type Enum
"""

from enum import Enum


class CapabilityType(str, Enum):
    """AI Capability types"""
    
    CHAT = "chat"
    INLINE_ASSIST = "inline_assist"
    AUTO_ACTION = "auto_action"
