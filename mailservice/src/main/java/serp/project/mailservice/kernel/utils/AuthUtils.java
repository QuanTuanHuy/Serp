/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.kernel.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class AuthUtils {

    public Optional<Jwt> getCurrentJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                return Optional.of(jwt);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting current JWT", e);
            return Optional.empty();
        }
    }

    public Optional<Long> getCurrentUserId() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("uid"))
                .filter(sub -> !sub.isEmpty())
                .map(Long::valueOf);
    }

    public Optional<Long> getCurrentTenantId() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("tid"))
                .filter(tenant -> !tenant.isEmpty())
                .map(Long::valueOf);
    }

    public Optional<String> getCurrentUserEmail() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("email"));
    }

    public Optional<String> getCurrentUserFullName() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("name"));
    }

    @SuppressWarnings("unchecked")
    public List<String> getRealmRoles() {
        try {
            return getCurrentJwt()
                    .map(jwt -> {
                        Object realmAccess = jwt.getClaim("realm_access");
                        if (realmAccess instanceof Map) {
                            Map<String, Object> realmAccessMap = (Map<String, Object>) realmAccess;
                            Object roles = realmAccessMap.get("roles");
                            if (roles instanceof List) {
                                return new ArrayList<>((List<String>) roles);
                            }
                        }
                        return Collections.<String>emptyList();
                    })
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("Error extracting realm roles from current JWT", e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getClientRoles(String clientId) {
        try {
            return getCurrentJwt()
                    .map(jwt -> {
                        Object resourceAccess = jwt.getClaim("resource_access");
                        if (resourceAccess instanceof Map) {
                            Map<String, Object> resourceAccessMap = (Map<String, Object>) resourceAccess;
                            Object clientAccess = resourceAccessMap.get(clientId);
                            if (clientAccess instanceof Map) {
                                Map<String, Object> clientAccessMap = (Map<String, Object>) clientAccess;
                                Object roles = clientAccessMap.get("roles");
                                if (roles instanceof List) {
                                    return new ArrayList<>((List<String>) roles);
                                }
                            }
                        }
                        return Collections.<String>emptyList();
                    })
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("Error extracting client roles from current JWT for client: {}", clientId, e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllRoles() {
        try {
            return getCurrentJwt()
                    .map(jwt -> {
                        List<String> allRoles = new ArrayList<>();

                        // Get realm roles
                        Object realmAccess = jwt.getClaim("realm_access");
                        if (realmAccess instanceof Map) {
                            Map<String, Object> realmAccessMap = (Map<String, Object>) realmAccess;
                            Object realmRoles = realmAccessMap.get("roles");
                            if (realmRoles instanceof List) {
                                allRoles.addAll((List<String>) realmRoles);
                            }
                        }

                        // Get all client roles
                        Object resourceAccess = jwt.getClaim("resource_access");
                        if (resourceAccess instanceof Map) {
                            Map<String, Object> resourceAccessMap = (Map<String, Object>) resourceAccess;
                            for (Object clientAccess : resourceAccessMap.values()) {
                                if (clientAccess instanceof Map) {
                                    Map<String, Object> clientAccessMap = (Map<String, Object>) clientAccess;
                                    Object clientRoles = clientAccessMap.get("roles");
                                    if (clientRoles instanceof List) {
                                        allRoles.addAll((List<String>) clientRoles);
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
            log.error("Error extracting all roles from current JWT", e);
            return Collections.emptyList();
        }
    }

    public boolean hasRealmRole(String roleName) {
        List<String> realmRoles = getRealmRoles();
        return realmRoles.contains(roleName) || realmRoles.contains(roleName.toUpperCase());
    }

    public boolean hasClientRole(String clientId, String roleName) {
        List<String> clientRoles = getClientRoles(clientId);
        return clientRoles.contains(roleName) || clientRoles.contains(roleName.toUpperCase());
    }

    public boolean hasAnyRole(String... roleNames) {
        List<String> allRoles = getAllRoles();
        for (String roleName : roleNames) {
            if (allRoles.contains(roleName) || allRoles.contains(roleName.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSystemAdmin() {
        return hasAnyRole("SUPER_ADMIN", "SYSTEM_MODERATOR");
    }

    /**
     * Validate tenant access: Check if user's tenantId matches the organizationId
     * System admins bypass this check
     * 
     * @return true if user has access, false otherwise
     */
    public boolean canAccessOrganization(Long organizationId) {
        if (isSystemAdmin()) {
            return true;
        }

        Optional<Long> currentTenantId = getCurrentTenantId();
        if (currentTenantId.isEmpty()) {
            log.warn("User has no tenantId in JWT");
            return false;
        }

        boolean hasAccess = currentTenantId.get().equals(organizationId);
        if (!hasAccess) {
            log.warn("User tenantId {} does not match organizationId {}",
                    currentTenantId.get(), organizationId);
        }
        return hasAccess;
    }
}
