/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.client.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.port.client.IEmailProviderFactoryPort;
import serp.project.mailservice.core.port.client.IEmailProviderPort;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProviderFactory implements IEmailProviderFactoryPort {

    @Qualifier("javaMailProvider")
    private final IEmailProviderPort javaMailProvider;

    @Qualifier("brevoProvider")
    private final IEmailProviderPort brevoProvider;

    private final Map<EmailProvider, Boolean> providerHealthCache = new HashMap<>();
    private long lastHealthCheckTime = 0;
    private static final long HEALTH_CHECK_INTERVAL_MS = 60_000;

    @Override
    public IEmailProviderPort getProvider(EmailProvider provider) {
        if (provider == null) {
            log.warn("Provider is null, using default JAVA_MAIL provider");
            return javaMailProvider;
        }

        return switch (provider) {
            case JAVA_MAIL -> javaMailProvider;
            case BREVO -> brevoProvider;
            default -> {
                log.warn("Unknown provider: {}, using default JAVA_MAIL provider", provider);
                yield javaMailProvider;
            }
        };
    }

    @Override
    public IEmailProviderPort getHealthyProvider(EmailProvider preferredProvider) {
        refreshHealthCacheIfNeeded();

        if (preferredProvider != null) {
            IEmailProviderPort provider = getProvider(preferredProvider);
            if (isProviderHealthy(preferredProvider)) {
                log.debug("Using preferred provider: {}", preferredProvider);
                return provider;
            }
            log.warn("Preferred provider {} is unhealthy, trying fallback", preferredProvider);
        }

        if (isProviderHealthy(EmailProvider.JAVA_MAIL)) {
            log.info("Using JAVA_MAIL fallback provider");
            return javaMailProvider;
        }

        if (isProviderHealthy(EmailProvider.BREVO)) {
            log.info("Using Brevo fallback provider");
            return brevoProvider;
        }

        log.error("All email providers are unhealthy! Returning JAVA_MAIL provider");
        return javaMailProvider;
    }

    public boolean isProviderHealthy(EmailProvider provider) {
        refreshHealthCacheIfNeeded();
        return providerHealthCache.getOrDefault(provider, false);
    }

    public Map<EmailProvider, Boolean> checkAllProvidersHealth() {
        log.info("Performing health check for all email providers");

        Map<EmailProvider, Boolean> healthStatus = new HashMap<>();

        boolean javaMailHealthy = javaMailProvider.isHealthy();
        healthStatus.put(EmailProvider.JAVA_MAIL, javaMailHealthy);
        providerHealthCache.put(EmailProvider.JAVA_MAIL, javaMailHealthy);

        boolean brevoHealthy = brevoProvider.isHealthy();
        healthStatus.put(EmailProvider.BREVO, brevoHealthy);
        providerHealthCache.put(EmailProvider.BREVO, brevoHealthy);

        lastHealthCheckTime = System.currentTimeMillis();

        log.info("Health check completed. JAVA_MAIL: {}, Brevo: {}", javaMailHealthy, brevoHealthy);

        return healthStatus;
    }

    public Map<EmailProvider, Boolean> getProvidersHealthStatus() {
        refreshHealthCacheIfNeeded();
        return new HashMap<>(providerHealthCache);
    }

    private void refreshHealthCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastHealthCheckTime > HEALTH_CHECK_INTERVAL_MS) {
            checkAllProvidersHealth();
        }
    }

    public void invalidateHealthCache() {
        providerHealthCache.clear();
        lastHealthCheckTime = 0;
        log.debug("Health cache invalidated");
    }
}
