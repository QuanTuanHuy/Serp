/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.security.support;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class KeycloakAuthoritiesExtractor {
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String REALM_ACCESS = "realm_access";
    private static final String RESOURCE_ACCESS = "resource_access";
    private static final String ROLES = "roles";

    private KeycloakAuthoritiesExtractor() {
    }

    @SuppressWarnings("unchecked")
    public static Set<String> extractRoleNames(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();

        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);
        if (realmAccess != null && realmAccess.containsKey(ROLES)) {
            Collection<String> realmRoles = (Collection<String>) realmAccess.get(ROLES);
            roles.addAll(realmRoles);
        }

        Map<String, Object> resourceAccess = jwt.getClaimAsMap(RESOURCE_ACCESS);
        if (resourceAccess != null) {
            resourceAccess.values().forEach(resource -> {
                if (resource instanceof Map<?, ?> resourceMap && resourceMap.containsKey(ROLES)) {
                    Collection<String> resourceRoles = (Collection<String>) resourceMap.get(ROLES);
                    roles.addAll(resourceRoles);
                }
            });
        }

        return roles;
    }

    public static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        extractRoleNames(jwt).forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));
        return authorities;
    }
}
