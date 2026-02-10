/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.mailservice.core.domain.constant.RedisKey;
import serp.project.mailservice.core.exception.AppException;
import serp.project.mailservice.core.exception.ErrorCode;
import serp.project.mailservice.core.domain.dto.response.ProviderHealthResponse;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.ProviderStatus;
import serp.project.mailservice.core.port.client.IEmailProviderPort;
import serp.project.mailservice.core.port.client.IRedisCachePort;
import serp.project.mailservice.infrastructure.client.provider.EmailProviderRegistry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProviderService implements IEmailProviderService {

    private final EmailProviderRegistry emailProviderRegistry;
    private final IRedisCachePort redisCachePort;

    private static final Duration DEFAULT_DOWNTIME = Duration.ofMinutes(5);

    @Override
    public IEmailProviderPort selectProvider(EmailEntity email) {
        log.debug("Selecting provider for email: {}", email.getMessageId());

        if (email.getProvider() != null) {
            EmailProvider requestedProvider = email.getProvider();

            if (isProviderHealthy(requestedProvider)) {
                log.info("Using requested provider: {} for email: {}", requestedProvider, email.getMessageId());
                return emailProviderRegistry.getProvider(requestedProvider);
            } else {
                log.warn("Requested provider {} is down, falling back to healthy provider", requestedProvider);
            }
        }

        IEmailProviderPort provider = getHealthyProvider();

        if (provider == null) {
            log.error("No healthy email provider available for email: {}", email.getMessageId());
            throw new AppException(ErrorCode.NO_HEALTHY_PROVIDER);
        }

        log.info("Selected provider: {} for email: {}", provider.getProviderName(), email.getMessageId());
        return provider;
    }

    @Override
    public void markProviderDown(EmailProvider provider, Duration downtime) {
        if (provider == null) {
            return;
        }

        Duration effectiveDowntime = downtime != null ? downtime : DEFAULT_DOWNTIME;
        String healthKey = RedisKey.PROVIDER_HEALTH_PREFIX + provider.name();

        redisCachePort.setToCache(healthKey, "DOWN", effectiveDowntime.toSeconds());

        log.warn("Marked provider {} as DOWN for {} seconds", provider, effectiveDowntime.toSeconds());
    }

    @Override
    public void markProviderUp(EmailProvider provider) {
        if (provider == null) {
            return;
        }

        String healthKey = RedisKey.PROVIDER_HEALTH_PREFIX + provider.name();
        redisCachePort.deleteFromCache(healthKey);

        log.info("Marked provider {} as UP", provider);
    }

    @Override
    public boolean isProviderHealthy(EmailProvider provider) {
        if (provider == null) {
            return false;
        }

        String healthKey = RedisKey.PROVIDER_HEALTH_PREFIX + provider.name();

        String healthStatus = redisCachePort.getFromCache(healthKey);
        boolean isHealthy = healthStatus == null || !"DOWN".equals(healthStatus);

        log.debug("Provider {} health status: {}", provider, isHealthy ? "UP" : "DOWN");

        return isHealthy;
    }

    @Override
    public IEmailProviderPort getProvider(EmailProvider providerType) {
        if (providerType == null) {
            return null;
        }

        if (!isProviderHealthy(providerType)) {
            log.warn("Provider {} is marked as unhealthy", providerType);
            return null;
        }

        return emailProviderRegistry.getProvider(providerType);
    }

    @Override
    public IEmailProviderPort getHealthyProvider() {
        for (IEmailProviderPort provider : emailProviderRegistry.getAllProviders()) {
            if (isProviderHealthy(provider.getProviderType())) {
                return provider;
            }
        }

        log.error("No healthy email provider found, returning first available provider");
        return emailProviderRegistry.getAllProviders().stream().findFirst().orElse(null);
    }

    @Override
    public void updateProviderHealth(EmailProvider provider, boolean isHealthy) {
        if (isHealthy) {
            markProviderUp(provider);
        } else {
            markProviderDown(provider, DEFAULT_DOWNTIME);
        }
    }

    @Override
    public void checkAllProviderHealth() {
        log.info("Performing health check on all email providers");

        for (IEmailProviderPort provider : emailProviderRegistry.getAllProviders()) {
            EmailProvider providerType = provider.getProviderType();
            try {
                boolean isHealthy = provider.isHealthy();

                if (isHealthy) {
                    markProviderUp(providerType);
                    log.info("Health check passed for provider: {}", providerType);
                } else {
                    markProviderDown(providerType, DEFAULT_DOWNTIME);
                    log.warn("Health check failed for provider: {}", providerType);
                }
            } catch (Exception e) {
                log.error("Error checking health for provider: {}", providerType, e);
                markProviderDown(providerType, DEFAULT_DOWNTIME);
            }
        }

        log.info("Health check completed for all providers");
    }

    @Override
    public List<ProviderHealthResponse> getAllProviderHealth() {
        return Arrays.stream(EmailProvider.values())
                .filter(emailProviderRegistry::hasProvider)
                .map(provider -> {
                    boolean healthy = isProviderHealthy(provider);
                    return ProviderHealthResponse.builder()
                            .provider(provider)
                            .status(healthy ? ProviderStatus.UP : ProviderStatus.DOWN)
                            .lastCheckAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
