package serp.project.school_bus_service.kernel.shared.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public boolean hasAnyRole(String... roleNames) {
        List<String> roles = getAllRoles();
        for (String roleName : roleNames) {
            if (roles.contains(roleName) || roles.contains(roleName.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public void requireAnyRole(String... roleNames) {
        if (!hasAnyRole(roleNames)) {
            throw new AppException(AppErrorCode.FORBIDDEN);
        }
    }

    private List<String> getAllRoles() {
        try {
            return getCurrentJwt()
                    .map(jwt -> {
                        List<String> allRoles = new ArrayList<>();

                        Object realmAccess = jwt.getClaim("realm_access");
                        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
                            Object realmRoles = realmAccessMap.get("roles");
                            if (realmRoles instanceof List<?> roles) {
                                roles.forEach(role -> allRoles.add(String.valueOf(role)));
                            }
                        }

                        Object resourceAccess = jwt.getClaim("resource_access");
                        if (resourceAccess instanceof Map<?, ?> resourceAccessMap) {
                            for (Object clientAccess : resourceAccessMap.values()) {
                                if (clientAccess instanceof Map<?, ?> clientAccessMap) {
                                    Object clientRoles = clientAccessMap.get("roles");
                                    if (clientRoles instanceof List<?> roles) {
                                        roles.forEach(role -> allRoles.add(String.valueOf(role)));
                                    }
                                }
                            }
                        }

                        return allRoles.stream()
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList();
                    })
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
