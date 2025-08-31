/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.config;

import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import serp.project.account.kernel.property.RequestFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final RequestFilter requestFilter;

    private final CustomJwtDecoder customJwtDecoder;

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

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                .decoder(customJwtDecoder)
                .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}