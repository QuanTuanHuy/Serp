package serp.project.tms_payment_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Configuration
public class CustomJwtDecoder {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    /**
     * Legacy fallback (HS512) for non-Keycloak environments.
     * Prefer Keycloak via `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`.
     */
    @Value("${jwt.signerKey:}")
    private String signerKey;

    private NimbusJwtDecoder cachedDecoder = null;

    @Bean
    public JwtDecoder jwtDecoder() {
        if (Objects.nonNull(cachedDecoder)) {
            return cachedDecoder;
        }

        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            cachedDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
            return cachedDecoder;
        }

        if (signerKey != null && !signerKey.isBlank()) {
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(signerKey.getBytes(StandardCharsets.UTF_8), "HS512");
            cachedDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
            return cachedDecoder;
        }

        throw new IllegalStateException(
                "No JwtDecoder configuration found. Set KEYCLOAK_URL (preferred) or jwt.signerKey (legacy)."
        );
    }

    /**
     * Kept only for backward compatibility with older wiring.
     * New configuration should rely on the `JwtDecoder` bean above.
     */
    @Deprecated
    public Jwt decode(String token) {
        return jwtDecoder().decode(token);
    }
}
