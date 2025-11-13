/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.client;

import org.springframework.core.ParameterizedTypeReference;

import java.time.Duration;

public interface IRedisCachePort {

    void setToCache(String key, Object value, long ttl);

    String getFromCache(String key);

    <T> T getFromCache(String key, Class<T> clazz);

    <T> T getFromCache(String key, ParameterizedTypeReference<T> typeReference);

    void deleteFromCache(String key);

    void deleteAllByPattern(String pattern);

    boolean exists(String key);

    Long increment(String key);

    void expire(String key, Duration ttl);

    boolean acquireLock(String key, Duration ttl);

    void releaseLock(String key);
}
