/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.security.autoconfigure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import io.github.serp.platform.security.context.DefaultSerpAuthContext;
import io.github.serp.platform.security.context.SerpAuthContext;
import io.github.serp.platform.security.properties.SerpSecurityProperties;
import io.github.serp.platform.security.support.KeycloakAuthoritiesExtractor;

@AutoConfiguration
@EnableConfigurationProperties(SerpSecurityProperties.class)
public class SerpSecurityAutoConfiguration {
    private static final String ROLE_PREFIX = "ROLE_";

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(SerpSecurityProperties properties) {
        String jwkSetUri = properties.getJwt().getJwkSetUri();
        Assert.hasText(jwkSetUri, "serp.security.jwt.jwk-set-uri is required");
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "userJwtAuthenticationConverter")
    public JwtAuthenticationConverter userJwtAuthenticationConverter(SerpSecurityProperties properties) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName(properties.getJwt().getPrincipalClaimName());
        converter.setJwtGrantedAuthoritiesConverter(KeycloakAuthoritiesExtractor::extractAuthorities);
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceJwtAuthenticationConverter")
    public JwtAuthenticationConverter serviceJwtAuthenticationConverter(SerpSecurityProperties properties) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName(properties.getService().getAzpClaim());
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            String azpValue = jwt.getClaimAsString(properties.getService().getAzpClaim());
            String clientIdValue = jwt.getClaimAsString(properties.getService().getClientIdClaim());

            if (isAllowedServiceClient(azpValue, properties) && isAllowedServiceClient(clientIdValue, properties)) {
                authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + properties.getService().getRequiredRole()));
            }

            return authorities;
        });
        return converter;
    }

    @Bean
    @Order(1)
    @ConditionalOnMissingBean(name = "internalApiFilterChain")
    public SecurityFilterChain internalApiFilterChain(
            HttpSecurity httpSecurity,
            JwtDecoder jwtDecoder,
            @Qualifier("serviceJwtAuthenticationConverter")
            JwtAuthenticationConverter serviceJwtAuthenticationConverter,
            SerpSecurityProperties properties) throws Exception {
        httpSecurity.securityMatcher("/internal/**")
                .authorizeHttpRequests(request -> request.requestMatchers("/internal/api/**")
                        .hasRole(properties.getService().getRequiredRole())
                        .anyRequest()
                        .authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(serviceJwtAuthenticationConverter)));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    @Order(2)
    @ConditionalOnMissingBean(name = "publicApiFilterChain")
    public SecurityFilterChain publicApiFilterChain(
            HttpSecurity httpSecurity,
            JwtDecoder jwtDecoder,
            @Qualifier("userJwtAuthenticationConverter")
            JwtAuthenticationConverter userJwtAuthenticationConverter,
            SerpSecurityProperties properties) throws Exception {
        httpSecurity.securityMatcher("/api/**")
                .authorizeHttpRequests(request -> {
                    if (properties.getPublicUrls() != null) {
                        properties.getPublicUrls().forEach(url -> {
                            String pattern = url.getPattern();
                            if (pattern == null || pattern.isBlank()) {
                                return;
                            }

                            HttpMethod method = resolveHttpMethod(url.getMethod());
                            if (method == null) {
                                request.requestMatchers(pattern).permitAll();
                                return;
                            }
                            request.requestMatchers(method, pattern).permitAll();
                        });
                    }

                    if (properties.getProtectedUrls() != null) {
                        properties.getProtectedUrls().forEach(url -> {
                            String urlPattern = url.getUrlPattern();
                            if (urlPattern == null || urlPattern.isBlank()) {
                                return;
                            }

                            HttpMethod method = resolveHttpMethod(url.getMethod());
                            List<String> roles = safeList(url.getRoles());
                            List<String> permissions = safeList(url.getPermissions());

                            boolean hasRoles = !roles.isEmpty();
                            boolean hasPermissions = !permissions.isEmpty();

                            if (hasRoles && hasPermissions) {
                                String[] allAuthorities = Stream.concat(
                                                roles.stream().map(this::toRoleAuthority),
                                                permissions.stream()
                                                        .filter(permission -> permission != null
                                                                && !permission.isBlank())
                                                        .map(String::trim))
                                        .filter(authority -> authority != null && !authority.isBlank())
                                        .distinct()
                                        .toArray(String[]::new);
                                if (allAuthorities.length == 0) {
                                    return;
                                }

                                if (method == null) {
                                    request.requestMatchers(urlPattern).hasAnyAuthority(allAuthorities);
                                    return;
                                }
                                request.requestMatchers(method, urlPattern).hasAnyAuthority(allAuthorities);
                                return;
                            }

                            if (hasRoles) {
                                String[] roleNames = roles.stream()
                                        .map(this::normalizeRoleName)
                                        .filter(role -> role != null && !role.isBlank())
                                        .toArray(String[]::new);
                                if (roleNames.length == 0) {
                                    return;
                                }

                                if (method == null) {
                                    request.requestMatchers(urlPattern).hasAnyRole(roleNames);
                                    return;
                                }
                                request.requestMatchers(method, urlPattern).hasAnyRole(roleNames);
                                return;
                            }

                            if (hasPermissions) {
                                String[] permissionNames = permissions.stream()
                                        .filter(permission -> permission != null && !permission.isBlank())
                                        .map(String::trim)
                                        .toArray(String[]::new);
                                if (permissionNames.length == 0) {
                                    return;
                                }

                                if (method == null) {
                                    request.requestMatchers(urlPattern).hasAnyAuthority(permissionNames);
                                    return;
                                }
                                request.requestMatchers(method, urlPattern).hasAnyAuthority(permissionNames);
                            }
                        });
                    }

                    request.anyRequest().authenticated();
                });

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(userJwtAuthenticationConverter)));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SerpAuthContext serpAuthContext(SerpSecurityProperties properties) {
        return new DefaultSerpAuthContext(properties);
    }

    private boolean isAllowedServiceClient(String clientId, SerpSecurityProperties properties) {
        if (clientId == null || clientId.isBlank()) {
            return false;
        }
        return properties.getService().getAllowedClientIds().contains(clientId);
    }

    private HttpMethod resolveHttpMethod(String method) {
        if (method == null || method.isBlank()) {
            return null;
        }

        try {
            return HttpMethod.valueOf(method.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private List<String> safeList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values;
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return "";
        }

        String normalized = roleName.trim();
        if (normalized.startsWith(ROLE_PREFIX)) {
            return normalized.substring(ROLE_PREFIX.length());
        }

        return normalized;
    }

    private String toRoleAuthority(String roleName) {
        String normalizedRole = normalizeRoleName(roleName);
        if (normalizedRole.isBlank()) {
            return "";
        }
        return ROLE_PREFIX + normalizedRole;
    }
}
