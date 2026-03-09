/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.security.context;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import io.github.serp.platform.security.properties.SerpSecurityProperties;
import io.github.serp.platform.security.support.KeycloakAuthoritiesExtractor;

public class DefaultSerpAuthContext implements SerpAuthContext {
    private static final String EMAIL_CLAIM = "email";

    private final SerpSecurityProperties properties;

    public DefaultSerpAuthContext(SerpSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<Jwt> getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Long> getCurrentUserId() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString(properties.getJwt().getUserIdClaim()))
                .flatMap(this::parseLongSafely);
    }

    @Override
    public Optional<Long> getCurrentTenantId() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString(properties.getJwt().getTenantIdClaim()))
                .flatMap(this::parseLongSafely);
    }

    @Override
    public Optional<String> getCurrentEmail() {
        return getCurrentJwt().map(jwt -> jwt.getClaimAsString(EMAIL_CLAIM));
    }

    @Override
    public Set<String> getAllRoles() {
        return getCurrentJwt()
                .map(KeycloakAuthoritiesExtractor::extractRoleNames)
                .map(LinkedHashSet::new)
                .orElseGet(LinkedHashSet::new);
    }

    @Override
    public boolean hasAnyRole(String... roleNames) {
        Set<String> allRoles = getAllRoles();
        return Arrays.stream(roleNames)
                .filter(role -> role != null && !role.isBlank())
                .map(String::trim)
                .anyMatch(role -> allRoles.contains(role) || allRoles.contains(role.toUpperCase()));
    }

    private Optional<Long> parseLongSafely(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.valueOf(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
