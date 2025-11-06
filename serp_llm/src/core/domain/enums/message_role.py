# Author: QuanTuanHuy
# Description: Part of Serp Project - Message Role Enum

from enum import Enum


class MessageRole(str, Enum):
    """Message roles in conversation"""
    
    USER = "user"
    ASSISTANT = "assistant"
    SYSTEM = "system"
    FUNCTION = "function"
    TOOL = "tool"
