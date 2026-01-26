/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Redis cache adapter implementation
 */

package serp.project.discuss_service.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.port.client.ICachePort;
import serp.project.discuss_service.kernel.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis implementation of the ICachePort.
 * Provides low-level Redis operations for caching.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements ICachePort {

    private final JsonUtils jsonUtils;
    private final RedisTemplate<String, String> redisTemplate;

    // ==================== BASIC KEY-VALUE OPERATIONS ====================

    @Override
    public void setToCache(String key, Object value, long ttlSeconds) {
        try {
            String jsonValue = jsonUtils.toJson(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to set value to cache, key: {}", key, e);
            throw new RuntimeException("Failed to set value to cache", e);
        }
    }

    @Override
    public void setToCache(String key, Object value) {
        try {
            String jsonValue = jsonUtils.toJson(value);
            redisTemplate.opsForValue().set(key, jsonValue);
        } catch (Exception e) {
            log.error("Failed to set value to cache, key: {}", key, e);
            throw new RuntimeException("Failed to set value to cache", e);
        }
    }

    @Override
    public String getFromCache(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Failed to get value from cache, key: {}", key, e);
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
            log.error("Failed to get value from cache, key: {}", key, e);
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
            log.error("Failed to get value from cache, key: {}", key, e);
            return null;
        }
    }

    @Override
    public void deleteFromCache(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete value from cache, key: {}", key, e);
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
            log.error("Failed to delete values from cache by pattern, pattern: {}", pattern, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check if key exists, key: {}", key, e);
            return false;
        }
    }

    @Override
    public void expire(String key, long ttlSeconds) {
        try {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to set expiration, key: {}", key, e);
        }
    }

    // ==================== SET OPERATIONS ====================

    @Override
    public void addToSet(String key, String... members) {
        try {
            redisTemplate.opsForSet().add(key, members);
        } catch (Exception e) {
            log.error("Failed to add to set, key: {}", key, e);
        }
    }

    @Override
    public void removeFromSet(String key, String... members) {
        try {
            redisTemplate.opsForSet().remove(key, (Object[]) members);
        } catch (Exception e) {
            log.error("Failed to remove from set, key: {}", key, e);
        }
    }

    @Override
    public Set<String> getSetMembers(String key) {
        try {
            Set<String> members = redisTemplate.opsForSet().members(key);
            return members != null ? members : Collections.emptySet();
        } catch (Exception e) {
            log.error("Failed to get set members, key: {}", key, e);
            return Collections.emptySet();
        }
    }

    @Override
    public boolean isSetMember(String key, String member) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(key, member);
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.error("Failed to check set membership, key: {}", key, e);
            return false;
        }
    }

    @Override
    public long getSetSize(String key) {
        try {
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("Failed to get set size, key: {}", key, e);
            return 0L;
        }
    }

    // ==================== LIST OPERATIONS ====================

    @Override
    public void leftPush(String key, String value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("Failed to left push to list, key: {}", key, e);
        }
    }

    @Override
    public void rightPush(String key, String value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("Failed to right push to list, key: {}", key, e);
        }
    }

    @Override
    public List<String> getListRange(String key, long start, long end) {
        try {
            List<String> range = redisTemplate.opsForList().range(key, start, end);
            return range != null ? range : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to get list range, key: {}", key, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void trimList(String key, long start, long end) {
        try {
            redisTemplate.opsForList().trim(key, start, end);
        } catch (Exception e) {
            log.error("Failed to trim list, key: {}", key, e);
        }
    }

    @Override
    public long getListSize(String key) {
        try {
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("Failed to get list size, key: {}", key, e);
            return 0L;
        }
    }

    // ==================== HASH OPERATIONS ====================

    @Override
    public void hashSet(String key, String field, String value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Failed to set hash field, key: {}, field: {}", key, field, e);
        }
    }

    @Override
    public void hashSetAll(String key, Map<String, String> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error("Failed to set all hash fields, key: {}", key, e);
        }
    }

    @Override
    public String hashGet(String key, String field) {
        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("Failed to get hash field, key: {}, field: {}", key, field, e);
            return null;
        }
    }

    @Override
    public Map<String, String> hashGetAll(String key) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                return Collections.emptyMap();
            }
            return entries.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            e -> e.getKey().toString(),
                            e -> e.getValue().toString()
                    ));
        } catch (Exception e) {
            log.error("Failed to get all hash entries, key: {}", key, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void hashDelete(String key, String... fields) {
        try {
            redisTemplate.opsForHash().delete(key, (Object[]) fields);
        } catch (Exception e) {
            log.error("Failed to delete hash fields, key: {}", key, e);
        }
    }

    @Override
    public boolean hashExists(String key, String field) {
        try {
            Boolean exists = redisTemplate.opsForHash().hasKey(key, field);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check hash field existence, key: {}, field: {}", key, field, e);
            return false;
        }
    }

    @Override
    public long hashIncrement(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("Failed to increment hash field, key: {}, field: {}", key, field, e);
            return 0L;
        }
    }

    // ==================== COUNTER OPERATIONS ====================

    @Override
    public long increment(String key) {
        try {
            Long value = redisTemplate.opsForValue().increment(key);
            return value != null ? value : 0L;
        } catch (Exception e) {
            log.error("Failed to increment counter, key: {}", key, e);
            return 0L;
        }
    }

    @Override
    public long incrementBy(String key, long delta) {
        try {
            Long value = redisTemplate.opsForValue().increment(key, delta);
            return value != null ? value : 0L;
        } catch (Exception e) {
            log.error("Failed to increment counter by delta, key: {}", key, e);
            return 0L;
        }
    }

    @Override
    public long decrement(String key) {
        try {
            Long value = redisTemplate.opsForValue().decrement(key);
            return value != null ? value : 0L;
        } catch (Exception e) {
            log.error("Failed to decrement counter, key: {}", key, e);
            return 0L;
        }
    }

    // ==================== PUB/SUB OPERATIONS ====================

    @Override
    public void publish(String channel, String message) {
        try {
            redisTemplate.convertAndSend(channel, message);
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
            redisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                for (Map.Entry<String, Map<String, Long>> entry : operations.entrySet()) {
                    byte[] keyBytes = entry.getKey().getBytes();
                    for (Map.Entry<String, Long> fieldEntry : entry.getValue().entrySet()) {
                        byte[] fieldBytes = fieldEntry.getKey().getBytes();
                        connection.hashCommands().hIncrBy(keyBytes, fieldBytes, fieldEntry.getValue());
                    }
                }
                return null;
            });
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
            long deletedCount = 0;
            ScanOptions scanOptions = 
                    ScanOptions.scanOptions()
                            .match(pattern)
                            .count(batchSize)
                            .build();
            
            try (Cursor<String> cursor = 
                    redisTemplate.scan(scanOptions)) {
                List<String> keysToDelete = new ArrayList<>();
                while (cursor.hasNext()) {
                    keysToDelete.add(cursor.next());
                    if (keysToDelete.size() >= batchSize) {
                        redisTemplate.delete(keysToDelete);
                        deletedCount += keysToDelete.size();
                        keysToDelete.clear();
                    }
                }
                // Delete remaining keys
                if (!keysToDelete.isEmpty()) {
                    redisTemplate.delete(keysToDelete);
                    deletedCount += keysToDelete.size();
                }
            }
            log.debug("Scan and delete completed for pattern {}, deleted {} keys", pattern, deletedCount);
        } catch (Exception e) {
            log.error("Failed to scan and delete, pattern: {}", pattern, e);
        }
    }

    @Override
    public Set<String> scanKeys(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return Collections.emptySet();
        }
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(pattern)
                .build();
        
        Set<String> keys = new HashSet<>();
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }
}
