"""
Author: QuanTuanHuy
Description: Part of Serp Project - Source Type Enum
"""

from enum import Enum


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
