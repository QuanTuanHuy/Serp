/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Redis compatibility config for SERP platform starter
 */

package serp.project.discuss_service.kernel.config;

import io.github.serp.platform.redis.key.SerpRedisKeyStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

@Configuration
public class PlatformRedisCompatibilityConfig {

    @Bean(name = "serpRedisTemplate")
    public RedisTemplate<String, Object> serpRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        redisTemplate.setDefaultSerializer(stringSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    public SerpRedisKeyStrategy serpRedisKeyStrategy() {
        return new SerpRedisKeyStrategy() {
            @Override
            public String cacheKey(String namespace, String key) {
                Assert.hasText(key, "Redis cache key is required");
                return key.trim();
            }

            @Override
            public String lockKey(String lockName) {
                Assert.hasText(lockName, "Redis lock name is required");
                return lockName.trim();
            }
        };
    }
}
