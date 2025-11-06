"""
Author: QuanTuanHuy
Description: Part of Serp Project - Cache Port (Interface)
"""

from abc import ABC, abstractmethod
from typing import Any, Optional


class ICachePort(ABC):
    """
    Interface for caching operations
    
    Supports Redis, Memcached, or in-memory cache implementations
    """
    
    @abstractmethod
    async def get(self, key: str) -> Optional[Any]:
        """
        Get value from cache
        
        Args:
            key: Cache key
        
        Returns:
            Cached value or None if not found
        """
        pass
    
    @abstractmethod
    async def set(
        self, 
        key: str, 
        value: Any, 
        ttl: Optional[int] = None
    ) -> bool:
        """
        Set value in cache with optional TTL
        
        Args:
            key: Cache key
            value: Value to cache (must be serializable)
            ttl: Time to live in seconds. None = no expiration
        
        Returns:
            True if successful
        """
        pass
    
    @abstractmethod
    async def delete(self, key: str) -> bool:
        """
        Delete key from cache
        
        Args:
            key: Cache key
        
        Returns:
            True if key was deleted
        """
        pass
    
    @abstractmethod
    async def exists(self, key: str) -> bool:
        """
        Check if key exists in cache
        
        Args:
            key: Cache key
        
        Returns:
            True if key exists
        """
        pass
    
    @abstractmethod
    async def clear(self, pattern: Optional[str] = None) -> int:
        """
        Clear cache (optionally by pattern)
        
        Args:
            pattern: Key pattern to match (e.g., "user:*"). None = clear all
        
        Returns:
            Number of keys deleted
        """
        pass
    
    @abstractmethod
    async def get_many(self, keys: list[str]) -> dict[str, Any]:
        """
        Get multiple values at once
        
        Args:
            keys: List of cache keys
        
        Returns:
            Dict of key -> value pairs (missing keys omitted)
        """
        pass
    
    @abstractmethod
    async def set_many(
        self,
        items: dict[str, Any],
        ttl: Optional[int] = None
    ) -> bool:
        """
        Set multiple values at once
        
        Args:
            items: Dict of key -> value pairs
            ttl: Time to live in seconds
        
        Returns:
            True if all successful
        """
        pass
