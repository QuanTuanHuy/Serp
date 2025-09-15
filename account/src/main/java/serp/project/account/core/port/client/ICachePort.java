package serp.project.account.core.port.client;

import org.springframework.core.ParameterizedTypeReference;

public interface ICachePort {
    void setToCache(String key, Object value, long ttl);
    String getFromCache(String key);
    <T> T getFromCache(String key, Class<T> clazz);
    <T> T getFromCache(String key, ParameterizedTypeReference<T> typeReference);
    void deleteFromCache(String key);
    void deleteAllByPattern(String pattern);
}
