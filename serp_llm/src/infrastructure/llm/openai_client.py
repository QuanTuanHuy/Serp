"""
Author: QuanTuanHuy
Description: Part of Serp Project - OpenAI Client
"""

from typing import AsyncIterator, Optional
from openai import AsyncOpenAI
from loguru import logger
from tenacity import (
    retry,
    stop_after_attempt,
    wait_exponential,
    retry_if_exception_type,
)

from src.config import settings
from src.core.port.client import ILLMClientPort


class OpenAIClient(ILLMClientPort):
    """
    OpenAI client using OpenAI SDK

    Supports Google Gemini via OpenAI-compatible API:
    - Base URL: https://generativelanguage.googleapis.com/v1beta/openai/
    - Models: gemini-2.0-flash, gemini-2.5-pro, gemini-2.5-flash
    - Embeddings: text-embedding-004
    """
    
    def __init__(self):
        """Initialize Gemini client via OpenAI SDK"""
        self.client = AsyncOpenAI(
            api_key=settings.openai_api_key,
            base_url=settings.openai_base_url,
        )
        self.default_model = settings.default_model
        self.default_temperature = 0.7
        self.default_max_tokens = 2000
        self.embedding_model = settings.embedding_model
        
        logger.info(
            f"Gemini LLM client initialized - "
            f"Model: {self.default_model}, Base URL: {settings.openai_base_url}"
        )
    
    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        retry=retry_if_exception_type(Exception),
        reraise=True,
    )
    async def chat_completion(
        self,
        messages: list[dict],
        model: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
        stream: bool = False,
        **kwargs
    ) -> dict:
        """Generate chat completion using Gemini API"""
        try:
            model_name = model or self.default_model
            
            logger.debug(
                f"Chat completion request: model={model_name}, "
                f"messages_count={len(messages)}, stream={stream}"
            )
            
            response = await self.client.chat.completions.create(
                model=model_name,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
                stream=False,
                **kwargs
            )
            
            result = {
                "content": response.choices[0].message.content,
                "tokens_used": response.usage.total_tokens,
                "model": response.model,
                "finish_reason": response.choices[0].finish_reason,
                "prompt_tokens": response.usage.prompt_tokens,
                "completion_tokens": response.usage.completion_tokens,
            }
            
            logger.debug(
                f"Chat completion success: tokens={result['tokens_used']}, "
                f"content_length={len(result['content'])}"
            )
            
            return result
            
        except Exception as e:
            logger.error(f"Chat completion error: {e}")
            raise
    
    async def chat_completion_stream(
        self,
        messages: list[dict],
        model: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
        **kwargs
    ) -> AsyncIterator[str]:
        """Stream chat completion response"""
        try:
            model_name = model or self.default_model
            
            logger.debug(
                f"Chat completion stream request: model={model_name}, "
                f"messages_count={len(messages)}"
            )
            
            stream = await self.client.chat.completions.create(
                model=model_name,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
                stream=True,
                **kwargs
            )
            
            async for chunk in stream:
                if chunk.choices and chunk.choices[0].delta.content:
                    yield chunk.choices[0].delta.content
                    
        except Exception as e:
            logger.error(f"Chat streaming error: {e}")
            raise
    
    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        retry=retry_if_exception_type(Exception),
        reraise=True,
    )
    async def create_embedding(
        self,
        text: str,
        model: Optional[str] = None,
    ) -> list[float]:
        """Create text embedding"""
        try:
            model_name = model or self.embedding_model
            
            logger.debug(f"Embedding request: model={model_name}")
            
            response = await self.client.embeddings.create(
                model=model_name,
                input=text,
            )
            
            embedding = response.data[0].embedding
            
            logger.debug(
                f"Embedding success: dimension={len(embedding)}, "
                f"tokens={response.usage.total_tokens}"
            )
            
            return embedding
            
        except Exception as e:
            logger.error(f"Embedding generation error: {e}")
            raise
    
    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        retry=retry_if_exception_type(Exception),
        reraise=True,
    )
    async def create_embeddings_batch(
        self,
        texts: list[str],
        model: Optional[str] = None,
    ) -> list[list[float]]:
        """Create embeddings for multiple texts"""
        try:
            model_name = model or self.embedding_model
            
            logger.debug(f"Batch embedding request: model={model_name}, count={len(texts)}")
            
            response = await self.client.embeddings.create(
                model=model_name,
                input=texts,
            )
            
            embeddings = [item.embedding for item in response.data]
            
            logger.debug(
                f"Batch embedding success: count={len(embeddings)}, "
                f"dimension={len(embeddings[0]) if embeddings else 0}, "
                f"tokens={response.usage.total_tokens}"
            )
            
            return embeddings
            
        except Exception as e:
            logger.error(f"Batch embedding generation error: {e}")
            raise
    
    async def count_tokens(
        self,
        text: str,
        model: Optional[str] = None
    ) -> int:
        """
        Count tokens in text
        
        Note: This is an approximation using character count / 4.
        For exact count, consider using tiktoken library in production.
        """
        # Rough estimation: ~4 characters per token for English
        estimated_tokens = len(text) // 4
        
        logger.debug(f"Token count (approx): {estimated_tokens} for text length {len(text)}")
        
        return estimated_tokens
    
    async def close(self):
        """Close HTTP client"""
        await self.client.close()
        logger.info("Closed Gemini LLM client")
