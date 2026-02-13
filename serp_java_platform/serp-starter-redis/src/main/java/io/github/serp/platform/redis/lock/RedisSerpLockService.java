/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.serp.platform.redis.key.SerpRedisKeyStrategy;
import io.github.serp.platform.redis.properties.SerpRedisProperties;

public class RedisSerpLockService implements SerpLockService {
    private static final String COMPARE_AND_DELETE_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) "
                    + "else "
                    + "return 0 "
                    + "end";

    private final StringRedisTemplate redisTemplate;
    private final SerpRedisKeyStrategy keyStrategy;
    private final SerpRedisProperties properties;
    private final DefaultRedisScript<Long> compareAndDeleteScript;

    public RedisSerpLockService(
            StringRedisTemplate redisTemplate,
            SerpRedisKeyStrategy keyStrategy,
            SerpRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.keyStrategy = keyStrategy;
        this.properties = properties;
        this.compareAndDeleteScript = new DefaultRedisScript<>();
        this.compareAndDeleteScript.setScriptText(COMPARE_AND_DELETE_SCRIPT);
        this.compareAndDeleteScript.setResultType(Long.class);
    }

    @Override
    public boolean tryLock(String lockName, String ownerId, Duration ttl) {
        Assert.hasText(ownerId, "Redis lock ownerId is required");
        String lockKey = keyStrategy.lockKey(lockName);
        Duration effectiveTtl = (ttl == null) ? resolveDefaultLockTtl() : ttl;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, ownerId.trim(), effectiveTtl);
        return Boolean.TRUE.equals(locked);
    }

    @Override
    public boolean unlock(String lockName, String ownerId) {
        Assert.hasText(ownerId, "Redis lock ownerId is required");
        String lockKey = keyStrategy.lockKey(lockName);
        Long result = redisTemplate.execute(compareAndDeleteScript, List.of(lockKey), ownerId.trim());
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public <T> Optional<T> executeWithLock(String lockName, String ownerId, Duration ttl, Supplier<T> task) {
        Assert.notNull(task, "Redis lock task is required");
        boolean locked = tryLock(lockName, ownerId, ttl);
        if (!locked) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(task.get());
        } finally {
            unlock(lockName, ownerId);
        }
    }

    private Duration resolveDefaultLockTtl() {
        long ttlSeconds = Math.max(1, properties.getLock().getDefaultTtlSeconds());
        return Duration.ofSeconds(ttlSeconds);
    }
}
