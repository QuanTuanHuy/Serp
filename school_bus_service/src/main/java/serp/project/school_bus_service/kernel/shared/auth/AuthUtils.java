package serp.project.school_bus_service.kernel.shared.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;

import java.util.Optional;

@Component
public class AuthUtils {

    public Optional<Jwt> getCurrentJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                return Optional.of(jwt);
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Long getCurrentUserIdOrThrow() {
        return Optional.ofNullable(getCurrentUserIdValue())
                .map(Long::valueOf)
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
    }

    public Long getCurrentTenantIdOrThrow() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("tid"))
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
    }

    public String getCurrentUserIdValue() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("uid"))
                .filter(value -> !value.isBlank())
                .orElse(null);
    }
}
