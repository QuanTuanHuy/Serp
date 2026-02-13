/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.cache;

import java.time.Duration;
import java.util.Optional;

public interface SerpCacheService {
    void put(String namespace, String key, Object value);

    void put(String namespace, String key, Object value, Duration ttl);

    Optional<Object> get(String namespace, String key);

    <T> Optional<T> get(String namespace, String key, Class<T> type);

    void evict(String namespace, String key);
}
