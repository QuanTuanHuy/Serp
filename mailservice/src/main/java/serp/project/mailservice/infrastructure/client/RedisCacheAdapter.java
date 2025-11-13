/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.mailservice.core.port.client.IRedisCachePort;
import serp.project.mailservice.kernel.utils.JsonUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements IRedisCachePort {
    private final JsonUtils jsonUtils;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void setToCache(String key, Object value, long ttl) {
        try {
            String jsonValue = jsonUtils.toJson(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set value to cache", e);
        }
    }

    @Override
    public String getFromCache(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Failed to get value from cache, key: {}", key);
            return null;
        }
    }

    @Override
    public <T> T getFromCache(String key, Class<T> clazz) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            return jsonUtils.fromJson(jsonValue, clazz);
        } catch (Exception e) {
            log.error("Failed to get value from cache, key: {}", key);
            return null;
        }
    }

    @Override
    public <T> T getFromCache(String key, ParameterizedTypeReference<T> typeReference) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            return jsonUtils.fromJson(jsonValue, typeReference);
        } catch (Exception e) {
            log.error("Failed to get value from cache, key: {}", key);
            return null;
        }
    }

    @Override
    public void deleteFromCache(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete value from cache, key: {}", key);
        }
    }

    @Override
    public void deleteAllByPattern(String pattern) {
        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Failed to delete values from cache by pattern, pattern: {}", pattern);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check if key exists, key: {}", key);
            return false;
        }
    }

    @Override
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Failed to increment key, key: {}", key);
            throw new RuntimeException("Failed to increment key: " + key, e);
        }
    }

    @Override
    public void expire(String key, Duration ttl) {
        try {
            redisTemplate.expire(key, ttl.getSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to set expiration on key, key: {}", key);
        }
    }

    @Override
    public boolean acquireLock(String key, Duration ttl) {
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(key, "LOCKED", ttl.getSeconds(), TimeUnit.SECONDS);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.error("Failed to acquire lock, key: {}", key);
            return false;
        }
    }

    @Override
    public void releaseLock(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to release lock, key: {}", key);
        }
    }
}
