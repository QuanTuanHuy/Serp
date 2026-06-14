package serp.project.tms_payment_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_ENDPOINTS = {
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/v1/payments/zalopay/callback",  // ZaloPay callback phải public (no /api prefix)
            "/v1/payments/zalopay/create-order",
            "/v1/payments/*/callback",
            "/v1/payments/*/create-order"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/v1/payments/zalopay/health",   // Health check (no /api prefix)
            "/v1/payments/zalopay/banks",     // Lấy danh sách ngân hàng (no /api prefix)
            "/v1/payments/*/health",
            "/v1/payments/*/banks"
    };

    private static final String[] ACTUATOR_ENDPOINTS = {
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/actuator/info/**",
            "/actuator/prometheus",
            "/actuator/prometheus/**",
            "/actuator/metrics",
            "/actuator/metrics/**"
    };

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtDecoder jwtDecoder, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request ->
                        request
                                // Allow Swagger endpoints (highest priority)
                                .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                                // Allow actuator endpoints
                                .requestMatchers(ACTUATOR_ENDPOINTS).permitAll()
                                // Allow POST for ZaloPay callback
                                .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                                // Allow GET for public endpoints
                                .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                                // All other requests need authentication
                                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer ->
                                        jwtConfigurer.decoder(jwtDecoder)
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
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
