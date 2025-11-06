"""
Author: QuanTuanHuy
Description: Part of Serp Project - Model Type Enum
"""

from enum import Enum


class ModelType(str, Enum):
    """LLM Model types supported"""
    
    # Google Gemini Models (Default - via OpenAI-compatible API)
    GEMINI_2_FLASH = "gemini-2.0-flash"
    GEMINI_2_FLASH_THINKING = "gemini-2.0-flash-thinking-exp"
    GEMINI_2_5_FLASH = "gemini-2.5-flash"
    GEMINI_2_5_FLASH_THINKING = "gemini-2.5-flash-thinking-exp"
    GEMINI_2_5_PRO = "gemini-2.5-pro"
    GEMINI_2_5_PRO_THINKING = "gemini-2.5-pro-thinking-exp"
    
    # OpenAI Models (for compatibility)
    GPT_4_TURBO = "gpt-4-turbo"
    GPT_4 = "gpt-4"
    GPT_4O = "gpt-4o"
    GPT_4O_MINI = "gpt-4o-mini"
    GPT_35_TURBO = "gpt-3.5-turbo"
    
    def is_openai(self) -> bool:
        """Check if model is OpenAI"""
        return self.value.startswith("gpt-")
    
    def is_gemini(self) -> bool:
        """Check if model is Gemini"""
        return self.value.startswith("gemini-")
