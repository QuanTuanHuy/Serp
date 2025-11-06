# Author: QuanTuanHuy
# Description: Part of Serp Project - LLM Provider Port

from abc import ABC, abstractmethod
from typing import AsyncIterator, Optional


class LLMMessage(dict):
    """LLM Message structure"""
    role: str
    content: str


class LLMProviderPort(ABC):
    """Port interface for LLM providers"""
    
    @abstractmethod
    async def chat_completion(
        self,
        messages: list[dict],
        model: Optional[str] = None,
        temperature: Optional[float] = None,
        max_tokens: Optional[int] = None,
        stream: bool = False,
        **kwargs
    ) -> dict | AsyncIterator[dict]:
        """
        Generate chat completion
        
        Args:
            messages: List of messages in conversation
            model: Model to use (e.g., gpt-4-turbo, gemini-1.5-pro)
            temperature: Sampling temperature (0-2)
            max_tokens: Maximum tokens to generate
            stream: Whether to stream response
            **kwargs: Additional provider-specific parameters
            
        Returns:
            Response dict or async iterator for streaming
        """
        pass
    
    @abstractmethod
    async def generate_embedding(
        self,
        text: str | list[str],
        model: Optional[str] = None,
        **kwargs
    ) -> list[float] | list[list[float]]:
        """
        Generate embeddings for text
        
        Args:
            text: Text or list of texts to embed
            model: Embedding model to use
            **kwargs: Additional parameters
            
        Returns:
            Embedding vector(s)
        """
        pass
    
    @abstractmethod
    async def count_tokens(self, text: str, model: Optional[str] = None) -> int:
        """
        Count tokens in text
        
        Args:
            text: Text to count tokens
            model: Model to use for counting
            
        Returns:
            Number of tokens
        """
        pass
