# Author: QuanTuanHuy
# Description: Part of Serp Project - LLM Client (Gemini via OpenAI-compatible API)

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
from src.core.ports.llm import LLMProviderPort


class OpenAIClient(LLMProviderPort):
    """
    LLM client using OpenAI SDK
    
    Supports Google Gemini via OpenAI-compatible API:
    - Base URL: https://generativelanguage.googleapis.com/v1beta/openai/
    - Models: gemini-2.0-flash, gemini-2.5-pro, gemini-2.5-flash
    - Embeddings: text-embedding-004
    
    Also works with OpenAI by changing base_url and api_key.
    """
    
    def __init__(self):
        """Initialize LLM client with Gemini (or OpenAI) configuration"""
        self.client = AsyncOpenAI(
            api_key=settings.openai_api_key,
            organization=settings.openai_org_id or None,
            base_url=settings.openai_base_url,
        )
        self.default_model = settings.default_model
        self.default_temperature = settings.default_temperature
        self.default_max_tokens = settings.default_max_tokens
        self.embedding_model = settings.embedding_model
        
        provider = "Gemini" if "generativelanguage.googleapis.com" in settings.openai_base_url else "OpenAI"
        logger.info(
            f"LLM client initialized - Provider: {provider}, "
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
        temperature: Optional[float] = None,
        max_tokens: Optional[int] = None,
        stream: bool = False,
        **kwargs
    ) -> dict | AsyncIterator[dict]:
        """Generate chat completion using OpenAI API"""
        try:
            model = model or self.default_model
            temperature = temperature if temperature is not None else self.default_temperature
            max_tokens = max_tokens or self.default_max_tokens
            
            logger.debug(
                f"Chat completion request: model={model}, stream={stream}, "
                f"messages_count={len(messages)}"
            )
            
            response = await self.client.chat.completions.create(
                model=model,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
                stream=stream,
                **kwargs
            )
            
            if stream:
                return self._process_stream(response)
            else:
                result = {
                    "content": response.choices[0].message.content,
                    "role": response.choices[0].message.role,
                    "model": response.model,
                    "tokens": {
                        "prompt": response.usage.prompt_tokens,
                        "completion": response.usage.completion_tokens,
                        "total": response.usage.total_tokens,
                    },
                    "finish_reason": response.choices[0].finish_reason,
                }
                
                logger.debug(
                    f"Chat completion success: tokens={result['tokens']['total']}"
                )
                return result
                
        except Exception as e:
            logger.error(f"Chat completion error: {e}")
            raise
    
    async def _process_stream(
        self, 
        stream: AsyncIterator
    ) -> AsyncIterator[dict]:
        """Process streaming response"""
        try:
            async for chunk in stream:
                if chunk.choices and chunk.choices[0].delta.content:
                    yield {
                        "content": chunk.choices[0].delta.content,
                        "role": "assistant",
                        "finish_reason": chunk.choices[0].finish_reason,
                    }
        except Exception as e:
            logger.error(f"Stream processing error: {e}")
            raise
    
    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        retry=retry_if_exception_type(Exception),
        reraise=True,
    )
    async def generate_embedding(
        self,
        text: str | list[str],
        model: Optional[str] = None,
        **kwargs
    ) -> list[float] | list[list[float]]:
        """Generate embeddings using OpenAI API"""
        try:
            model = model or self.embedding_model
            
            is_single = isinstance(text, str)
            texts = [text] if is_single else text
            
            logger.debug(f"Embedding request: model={model}, texts_count={len(texts)}")
            
            response = await self.client.embeddings.create(
                model=model,
                input=texts,
                **kwargs
            )
            
            embeddings = [item.embedding for item in response.data]
            
            logger.debug(
                f"Embedding success: dimension={len(embeddings[0])}, "
                f"tokens={response.usage.total_tokens}"
            )
            
            return embeddings[0] if is_single else embeddings
            
        except Exception as e:
            logger.error(f"Embedding generation error: {e}")
            raise
    
    async def count_tokens(self, text: str, model: Optional[str] = None) -> int:
        """
        Count tokens in text
        Note: This is an approximation. For exact count, use tiktoken library.
        """
        try:
            # Simple approximation: 1 token ≈ 4 characters for English
            # This is rough but avoids adding tiktoken dependency
            approx_tokens = len(text) // 4
            
            logger.debug(f"Token count (approx): {approx_tokens}")
            return approx_tokens
            
        except Exception as e:
            logger.error(f"Token counting error: {e}")
            return 0
