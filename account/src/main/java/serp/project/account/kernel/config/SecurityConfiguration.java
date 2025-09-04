/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.config;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.constant.KeyCloakConstants;
import serp.project.account.kernel.property.KeycloakProperties;
import serp.project.account.kernel.property.RequestFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final RequestFilter requestFilter;

    private final KeycloakProperties keycloakProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request -> {
            requestFilter.getPublicUrls().forEach(url -> request.requestMatchers(
                    HttpMethod.valueOf(url.getSecond()), url.getFirst())
                    .permitAll());

            requestFilter.getProtectedUrls().forEach(url -> {
                boolean hasRoles = url.getRoles() != null && !url.getRoles().isEmpty();
                boolean hasPermissions = url.getPermissions() != null && !url.getPermissions().isEmpty();

                if (hasRoles && hasPermissions) {
                    String[] allAuthorities = Stream.concat(
                            url.getRoles().stream(),
                            url.getPermissions().stream()).toArray(String[]::new);
                    request.requestMatchers(url.getUrlPattern())
                            .hasAnyAuthority(allAuthorities);
                } else if (hasRoles) {
                    request.requestMatchers(url.getUrlPattern())
                            .hasAnyRole(url.getRoles().toArray(new String[0]));
                } else if (hasPermissions) {
                    request.requestMatchers(url.getUrlPattern())
                            .hasAnyAuthority(url.getPermissions().toArray(new String[0]));
                }
            });

            request.anyRequest().authenticated();
        });

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
        );

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withJwkSetUri(keycloakProperties.getJwkSetUri())
                .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaimAsMap(KeyCloakConstants.REALM_ACCESS);
            if (realmAccess != null && realmAccess.containsKey(KeyCloakConstants.ROLES_ATTRIBUTE)) {
                Collection<String> realmRoles = (Collection<String>) realmAccess.get(KeyCloakConstants.ROLES_ATTRIBUTE);
                realmRoles.forEach(role -> authorities.add(new SimpleGrantedAuthority(Constants.Security.ROLE_PREFIX + role)));
            }

            Map<String, Object> resourceAccess = jwt.getClaimAsMap(KeyCloakConstants.RESOURCE_ACCESS);
            if (resourceAccess != null) {
                resourceAccess.values().forEach(resource -> {
                    if (resource instanceof Map) {
                        Map<String, Object> resourceMap = (Map<String, Object>) resource;
                        if (resourceMap.containsKey(KeyCloakConstants.ROLES_ATTRIBUTE)) {
                            Collection<String> resourceRoles = (Collection<String>) resourceMap.get(KeyCloakConstants.ROLES_ATTRIBUTE);
                            resourceRoles.forEach(role -> authorities.add(new SimpleGrantedAuthority(Constants.Security.ROLE_PREFIX + role)));
                        }
                    }
                });
            }

            return authorities;
        });
        return converter;
    }
    }