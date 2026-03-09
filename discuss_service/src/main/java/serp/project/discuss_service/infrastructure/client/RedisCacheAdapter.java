/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Redis cache adapter implementation
 */

package serp.project.discuss_service.infrastructure.client;

import io.github.serp.platform.redis.cache.SerpCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.port.client.ICachePort;
import serp.project.discuss_service.kernel.utils.JsonUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis implementation of the ICachePort.
 * Provides low-level Redis operations for caching.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements ICachePort {

    private static final String NAMESPACE = "legacy";

    private final JsonUtils jsonUtils;
    private final SerpCacheService cacheService;

    // ==================== BASIC KEY-VALUE OPERATIONS ====================

    @Override
    public void setToCache(String key, Object value, long ttlSeconds) {
        try {
            String jsonValue = jsonUtils.toJson(value);
            cacheService.put(NAMESPACE, key, jsonValue, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Failed to set value to cache, key: {}", key, e);
            throw new RuntimeException("Failed to set value to cache", e);
        }
    }

    @Override
    public void setToCache(String key, Object value) {
        try {
            String jsonValue = jsonUtils.toJson(value);
            cacheService.put(NAMESPACE, key, jsonValue);
        } catch (Exception e) {
            log.error("Failed to set value to cache, key: {}", key, e);
            throw new RuntimeException("Failed to set value to cache", e);
        }
    }

    @Override
    public String getFromCache(String key) {
        try {
            return cacheService.get(NAMESPACE, key)
                    .map(String::valueOf)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Failed to get value from cache, key: {}", key, e);
            return null;
        }
    }

    @Override
    public <T> T getFromCache(String key, Class<T> clazz) {
        try {
            String jsonValue = getFromCache(key);
            if (jsonValue == null) {
                return null;
            }
            return jsonUtils.fromJson(jsonValue, clazz);
        } catch (Exception e) {
            log.error("Failed to get value from cache, key: {}", key, e);
            return null;
        }
    }

    @Override
    public <T> T getFromCache(String key, ParameterizedTypeReference<T> typeReference) {
        try {
            String jsonValue = getFromCache(key);
            if (jsonValue == null) {
                return null;
            }
            return jsonUtils.fromJson(jsonValue, typeReference);
        } catch (Exception e) {
            log.error("Failed to get value from cache, key: {}", key, e);
            return null;
        }
    }

    @Override
    public void deleteFromCache(String key) {
        try {
            cacheService.evict(NAMESPACE, key);
        } catch (Exception e) {
            log.error("Failed to delete value from cache, key: {}", key, e);
        }
    }

    @Override
    public void deleteAllByPattern(String pattern) {
        try {
            cacheService.evictByPattern(NAMESPACE, pattern, 500);
        } catch (Exception e) {
            log.error("Failed to delete values from cache by pattern, pattern: {}", pattern, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return cacheService.exists(NAMESPACE, key);
        } catch (Exception e) {
            log.error("Failed to check if key exists, key: {}", key, e);
            return false;
        }
    }

    @Override
    public void expire(String key, long ttlSeconds) {
        try {
            cacheService.expire(NAMESPACE, key, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Failed to set expiration, key: {}", key, e);
        }
    }

    // ==================== SET OPERATIONS ====================

    @Override
    public void addToSet(String key, String... members) {
        try {
            cacheService.addToSet(NAMESPACE, key, members);
        } catch (Exception e) {
            log.error("Failed to add to set, key: {}", key, e);
        }
    }

    @Override
    public void removeFromSet(String key, String... members) {
        try {
            cacheService.removeFromSet(NAMESPACE, key, members);
        } catch (Exception e) {
            log.error("Failed to remove from set, key: {}", key, e);
        }
    }

    @Override
    public Set<String> getSetMembers(String key) {
        try {
            Set<String> members = cacheService.getSetMembers(NAMESPACE, key);
            return members != null ? members : Collections.emptySet();
        } catch (Exception e) {
            log.error("Failed to get set members, key: {}", key, e);
            return Collections.emptySet();
        }
    }

    @Override
    public boolean isSetMember(String key, String member) {
        try {
            return cacheService.isSetMember(NAMESPACE, key, member);
        } catch (Exception e) {
            log.error("Failed to check set membership, key: {}", key, e);
            return false;
        }
    }

    @Override
    public long getSetSize(String key) {
        try {
            return cacheService.getSetSize(NAMESPACE, key);
        } catch (Exception e) {
            log.error("Failed to get set size, key: {}", key, e);
            return 0L;
        }
    }

    // ==================== LIST OPERATIONS ====================

    @Override
    public void leftPush(String key, String value) {
        try {
            cacheService.leftPush(NAMESPACE, key, value);
        } catch (Exception e) {
            log.error("Failed to left push to list, key: {}", key, e);
        }
    }

    @Override
    public void rightPush(String key, String value) {
        try {
            cacheService.rightPush(NAMESPACE, key, value);
        } catch (Exception e) {
            log.error("Failed to right push to list, key: {}", key, e);
        }
    }

    @Override
    public List<String> getListRange(String key, long start, long end) {
        try {
            List<String> range = cacheService.getListRange(NAMESPACE, key, start, end);
            return range != null ? range : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to get list range, key: {}", key, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void trimList(String key, long start, long end) {
        try {
            cacheService.trimList(NAMESPACE, key, start, end);
        } catch (Exception e) {
            log.error("Failed to trim list, key: {}", key, e);
        }
    }

    @Override
    public long getListSize(String key) {
        try {
            return cacheService.getListSize(NAMESPACE, key);
        } catch (Exception e) {
            log.error("Failed to get list size, key: {}", key, e);
            return 0L;
        }
    }

    // ==================== HASH OPERATIONS ====================

    @Override
    public void hashSet(String key, String field, String value) {
        try {
            cacheService.hashPut(NAMESPACE, key, field, value);
        } catch (Exception e) {
            log.error("Failed to set hash field, key: {}, field: {}", key, field, e);
        }
    }

    @Override
    public void hashSetAll(String key, Map<String, String> map) {
        try {
            cacheService.hashPutAll(NAMESPACE, key, map);
        } catch (Exception e) {
            log.error("Failed to set all hash fields, key: {}", key, e);
        }
    }

    @Override
    public String hashGet(String key, String field) {
        try {
            return cacheService.hashGet(NAMESPACE, key, field).orElse(null);
        } catch (Exception e) {
            log.error("Failed to get hash field, key: {}, field: {}", key, field, e);
            return null;
        }
    }

    @Override
    public Map<String, String> hashGetAll(String key) {
        try {
            Map<String, String> entries = cacheService.hashGetAll(NAMESPACE, key);
            return entries != null ? entries : Collections.emptyMap();
        } catch (Exception e) {
            log.error("Failed to get all hash entries, key: {}", key, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void hashDelete(String key, String... fields) {
        try {
            cacheService.hashDelete(NAMESPACE, key, fields);
        } catch (Exception e) {
            log.error("Failed to delete hash fields, key: {}", key, e);
        }
    }

    @Override
    public boolean hashExists(String key, String field) {
        try {
            return cacheService.hashExists(NAMESPACE, key, field);
        } catch (Exception e) {
            log.error("Failed to check hash field existence, key: {}, field: {}", key, field, e);
            return false;
        }
    }

    @Override
    public long hashIncrement(String key, String field, long delta) {
        try {
            return cacheService.hashIncrement(NAMESPACE, key, field, delta);
        } catch (Exception e) {
            log.error("Failed to increment hash field, key: {}, field: {}", key, field, e);
            return 0L;
        }
    }

    // ==================== COUNTER OPERATIONS ====================

    @Override
    public long increment(String key) {
        try {
            return cacheService.increment(NAMESPACE, key);
        } catch (Exception e) {
            log.error("Failed to increment counter, key: {}", key, e);
            return 0L;
        }
    }

    @Override
    public long incrementBy(String key, long delta) {
        try {
            return cacheService.incrementBy(NAMESPACE, key, delta);
        } catch (Exception e) {
            log.error("Failed to increment counter by delta, key: {}", key, e);
            return 0L;
        }
    }

    @Override
    public long decrement(String key) {
        try {
            return cacheService.decrement(NAMESPACE, key);
        } catch (Exception e) {
            log.error("Failed to decrement counter, key: {}", key, e);
            return 0L;
        }
    }

    // ==================== PUB/SUB OPERATIONS ====================

    @Override
    public void publish(String channel, String message) {
        try {
            cacheService.publish(channel, message);
        } catch (Exception e) {
            log.error("Failed to publish message, channel: {}", channel, e);
        }
    }

    // ==================== BATCH/PIPELINE OPERATIONS ====================

    @Override
    public void batchHashIncrement(Map<String, Map<String, Long>> operations) {
        if (operations == null || operations.isEmpty()) {
            return;
        }
        try {
            cacheService.batchHashIncrement(NAMESPACE, operations);
            log.debug("Batch hash increment completed for {} keys", operations.size());
        } catch (Exception e) {
            log.error("Failed to batch hash increment", e);
        }
    }

    @Override
    public void scanAndDelete(String pattern, int batchSize) {
        if (pattern == null || pattern.isEmpty()) {
            return;
        }
        try {
            cacheService.evictByPattern(NAMESPACE, pattern, batchSize);
        } catch (Exception e) {
            log.error("Failed to scan and delete, pattern: {}", pattern, e);
        }
    }

    @Override
    public Set<String> scanKeys(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return Collections.emptySet();
        }

        try {
            return cacheService.scanKeys(NAMESPACE, pattern);
        } catch (Exception e) {
            log.error("Failed to scan keys, pattern: {}", pattern, e);
            return Collections.emptySet();
        }
    }

    @Override
    public Map<String, Map<String, String>> batchHashGetAll(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }

        try {
            return cacheService.batchHashGetAll(NAMESPACE, keys);
        } catch (Exception e) {
            log.error("Failed to batch hash get all", e);
            return new HashMap<>();
        }
    }
}
