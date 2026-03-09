/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.mailservice.core.domain.constant.RedisKey;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.port.client.IRedisCachePort;
import serp.project.mailservice.kernel.property.RateLimitProperties;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService implements IRateLimitService {

    private final IRedisCachePort redisCachePort;
    private final RateLimitProperties rateLimitProperties;

    @Override
    public boolean allowRequest(Long tenantId) {
        if (tenantId == null) {
            log.warn("Tenant ID is null, denying request");
            return false;
        }
        String rateLimitKey = RedisKey.RATE_LIMIT_TENANT_PREFIX + tenantId;
        return checkRateLimit(
                rateLimitKey, 
                rateLimitProperties.getDefaultPerTenantPerMinute(),
                Duration.ofMinutes(1)
        );
    }

    @Override
    public boolean allowProviderRequest(EmailProvider provider) {
        if (provider == null) {
            log.warn("Provider is null, denying request");
            return false;
        }
        String rateLimitKey = RedisKey.RATE_LIMIT_PROVIDER_PREFIX + provider.name();
        return checkRateLimit(
                rateLimitKey,
                rateLimitProperties.getDefaultPerProviderPerMinute(),
                Duration.ofMinutes(1)
        );
    }

    @Override
    public boolean allowUserRequest(Long userId) {
        if (userId == null) {
            log.warn("User ID is null, denying request");
            return false;
        }
        String rateLimitKey = RedisKey.RATE_LIMIT_USER_PREFIX + userId;
        
        return checkRateLimit(
                rateLimitKey,
                rateLimitProperties.getDefaultPerUserPerMinute(),
                Duration.ofMinutes(1)
        );
    }

    @Override
    public long getRemainingQuota(Long tenantId) {
        if (tenantId == null) {
            return 0;
        }
        String rateLimitKey = RedisKey.RATE_LIMIT_TENANT_PREFIX + tenantId;
        if (!redisCachePort.exists(rateLimitKey)) {
            return rateLimitProperties.getDefaultPerTenantPerMinute();
        }

        String countStr = redisCachePort.getFromCache(rateLimitKey);
        long currentCount = countStr != null ? Long.parseLong(countStr) : 0;
        
        long remaining = rateLimitProperties.getDefaultPerTenantPerMinute() - currentCount;
        
        return Math.max(0, remaining);
    }

    public long getProviderRemainingQuota(EmailProvider provider) {
        if (provider == null) {
            return 0;
        }

        String rateLimitKey = RedisKey.RATE_LIMIT_PROVIDER_PREFIX + provider.name();
        
        if (!redisCachePort.exists(rateLimitKey)) {
            return rateLimitProperties.getDefaultPerProviderPerMinute();
        }

        String countStr = redisCachePort.getFromCache(rateLimitKey);
        long currentCount = countStr != null ? Long.parseLong(countStr) : 0;
        
        long remaining = rateLimitProperties.getDefaultPerProviderPerMinute() - currentCount;
        
        return Math.max(0, remaining);
    }

    private boolean checkRateLimit(String key, int maxRequests, Duration window) {
        try {
            Long currentCount = redisCachePort.increment(key);
            if (currentCount == null) {
                log.error("Failed to increment rate limit counter for key: {}", key);
                return false;
            }

            if (currentCount == 1) {
                redisCachePort.expire(key, window);
            }

            boolean allowed = currentCount <= maxRequests;
            
            if (!allowed) {
                log.warn("Rate limit exceeded for key: {} ({}/{} requests)", key, currentCount, maxRequests);
            } else {
                log.debug("Rate limit check passed for key: {} ({}/{} requests)", key, currentCount, maxRequests);
            }
            
            return allowed;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            return true;
        }
    }

    public void resetTenantRateLimit(Long tenantId) {
        if (tenantId == null) {
            return;
        }

        String rateLimitKey = RedisKey.RATE_LIMIT_TENANT_PREFIX + tenantId;
        redisCachePort.deleteFromCache(rateLimitKey);
        
        log.info("Reset rate limit for tenant: {}", tenantId);
    }

    public void resetProviderRateLimit(EmailProvider provider) {
        if (provider == null) {
            return;
        }

        String rateLimitKey = RedisKey.RATE_LIMIT_PROVIDER_PREFIX + provider.name();
        redisCachePort.deleteFromCache(rateLimitKey);
        
        log.info("Reset rate limit for provider: {}", provider);
    }
}
