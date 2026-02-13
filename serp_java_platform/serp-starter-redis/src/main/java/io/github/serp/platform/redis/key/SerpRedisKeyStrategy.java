/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.key;

public interface SerpRedisKeyStrategy {
    String cacheKey(String namespace, String key);

    String lockKey(String lockName);
}
