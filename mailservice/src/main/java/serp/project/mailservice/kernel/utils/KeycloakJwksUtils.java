/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.kernel.utils;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.mailservice.kernel.property.KeycloakProperties;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakJwksUtils {
    
    private final KeycloakProperties keycloakProperties;
    
    private final ConcurrentHashMap<String, RSAPublicKey> keyCache = new ConcurrentHashMap<>();
    private volatile JWKSet jwkSet;
    private volatile long lastFetchTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    private final Object fetchLock = new Object();

    public RSAPublicKey getPublicKey(String keyId) {
        try {
            RSAPublicKey cachedKey = keyCache.get(keyId);
            if (cachedKey != null) {
                return cachedKey;
            }

            if (jwkSet == null || isCacheExpired()) {
                synchronized (fetchLock) {
                    if (jwkSet == null || isCacheExpired()) {
                        fetchJwkSet();
                    }
                }
            }

            JWK jwk = jwkSet.getKeyByKeyId(keyId);
            if (jwk instanceof RSAKey rsaKey) {
                RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
                
                keyCache.put(keyId, publicKey);
                return publicKey;
            }

            return null;

        } catch (Exception e) {
            log.error("Error fetching public key for key ID: {}", keyId, e);
            return null;
        }
    }

    private void fetchJwkSet() {
        try {
            jwkSet = JWKSet.load(URI.create(keycloakProperties.getJwkSetUri()).toURL());
            lastFetchTime = System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Failed to fetch JWK Set from: {}", keycloakProperties.getJwkSetUri(), e);
            throw new RuntimeException("Failed to fetch JWK Set", e);
        }
    }

    public void clearCache() {
        keyCache.clear();
        synchronized (fetchLock) {
            jwkSet = null;
            lastFetchTime = 0;
        }
    }

    public boolean isCacheExpired() {
        return System.currentTimeMillis() - lastFetchTime > CACHE_DURATION;
    }
}
