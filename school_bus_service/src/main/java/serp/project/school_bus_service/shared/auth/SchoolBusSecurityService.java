package serp.project.school_bus_service.shared.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SchoolBusSecurityService {

    private final AuthUtils authUtils;

    public SchoolBusSecurityService(AuthUtils authUtils) {
        this.authUtils = authUtils;
    }

    public Long getCurrentUserId() {
        return authUtils.getCurrentUserIdOrThrow();
    }

    public Long getCurrentTenantId() {
        return authUtils.getCurrentTenantIdOrThrow();
    }

    public String getCurrentKeycloakId() {
        return authUtils.getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("sub"))
                .filter(sub -> sub != null && !sub.isBlank())
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
    }

    public Set<String> getCurrentRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::normalizeRole)
                .collect(Collectors.toSet());
    }

    public boolean hasRole(String role) {
        return getCurrentRoles().contains(role.toUpperCase(Locale.ROOT));
    }

    public boolean isAdmin() {
        return hasRole(SchoolBusRole.SCHOOL_BUS_ADMIN);
    }

    public boolean isDispatcher() {
        return hasRole(SchoolBusRole.SCHOOL_BUS_DISPATCHER);
    }

    public boolean isDriver() {
        return hasRole(SchoolBusRole.SCHOOL_BUS_DRIVER);
    }

    public boolean isAttendant() {
        return hasRole(SchoolBusRole.SCHOOL_BUS_ATTENDANT);
    }

    public boolean isParent() {
        return hasRole(SchoolBusRole.SCHOOL_BUS_PARENT);
    }

    public boolean isAdminOrDispatcher() {
        return isAdmin() || isDispatcher();
    }

    private String normalizeRole(String authority) {
        String role = authority == null ? "" : authority.trim();
        if (role.toUpperCase(Locale.ROOT).startsWith("ROLE_")) {
            role = role.substring(5);
        }
        return role.toUpperCase(Locale.ROOT);
    }
}
