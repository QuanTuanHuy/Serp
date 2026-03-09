/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.client.provider;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.port.client.IEmailProviderPort;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProviderRegistry {

    private final List<IEmailProviderPort> providers;

    private final Map<EmailProvider, IEmailProviderPort> providerMap = new EnumMap<>(EmailProvider.class);

    @PostConstruct
    public void init() {
        for (IEmailProviderPort provider : providers) {
            EmailProvider type = provider.getProviderType();
            providerMap.put(type, provider);
            log.info("Registered email provider: {}", type);
        }
        log.info("Total email providers registered: {}", providerMap.size());
    }

    public IEmailProviderPort getProvider(EmailProvider providerType) {
        IEmailProviderPort provider = providerMap.get(providerType);
        if (provider == null) {
            log.warn("No provider registered for type: {}", providerType);
        }
        return provider;
    }

    public Collection<IEmailProviderPort> getAllProviders() {
        return providerMap.values();
    }

    public boolean hasProvider(EmailProvider providerType) {
        return providerMap.containsKey(providerType);
    }
}
