/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for PlatformRedisCompatibilityConfig
 */

package serp.project.discuss_service.kernel.config;

import io.github.serp.platform.redis.key.SerpRedisKeyStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PlatformRedisCompatibilityConfigTest {

    private final PlatformRedisCompatibilityConfig config = new PlatformRedisCompatibilityConfig();

    @Test
    void serpRedisKeyStrategy_ShouldKeepLegacyKeyFormat() {
        SerpRedisKeyStrategy keyStrategy = config.serpRedisKeyStrategy();

        assertEquals("discuss:msg:123", keyStrategy.cacheKey("legacy", "discuss:msg:123"));
        assertEquals("discuss:typing:*", keyStrategy.cacheKey("legacy", "  discuss:typing:*  "));
        assertEquals("discuss:lock:presence", keyStrategy.lockKey(" discuss:lock:presence "));
    }

    @Test
    void serpRedisKeyStrategy_ShouldRejectBlankKeys() {
        SerpRedisKeyStrategy keyStrategy = config.serpRedisKeyStrategy();

        assertThrows(IllegalArgumentException.class, () -> keyStrategy.cacheKey("legacy", " "));
        assertThrows(IllegalArgumentException.class, () -> keyStrategy.lockKey(" "));
    }

    @Test
    void serpRedisTemplate_ShouldUseStringSerializersForCompatibility() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> redisTemplate = config.serpRedisTemplate(connectionFactory);

        assertSame(connectionFactory, redisTemplate.getConnectionFactory());
        assertInstanceOf(StringRedisSerializer.class, redisTemplate.getKeySerializer());
        assertInstanceOf(StringRedisSerializer.class, redisTemplate.getValueSerializer());
        assertInstanceOf(StringRedisSerializer.class, redisTemplate.getHashKeySerializer());
        assertInstanceOf(StringRedisSerializer.class, redisTemplate.getHashValueSerializer());
        assertInstanceOf(StringRedisSerializer.class, redisTemplate.getDefaultSerializer());
    }
}
