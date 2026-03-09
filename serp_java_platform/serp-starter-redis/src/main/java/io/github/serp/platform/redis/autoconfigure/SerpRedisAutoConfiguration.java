/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.github.serp.platform.redis.cache.RedisSerpCacheService;
import io.github.serp.platform.redis.cache.SerpCacheService;
import io.github.serp.platform.redis.key.DefaultSerpRedisKeyStrategy;
import io.github.serp.platform.redis.key.SerpRedisKeyStrategy;
import io.github.serp.platform.redis.lock.RedisSerpLockService;
import io.github.serp.platform.redis.lock.SerpLockService;
import io.github.serp.platform.redis.properties.SerpRedisProperties;

@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory.class)
@EnableConfigurationProperties(SerpRedisProperties.class)
@ConditionalOnProperty(prefix = "serp.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SerpRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "serpRedisTemplate")
    public RedisTemplate<String, Object> serpRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public SerpRedisKeyStrategy serpRedisKeyStrategy(SerpRedisProperties properties) {
        return new DefaultSerpRedisKeyStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SerpCacheService serpCacheService(
            RedisTemplate<String, Object> serpRedisTemplate,
            SerpRedisKeyStrategy keyStrategy,
            SerpRedisProperties properties) {
        return new RedisSerpCacheService(serpRedisTemplate, keyStrategy, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SerpLockService serpLockService(
            StringRedisTemplate stringRedisTemplate,
            SerpRedisKeyStrategy keyStrategy,
            SerpRedisProperties properties) {
        return new RedisSerpLockService(stringRedisTemplate, keyStrategy, properties);
    }
}
