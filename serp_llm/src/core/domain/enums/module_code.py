"""
Author: QuanTuanHuy
Description: Part of Serp Project - Module Code Enum
"""

from enum import Enum


class ModuleCode(str, Enum):
    """AI Module codes"""
    
    CRM = "crm"
    PTM = "ptm"
    SALES = "sales"
