"""
Author: QuanTuanHuy
Description: Part of Serp Project - LLM Client Port (Interface)
"""

from abc import ABC, abstractmethod
from typing import List, Dict, Optional, AsyncGenerator


class ILLMClientPort(ABC):
    """
    Interface for LLM client operations
    
    Unified port for LLM providers (Gemini, OpenAI, etc.)
    Supports chat completion, streaming, and embeddings
    """
    
    @abstractmethod
    async def chat_completion(
        self,
        messages: List[Dict[str, str]],
        model: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
        stream: bool = False,
        **kwargs
    ) -> Dict:
        """
        Send chat completion request to LLM
        
        Args:
            messages: List of message dicts with 'role' and 'content'
            model: Model name (e.g., "gemini-2.0-flash"). Uses default if None
            temperature: Sampling temperature (0-2)
            max_tokens: Maximum tokens to generate
            stream: Whether to stream response
            **kwargs: Additional provider-specific parameters
        
        Returns:
            Dict with:
                - content: str (assistant response)
                - tokens_used: int (total tokens)
                - model: str (model used)
                - finish_reason: str (optional)
        """
        pass
    
    @abstractmethod
    async def chat_completion_stream(
        self,
        messages: List[Dict[str, str]],
        model: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
        **kwargs
    ) -> AsyncGenerator[str, None]:
        """
        Stream chat completion response from LLM
        
        Args:
            messages: List of message dicts with 'role' and 'content'
            model: Model name. Uses default if None
            temperature: Sampling temperature
            max_tokens: Maximum tokens to generate
            **kwargs: Additional parameters
        
        Yields:
            Content chunks as they arrive from LLM
        """
        pass
    
    @abstractmethod
    async def create_embedding(
        self,
        text: str,
        model: Optional[str] = None,
    ) -> List[float]:
        """
        Create text embedding
        
        Args:
            text: Text to embed
            model: Embedding model name. Uses default if None
        
        Returns:
            List of embedding values (768 dimensions for Gemini)
        """
        pass
    
    @abstractmethod
    async def create_embeddings_batch(
        self,
        texts: List[str],
        model: Optional[str] = None,
    ) -> List[List[float]]:
        """
        Create embeddings for multiple texts in batch
        
        Args:
            texts: List of texts to embed
            model: Embedding model name. Uses default if None
        
        Returns:
            List of embedding vectors
        """
        pass
    
    @abstractmethod
    async def count_tokens(
        self,
        text: str,
        model: Optional[str] = None
    ) -> int:
        """
        Count tokens in text
        
        Args:
            text: Text to count tokens
            model: Model to use for counting. Uses default if None
        
        Returns:
            Number of tokens
        """
        pass
