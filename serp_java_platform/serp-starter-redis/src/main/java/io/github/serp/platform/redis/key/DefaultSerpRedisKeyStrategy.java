/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.key;

import org.springframework.util.Assert;

import io.github.serp.platform.redis.properties.SerpRedisProperties;

public class DefaultSerpRedisKeyStrategy implements SerpRedisKeyStrategy {
    private final SerpRedisProperties properties;

    public DefaultSerpRedisKeyStrategy(SerpRedisProperties properties) {
        this.properties = properties;
    }

    @Override
    public String cacheKey(String namespace, String key) {
        Assert.hasText(key, "Redis cache key is required");

        String resolvedNamespace = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        String prefix = normalize(properties.getCache().getPrefix(), "serp:cache");
        String separator = normalize(properties.getCache().getSeparator(), ":");

        return prefix + separator + resolvedNamespace + separator + key.trim();
    }

    @Override
    public String lockKey(String lockName) {
        Assert.hasText(lockName, "Redis lock name is required");

        String prefix = normalize(properties.getLock().getPrefix(), "serp:lock");
        String separator = normalize(properties.getLock().getSeparator(), ":");

        return prefix + separator + lockName.trim();
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
