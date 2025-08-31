/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import serp.project.account.kernel.property.JwtProperties;
import serp.project.account.kernel.utils.JwtUtils;

import java.security.interfaces.RSAPublicKey;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtProperties jwtProperties;
    private final JwtUtils jwtUtils;
    private NimbusJwtDecoder nimbusJwtDecoder;

    @PostConstruct
    public void init() {
        try {
            RSAPublicKey publicKey = jwtUtils.loadPublicKey(jwtProperties.getPublicKey());
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withPublicKey(publicKey)
                    .signatureAlgorithm(SignatureAlgorithm.RS256)
                    .build();
            log.info("CustomJwtDecoder initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize CustomJwtDecoder", e);
            throw new RuntimeException("Failed to initialize JWT decoder", e);
        }
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            jwtUtils.validateToken(token);

            if (!jwtUtils.isAccessToken(token)) {
                throw new JwtException("Not an access token");
            }

            return nimbusJwtDecoder.decode(token);

        } catch (Exception e) {
            log.error("Error decoding JWT token", e);
            throw new JwtException("Invalid JWT token", e);
        }
    }
}