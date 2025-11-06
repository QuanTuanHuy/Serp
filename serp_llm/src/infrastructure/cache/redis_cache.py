"""
Author: QuanTuanHuy
Description: Part of Serp Project - Redis Cache Implementation
"""

import json
from typing import Any, Optional
from redis import asyncio as aioredis
from loguru import logger

from src.config import settings
from src.core.port.client import ICachePort


class RedisCache(ICachePort):
    """Redis cache implementation"""
    
    def __init__(self):
        """Initialize Redis connection"""
        self.redis: Optional[aioredis.Redis] = None
        self.default_ttl = settings.redis_ttl if hasattr(settings, 'redis_ttl') else 3600
    
    async def connect(self):
        """Connect to Redis"""
        try:
            redis_url = getattr(settings, 'redis_url', None)
            if not redis_url:
                redis_host = getattr(settings, 'redis_host', 'localhost')
                redis_port = getattr(settings, 'redis_port', 6379)
                redis_url = f"redis://{redis_host}:{redis_port}"
            
            self.redis = await aioredis.from_url(
                redis_url,
                encoding="utf-8",
                decode_responses=True,
                max_connections=20,
            )
            await self.redis.ping()
            logger.info(f"Redis connected successfully: {redis_url}")
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
            
            if not isinstance(value, str):
                value = json.dumps(value)
            
            ttl = ttl or self.default_ttl
            await self.redis.setex(key, ttl, value)
            logger.debug(f"Cache SET: {key} (TTL: {ttl}s)")
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
            if result > 0:
                logger.debug(f"Cache DELETE: {key}")
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
                    deleted = await self.redis.delete(*keys)
                    logger.info(f"Cache CLEAR: Deleted {deleted} keys matching '{pattern}'")
                    return deleted
                return 0
            else:
                await self.redis.flushdb()
                logger.warning("Cache CLEAR: Flushed entire database")
                return -1
        except Exception as e:
            logger.error(f"Redis CLEAR error: {e}")
            return 0
    
    async def get_many(self, keys: list[str]) -> dict[str, Any]:
        """Get multiple values at once"""
        try:
            if not self.redis:
                logger.warning("Redis not connected")
                return {}
            
            if not keys:
                return {}
            
            values = await self.redis.mget(keys)
            
            result = {}
            for key, value in zip(keys, values):
                if value is not None:
                    try:
                        result[key] = json.loads(value)
                    except json.JSONDecodeError:
                        result[key] = value
            
            logger.debug(f"Cache GET_MANY: Retrieved {len(result)}/{len(keys)} keys")
            return result
            
        except Exception as e:
            logger.error(f"Redis GET_MANY error: {e}")
            return {}
    
    async def set_many(
        self,
        items: dict[str, Any],
        ttl: Optional[int] = None
    ) -> bool:
        """Set multiple values at once"""
        try:
            if not self.redis:
                logger.warning("Redis not connected")
                return False
            
            if not items:
                return True
            
            ttl = ttl or self.default_ttl
            
            async with self.redis.pipeline(transaction=False) as pipe:
                for key, value in items.items():
                    if not isinstance(value, str):
                        value = json.dumps(value)
                    
                    pipe.setex(key, ttl, value)
                
                await pipe.execute()
            
            logger.debug(f"Cache SET_MANY: Set {len(items)} keys (TTL: {ttl}s)")
            return True
            
        except Exception as e:
            logger.error(f"Redis SET_MANY error: {e}")
            return False
