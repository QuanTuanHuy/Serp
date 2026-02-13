/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.cache;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;

import io.github.serp.platform.redis.key.SerpRedisKeyStrategy;
import io.github.serp.platform.redis.properties.SerpRedisProperties;

public class RedisSerpCacheService implements SerpCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final SerpRedisKeyStrategy keyStrategy;
    private final SerpRedisProperties properties;

    public RedisSerpCacheService(
            RedisTemplate<String, Object> redisTemplate,
            SerpRedisKeyStrategy keyStrategy,
            SerpRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.keyStrategy = keyStrategy;
        this.properties = properties;
    }

    @Override
    public void put(String namespace, String key, Object value) {
        put(namespace, key, value, null);
    }

    @Override
    public void put(String namespace, String key, Object value, Duration ttl) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        if (ttl == null) {
            redisTemplate.opsForValue().set(cacheKey, value, resolveDefaultCacheTtl());
            return;
        }
        redisTemplate.opsForValue().set(cacheKey, value, ttl);
    }

    @Override
    public Optional<Object> get(String namespace, String key) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        return Optional.ofNullable(redisTemplate.opsForValue().get(cacheKey));
    }

    @Override
    public <T> Optional<T> get(String namespace, String key, Class<T> type) {
        return get(namespace, key)
                .filter(type::isInstance)
                .map(type::cast);
    }

    @Override
    public void evict(String namespace, String key) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.delete(cacheKey);
    }

    private Duration resolveDefaultCacheTtl() {
        long ttlSeconds = Math.max(1, properties.getCache().getDefaultTtlSeconds());
        return Duration.ofSeconds(ttlSeconds);
    }
}
