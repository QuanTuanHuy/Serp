"""
Author: QuanTuanHuy
Description: Part of Serp Project - Domain Enums
"""

from enum import Enum


class ModuleCode(str, Enum):
    """AI Module codes"""
    CRM = "crm"
    PTM = "ptm"
    SALES = "sales"


class ContextType(str, Enum):
    """Context types for conversations"""
    # General
    GENERAL = "general"
    
    # CRM contexts
    CUSTOMER = "customer"
    LEAD = "lead"
    OPPORTUNITY = "opportunity"
    
    # PTM contexts
    TASK = "task"
    PROJECT = "project"
    SCHEDULE = "schedule"
    
    # Sales contexts
    DEAL = "deal"
    PIPELINE = "pipeline"


class ConversationStatus(str, Enum):
    """Conversation status"""
    ACTIVE = "active"
    ARCHIVED = "archived"


class CapabilityType(str, Enum):
    """AI Capability types"""
    CHAT = "chat"
    INLINE_ASSIST = "inline_assist"
    AUTO_ACTION = "auto_action"


class MessageRole(str, Enum):
    """Message roles in conversation"""
    USER = "user"
    ASSISTANT = "assistant"
    SYSTEM = "system"
    FUNCTION = "function"


class ContentType(str, Enum):
    """Message content types"""
    TEXT = "text"
    IMAGE = "image"
    AUDIO = "audio"
    FILE = "file"


class JobType(str, Enum):
    """Embedding job types"""
    BOOTSTRAP = "bootstrap"
    INCREMENTAL = "incremental"
    REINDEX = "reindex"


class JobStatus(str, Enum):
    """Embedding job status"""
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"


class SourceType(str, Enum):
    """Document source types for embeddings"""
    # CRM sources
    CUSTOMER = "customer"
    LEAD = "lead"
    OPPORTUNITY = "opportunity"
    EMAIL = "email"
    
    # PTM sources
    TASK = "task"
    PROJECT = "project"
    SCHEDULE = "schedule"
    NOTE = "note"
    
    # Sales sources
    DEAL = "deal"
    PIPELINE = "pipeline"
    FORECAST = "forecast"


# Legacy for backward compatibility (can be removed later)
class ModelType(str, Enum):
    """LLM Model types"""
    GEMINI_2_FLASH = "gemini-2.0-flash"
    GEMINI_2_FLASH_THINKING = "gemini-2.0-flash-thinking-exp"
    GEMINI_15_PRO = "gemini-1.5-pro"
    GEMINI_15_FLASH = "gemini-1.5-flash"
    GPT_4_TURBO = "gpt-4-turbo"
    GPT_4 = "gpt-4"
    GPT_35_TURBO = "gpt-3.5-turbo"
