/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.kernel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import serp.project.tms_billing_service.kernel.utils.AuthUtils;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class JpaAuditingConfig {
    private static final String SYSTEM_AUDITOR = "system";

    private final AuthUtils authUtils;

    @Bean(name = "auditorAware")
    public AuditorAware<String> auditorAware() {
        return () -> {
            Optional<String> currentAuditPrincipal = authUtils.getCurrentAuditPrincipal();
            return currentAuditPrincipal.isPresent()
                    ? currentAuditPrincipal
                    : Optional.of(SYSTEM_AUDITOR);
        };
    }
}

