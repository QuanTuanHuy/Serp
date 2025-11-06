"""
Author: QuanTuanHuy
Description: Part of Serp Project - Context Type Enum
"""

from enum import Enum


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
