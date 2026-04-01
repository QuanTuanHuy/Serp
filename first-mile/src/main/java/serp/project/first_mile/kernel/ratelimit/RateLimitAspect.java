/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kernel.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private static final int DEFAULT_PERMITS_PER_SECOND = 1;

    private final Environment environment;
    private final InMemoryRateLimiter inMemoryRateLimiter;

    @Around("@annotation(rateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        int permitsPerSecond = resolvePermitsPerSecond(rateLimit);
        String key = rateLimit.key();

        if (!inMemoryRateLimiter.tryAcquire(key, permitsPerSecond)) {
            log.warn("Rate limit exceeded. key={}, permitsPerSecond={}", key, permitsPerSecond);
            throw new AppException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

        return joinPoint.proceed();
    }

    private int resolvePermitsPerSecond(RateLimit rateLimit) {
        int permitsPerSecond = rateLimit.permitsPerSecond();
        String propertyKey = rateLimit.permitsPerSecondProperty();

        if (!propertyKey.isBlank()) {
            String propertyValue = environment.getProperty(propertyKey);
            if (propertyValue != null && !propertyValue.isBlank()) {
                try {
                    permitsPerSecond = Integer.parseInt(propertyValue.trim());
                } catch (NumberFormatException exception) {
                    log.warn(
                            "Invalid rate limit config value. propertyKey={}, propertyValue={}, fallback={}",
                            propertyKey,
                            propertyValue,
                            DEFAULT_PERMITS_PER_SECOND
                    );
                    permitsPerSecond = DEFAULT_PERMITS_PER_SECOND;
                }
            }
        }

        if (permitsPerSecond < 1) {
            log.warn("Invalid permitsPerSecond={}, fallback={}", permitsPerSecond, DEFAULT_PERMITS_PER_SECOND);
            return DEFAULT_PERMITS_PER_SECOND;
        }

        return permitsPerSecond;
    }
}