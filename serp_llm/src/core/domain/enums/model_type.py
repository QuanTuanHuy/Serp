# Author: QuanTuanHuy
# Description: Part of Serp Project - Model Type Enum

from enum import Enum


class ModelType(str, Enum):
    """LLM Model types supported"""
    
    # OpenAI Models
    GPT_4_TURBO = "gpt-4-turbo"
    GPT_4 = "gpt-4"
    GPT_4O = "gpt-4o"
    GPT_4O_MINI = "gpt-4o-mini"
    GPT_35_TURBO = "gpt-3.5-turbo"
    
    # Google Gemini Models (via OpenAI-compatible API)
    GEMINI_15_PRO = "gemini-1.5-pro"
    GEMINI_15_FLASH = "gemini-1.5-flash"
    GEMINI_10_PRO = "gemini-1.0-pro"
    
    def is_openai(self) -> bool:
        """Check if model is OpenAI"""
        return self.value.startswith("gpt-")
    
    def is_gemini(self) -> bool:
        """Check if model is Gemini"""
        return self.value.startswith("gemini-")
