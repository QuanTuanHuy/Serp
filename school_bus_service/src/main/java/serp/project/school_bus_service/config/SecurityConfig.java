package serp.project.school_bus_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${security.roles.serp_service}")
    private String serpServiceRole;

    @Value("${security.role-prefix}")
    private String rolePrefix;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    @Order(1)
    public SecurityFilterChain internalApiFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.securityMatcher("/internal/**")
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/internal/api/**").hasRole(serpServiceRole)
                        .anyRequest().authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(serviceJwtAuthenticationConverter())));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain publicApiFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.securityMatcher("/school-bus/api/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/school-bus/api/v1/**")
                        .hasAnyRole(
                                "SCHOOL_BUS_ADMIN",
                                "SCHOOL_BUS_DISPATCHER",
                                "SCHOOL_BUS_DRIVER",
                                "SCHOOL_BUS_ATTENDANT",
                                "SCHOOL_BUS_PARENT")
                        .anyRequest()
                        .authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");
                realmRoles.forEach(role -> authorities.add(new SimpleGrantedAuthority(rolePrefix + role)));
            }

            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                resourceAccess.values().forEach(resource -> {
                    if (resource instanceof Map<?, ?> resourceMap && resourceMap.containsKey("roles")) {
                        @SuppressWarnings("unchecked")
                        Collection<String> resourceRoles = (Collection<String>) resourceMap.get("roles");
                        resourceRoles.forEach(role -> authorities.add(new SimpleGrantedAuthority(rolePrefix + role)));
                    }
                });
            }

            return authorities;
        });
        return converter;
    }

    @Bean
    public JwtAuthenticationConverter serviceJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            String azp = jwt.getClaimAsString("azp");
            String clientId = jwt.getClaimAsString("client_id");

            if (isValidServiceClient(azp) && isValidServiceClient(clientId)) {
                authorities.add(new SimpleGrantedAuthority(rolePrefix + serpServiceRole));
            }

            return authorities;
        });
        converter.setPrincipalClaimName("azp");
        return converter;
    }

    private boolean isValidServiceClient(String clientId) {
        return clientId != null && !clientId.isBlank();
    }
}
