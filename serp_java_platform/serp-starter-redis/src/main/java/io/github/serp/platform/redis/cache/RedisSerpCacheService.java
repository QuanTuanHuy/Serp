/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.cache;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Override
    public long evictByPattern(String namespace, String pattern, int batchSize) {
        String resolvedPattern = resolvePattern(namespace, pattern);
        int resolvedBatchSize = resolveBatchSize(batchSize);

        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(resolvedPattern)
                .count(resolvedBatchSize)
                .build();

        long deletedCount = 0L;
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            List<String> keysToDelete = new ArrayList<>();
            while (cursor.hasNext()) {
                keysToDelete.add(cursor.next());
                if (keysToDelete.size() >= resolvedBatchSize) {
                    Long removed = redisTemplate.delete(keysToDelete);
                    deletedCount += removed == null ? 0L : removed;
                    keysToDelete.clear();
                }
            }

            if (!keysToDelete.isEmpty()) {
                Long removed = redisTemplate.delete(keysToDelete);
                deletedCount += removed == null ? 0L : removed;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to evict by pattern: " + resolvedPattern, ex);
        }

        return deletedCount;
    }

    @Override
    public Set<String> scanKeys(String namespace, String pattern) {
        String resolvedPattern = resolvePattern(namespace, pattern);
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(resolvedPattern)
                .build();

        Set<String> keys = new LinkedHashSet<>();
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to scan keys for pattern: " + resolvedPattern, ex);
        }

        return keys;
    }

    @Override
    public boolean exists(String namespace, String key) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }

    @Override
    public void expire(String namespace, String key, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be greater than zero");
        }
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.expire(cacheKey, ttl);
    }

    @Override
    public void addToSet(String namespace, String key, String... members) {
        if (members == null || members.length == 0) {
            return;
        }
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.opsForSet().add(cacheKey, (Object[]) members);
    }

    @Override
    public void removeFromSet(String namespace, String key, String... members) {
        if (members == null || members.length == 0) {
            return;
        }
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.opsForSet().remove(cacheKey, (Object[]) members);
    }

    @Override
    public Set<String> getSetMembers(String namespace, String key) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        Set<Object> members = redisTemplate.opsForSet().members(cacheKey);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream().map(String::valueOf).collect(Collectors.toSet());
    }

    @Override
    public boolean isSetMember(String namespace, String key, String member) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(cacheKey, member));
    }

    @Override
    public long getSetSize(String namespace, String key) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        Long size = redisTemplate.opsForSet().size(cacheKey);
        return size == null ? 0L : size;
    }

    @Override
    public void leftPush(String namespace, String key, String value) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.opsForList().leftPush(cacheKey, value);
    }

    @Override
    public void rightPush(String namespace, String key, String value) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.opsForList().rightPush(cacheKey, value);
    }

    @Override
    public List<String> getListRange(String namespace, String key, long start, long end) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        List<Object> values = redisTemplate.opsForList().range(cacheKey, start, end);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream().map(String::valueOf).toList();
    }

    @Override
    public void trimList(String namespace, String key, long start, long end) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.opsForList().trim(cacheKey, start, end);
    }

    @Override
    public long getListSize(String namespace, String key) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        Long size = redisTemplate.opsForList().size(cacheKey);
        return size == null ? 0L : size;
    }

    @Override
    public void hashPut(String namespace, String key, String field, String value) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.opsForHash().put(cacheKey, field, value);
    }

    @Override
    public void hashPutAll(String namespace, String key, Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.opsForHash().putAll(cacheKey, map);
    }

    @Override
    public Optional<String> hashGet(String namespace, String key, String field) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        Object value = redisTemplate.opsForHash().get(cacheKey, field);
        return Optional.ofNullable(value).map(String::valueOf);
    }

    @Override
    public Map<String, String> hashGetAll(String namespace, String key) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cacheKey);
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyMap();
        }
        return entries.entrySet().stream().collect(Collectors.toMap(
                entry -> String.valueOf(entry.getKey()),
                entry -> String.valueOf(entry.getValue())));
    }

    @Override
    public void hashDelete(String namespace, String key, String... fields) {
        if (fields == null || fields.length == 0) {
            return;
        }
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        redisTemplate.opsForHash().delete(cacheKey, (Object[]) fields);
    }

    @Override
    public boolean hashExists(String namespace, String key, String field) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(cacheKey, field));
    }

    @Override
    public long hashIncrement(String namespace, String key, String field, long delta) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        Long value = redisTemplate.opsForHash().increment(cacheKey, field, delta);
        return value == null ? 0L : value;
    }

    @Override
    public long increment(String namespace, String key) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        Long value = redisTemplate.opsForValue().increment(cacheKey);
        return value == null ? 0L : value;
    }

    @Override
    public long incrementBy(String namespace, String key, long delta) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        Long value = redisTemplate.opsForValue().increment(cacheKey, delta);
        return value == null ? 0L : value;
    }

    @Override
    public long decrement(String namespace, String key) {
        String cacheKey = keyStrategy.cacheKey(namespace, key);
        Long value = redisTemplate.opsForValue().decrement(cacheKey);
        return value == null ? 0L : value;
    }

    @Override
    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }

    @Override
    public void batchHashIncrement(String namespace, Map<String, Map<String, Long>> operations) {
        if (operations == null || operations.isEmpty()) {
            return;
        }

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Map.Entry<String, Map<String, Long>> entry : operations.entrySet()) {
                String resolvedKey = keyStrategy.cacheKey(namespace, entry.getKey());
                byte[] keyBytes = resolvedKey.getBytes(StandardCharsets.UTF_8);
                for (Map.Entry<String, Long> fieldEntry : entry.getValue().entrySet()) {
                    byte[] fieldBytes = fieldEntry.getKey().getBytes(StandardCharsets.UTF_8);
                    connection.hashCommands().hIncrBy(keyBytes, fieldBytes, fieldEntry.getValue());
                }
            }
            return null;
        });
    }

    @Override
    public Map<String, Map<String, String>> batchHashGetAll(String namespace, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> resolvedKeys = keys.stream()
                .map(key -> keyStrategy.cacheKey(namespace, key))
                .toList();

        List<Object> responses = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : resolvedKeys) {
                connection.hashCommands().hGetAll(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        return IntStream.range(0, keys.size())
                .boxed()
                .collect(Collectors.toMap(
                        keys::get,
                        index -> convertHashResponse(responses.get(index)),
                        (left, right) -> left,
                        HashMap::new));
    }

    private Duration resolveDefaultCacheTtl() {
        long ttlSeconds = Math.max(1, properties.getCache().getDefaultTtlSeconds());
        return Duration.ofSeconds(ttlSeconds);
    }

    private String resolvePattern(String namespace, String pattern) {
        String safePattern = (pattern == null || pattern.isBlank()) ? "*" : pattern.trim();
        return keyStrategy.cacheKey(namespace, safePattern);
    }

    private int resolveBatchSize(int batchSize) {
        if (batchSize <= 0) {
            return 500;
        }
        return batchSize;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> convertHashResponse(Object response) {
        if (!(response instanceof Map<?, ?> rawMap) || rawMap.isEmpty()) {
            return Collections.emptyMap();
        }

        return ((Map<Object, Object>) rawMap).entrySet().stream().collect(Collectors.toMap(
                entry -> String.valueOf(entry.getKey()),
                entry -> String.valueOf(entry.getValue())));
    }
}
