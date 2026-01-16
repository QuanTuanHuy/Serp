/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Generic Redis cache port
 */

package serp.project.discuss_service.core.port.client;

import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generic cache port for Redis operations.
 * Provides low-level cache operations that can be used by any service.
 */
public interface ICachePort {

    // ==================== BASIC KEY-VALUE OPERATIONS ====================

    /**
     * Set a value in cache with TTL
     * @param key Cache key
     * @param value Value to cache (will be serialized to JSON)
     * @param ttlSeconds Time to live in seconds
     */
    void setToCache(String key, Object value, long ttlSeconds);

    /**
     * Set a value in cache without TTL (persistent)
     */
    void setToCache(String key, Object value);

    /**
     * Get raw string value from cache
     */
    String getFromCache(String key);

    /**
     * Get typed value from cache
     */
    <T> T getFromCache(String key, Class<T> clazz);

    /**
     * Get typed value from cache with parameterized type (for generics like List<T>)
     */
    <T> T getFromCache(String key, ParameterizedTypeReference<T> typeReference);

    /**
     * Delete a key from cache
     */
    void deleteFromCache(String key);

    /**
     * Delete all keys matching a pattern
     */
    void deleteAllByPattern(String pattern);

    /**
     * Check if key exists in cache
     */
    boolean exists(String key);

    /**
     * Set expiration time on a key
     */
    void expire(String key, long ttlSeconds);

    // ==================== SET OPERATIONS ====================

    /**
     * Add members to a set
     */
    void addToSet(String key, String... members);

    /**
     * Remove members from a set
     */
    void removeFromSet(String key, String... members);

    /**
     * Get all members of a set
     */
    Set<String> getSetMembers(String key);

    /**
     * Check if member exists in set
     */
    boolean isSetMember(String key, String member);

    /**
     * Get set size
     */
    long getSetSize(String key);

    // ==================== LIST OPERATIONS ====================

    /**
     * Push to left of list
     */
    void leftPush(String key, String value);

    /**
     * Push to right of list
     */
    void rightPush(String key, String value);

    /**
     * Get range from list
     */
    List<String> getListRange(String key, long start, long end);

    /**
     * Trim list to specified range
     */
    void trimList(String key, long start, long end);

    /**
     * Get list size
     */
    long getListSize(String key);

    // ==================== HASH OPERATIONS ====================

    /**
     * Set a hash field
     */
    void hashSet(String key, String field, String value);

    /**
     * Set multiple hash fields
     */
    void hashSetAll(String key, Map<String, String> map);

    /**
     * Get a hash field
     */
    String hashGet(String key, String field);

    /**
     * Get all hash entries
     */
    Map<String, String> hashGetAll(String key);

    /**
     * Delete hash fields
     */
    void hashDelete(String key, String... fields);

    /**
     * Check if hash field exists
     */
    boolean hashExists(String key, String field);

    /**
     * Increment hash field value
     */
    long hashIncrement(String key, String field, long delta);

    // ==================== COUNTER OPERATIONS ====================

    /**
     * Increment a counter
     */
    long increment(String key);

    /**
     * Increment by delta
     */
    long incrementBy(String key, long delta);

    /**
     * Decrement a counter
     */
    long decrement(String key);

    // ==================== PUB/SUB OPERATIONS ====================

    /**
     * Publish message to a channel
     */
    void publish(String channel, String message);

    // ==================== BATCH/PIPELINE OPERATIONS ====================

    /**
     * Batch increment multiple hash fields across different keys using Redis pipeline.
     * This is much more efficient than calling hashIncrement multiple times.
     *
     * @param operations Map of key -> (field -> delta) to increment
     */
    void batchHashIncrement(Map<String, Map<String, Long>> operations);

    /**
     * Delete keys matching a pattern using SCAN (non-blocking) instead of KEYS.
     * This is safer for production use as it doesn't block Redis.
     *
     * @param pattern Redis key pattern (e.g., "prefix:*")
     * @param batchSize Number of keys to scan per iteration
     */
    void scanAndDelete(String pattern, int batchSize);
}
