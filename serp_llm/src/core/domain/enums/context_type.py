# Author: QuanTuanHuy
# Description: Part of Serp Project - Context Type Enum

from enum import Enum


class ContextType(str, Enum):
    """Context types for conversations"""
    
    GENERAL = "general"
    CRM = "crm"
    TASK = "task"
    SCHEDULE = "schedule"
    PROJECT = "project"
    CUSTOMER = "customer"
    LEAD = "lead"
