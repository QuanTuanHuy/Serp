"""
Author: QuanTuanHuy
Description: Part of Serp Project - Content Type Enum
"""

from enum import Enum


class ContentType(str, Enum):
    """Message content types"""
    
    TEXT = "text"
    IMAGE = "image"
    AUDIO = "audio"
    FILE = "file"
