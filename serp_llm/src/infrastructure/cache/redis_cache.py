# Author: QuanTuanHuy
# Description: Part of Serp Project - Redis Cache Implementation

import json
from typing import Any, Optional
from redis import asyncio as aioredis
from loguru import logger

from src.config import settings
from src.core.ports.cache import CachePort


class RedisCache(CachePort):
    """Redis cache implementation"""
    
    def __init__(self):
        """Initialize Redis connection"""
        self.redis: Optional[aioredis.Redis] = None
        self.default_ttl = settings.redis_ttl
    
    async def connect(self):
        """Connect to Redis"""
        try:
            self.redis = await aioredis.from_url(
                settings.redis_url,
                encoding="utf-8",
                decode_responses=True,
                max_connections=20,
            )
            await self.redis.ping()
            logger.info(f"Redis connected successfully at {settings.redis_host}:{settings.redis_port}")
        except Exception as e:
            logger.error(f"Failed to connect to Redis: {e}")
            raise
    
    async def disconnect(self):
        """Disconnect from Redis"""
        if self.redis:
            await self.redis.close()
            logger.info("Redis disconnected")
    
    async def get(self, key: str) -> Optional[Any]:
        """Get value from cache"""
        try:
            if not self.redis:
                logger.warning("Redis not connected")
                return None
            
            value = await self.redis.get(key)
            if value:
                # Try to deserialize JSON
                try:
                    return json.loads(value)
                except json.JSONDecodeError:
                    return value
            return None
        except Exception as e:
            logger.error(f"Redis GET error for key '{key}': {e}")
            return None
    
    async def set(
        self, 
        key: str, 
        value: Any, 
        ttl: Optional[int] = None
    ) -> bool:
        """Set value in cache with optional TTL"""
        try:
            if not self.redis:
                logger.warning("Redis not connected")
                return False
            
            # Serialize value if it's not a string
            if not isinstance(value, str):
                value = json.dumps(value)
            
            ttl = ttl or self.default_ttl
            await self.redis.setex(key, ttl, value)
            return True
        except Exception as e:
            logger.error(f"Redis SET error for key '{key}': {e}")
            return False
    
    async def delete(self, key: str) -> bool:
        """Delete key from cache"""
        try:
            if not self.redis:
                logger.warning("Redis not connected")
                return False
            
            result = await self.redis.delete(key)
            return result > 0
        except Exception as e:
            logger.error(f"Redis DELETE error for key '{key}': {e}")
            return False
    
    async def exists(self, key: str) -> bool:
        """Check if key exists in cache"""
        try:
            if not self.redis:
                logger.warning("Redis not connected")
                return False
            
            return await self.redis.exists(key) > 0
        except Exception as e:
            logger.error(f"Redis EXISTS error for key '{key}': {e}")
            return False
    
    async def clear(self, pattern: Optional[str] = None) -> int:
        """Clear cache (optionally by pattern)"""
        try:
            if not self.redis:
                logger.warning("Redis not connected")
                return 0
            
            if pattern:
                keys = []
                async for key in self.redis.scan_iter(match=pattern):
                    keys.append(key)
                
                if keys:
                    return await self.redis.delete(*keys)
                return 0
            else:
                await self.redis.flushdb()
                return -1
        except Exception as e:
            logger.error(f"Redis CLEAR error: {e}")
            return 0


# Global Redis cache instance
redis_cache = RedisCache()
