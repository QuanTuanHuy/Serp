package serp.project.school_bus_service.shared.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Component("roleAuthorizer")
@Slf4j
public class RoleAuthorizer {

    private static final String PERMISSION_PREFIX = "permission.";

    private final Properties permissions;
    private final String rolePrefix;

    public RoleAuthorizer(@Value("${security.role-prefix:ROLE_}") String rolePrefix) {
        this.rolePrefix = rolePrefix == null ? "" : rolePrefix;
        this.permissions = loadPermissions();
    }

    public boolean hasPermission(String permissionKey) {
        Set<String> allowedRoles = resolveAllowedRoles(permissionKey);
        if (allowedRoles.isEmpty()) {
            log.warn("No roles configured for permission key '{}'", permissionKey);
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::normalizeRole)
                .collect(Collectors.toSet());

        return currentRoles.stream().anyMatch(allowedRoles::contains);
    }

    private Set<String> resolveAllowedRoles(String permissionKey) {
        String configuredRoles = permissions.getProperty(PERMISSION_PREFIX + permissionKey);
        if (configuredRoles == null || configuredRoles.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(configuredRoles.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(this::normalizeRole)
                .collect(Collectors.toSet());
    }

    private String normalizeRole(String role) {
        String normalizedRole = role == null ? "" : role.trim();
        if (!rolePrefix.isBlank() && normalizedRole.startsWith(rolePrefix)) {
            normalizedRole = normalizedRole.substring(rolePrefix.length());
        }
        return normalizedRole.toUpperCase(Locale.ROOT);
    }

    private Properties loadPermissions() {
        try {
            return PropertiesLoaderUtils.loadProperties(new ClassPathResource("role.properties"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load role.properties for school bus permissions", exception);
        }
    }
}
