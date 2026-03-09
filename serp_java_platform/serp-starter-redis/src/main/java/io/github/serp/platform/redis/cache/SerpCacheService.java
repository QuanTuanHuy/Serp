/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.cache;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface SerpCacheService {
    void put(String namespace, String key, Object value);

    void put(String namespace, String key, Object value, Duration ttl);

    Optional<Object> get(String namespace, String key);

    <T> Optional<T> get(String namespace, String key, Class<T> type);

    void evict(String namespace, String key);

    long evictByPattern(String namespace, String pattern, int batchSize);

    Set<String> scanKeys(String namespace, String pattern);

    boolean exists(String namespace, String key);

    void expire(String namespace, String key, Duration ttl);

    void addToSet(String namespace, String key, String... members);

    void removeFromSet(String namespace, String key, String... members);

    Set<String> getSetMembers(String namespace, String key);

    boolean isSetMember(String namespace, String key, String member);

    long getSetSize(String namespace, String key);

    void leftPush(String namespace, String key, String value);

    void rightPush(String namespace, String key, String value);

    List<String> getListRange(String namespace, String key, long start, long end);

    void trimList(String namespace, String key, long start, long end);

    long getListSize(String namespace, String key);

    void hashPut(String namespace, String key, String field, String value);

    void hashPutAll(String namespace, String key, Map<String, String> map);

    Optional<String> hashGet(String namespace, String key, String field);

    Map<String, String> hashGetAll(String namespace, String key);

    void hashDelete(String namespace, String key, String... fields);

    boolean hashExists(String namespace, String key, String field);

    long hashIncrement(String namespace, String key, String field, long delta);

    long increment(String namespace, String key);

    long incrementBy(String namespace, String key, long delta);

    long decrement(String namespace, String key);

    void publish(String channel, String message);

    void batchHashIncrement(String namespace, Map<String, Map<String, Long>> operations);

    Map<String, Map<String, String>> batchHashGetAll(String namespace, List<String> keys);
}
