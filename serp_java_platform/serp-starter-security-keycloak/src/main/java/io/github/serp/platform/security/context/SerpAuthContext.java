/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.security.context;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.Set;

public interface SerpAuthContext {
    Optional<Jwt> getCurrentJwt();

    Optional<Long> getCurrentUserId();

    Optional<Long> getCurrentTenantId();

    Optional<String> getCurrentEmail();

    Set<String> getAllRoles();

    boolean hasAnyRole(String... roleNames);
}
