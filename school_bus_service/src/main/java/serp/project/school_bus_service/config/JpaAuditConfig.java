package serp.project.school_bus_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class JpaAuditConfig {

    private final AuthUtils authUtils;

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(authUtils.getCurrentUserIdValue()).or(() -> Optional.of("SYSTEM"));
    }
}
