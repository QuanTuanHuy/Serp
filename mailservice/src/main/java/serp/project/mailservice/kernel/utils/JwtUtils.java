/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.kernel.utils;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.mailservice.kernel.property.KeycloakProperties;

import org.springframework.stereotype.Component;


import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {
    private final KeycloakJwksUtils keycloakJwksUtils;
    private final KeycloakProperties keycloakProperties;

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            List<String> roles = new ArrayList<>();

            Object realmAccess = claimsSet.getClaim("realm_access");
            if (realmAccess instanceof Map) {
                Map<String, Object> realmAccessMap = (Map<String, Object>) realmAccess;
                Object realmRoles = realmAccessMap.get("roles");
                if (realmRoles instanceof List) {
                    roles.addAll((List<String>) realmRoles);
                }
            }

            Object resourceAccess = claimsSet.getClaim("resource_access");
            if (resourceAccess instanceof Map) {
                Map<String, Object> resourceAccessMap = (Map<String, Object>) resourceAccess;
                for (Object clientAccess : resourceAccessMap.values()) {
                    if (clientAccess instanceof Map) {
                        Map<String, Object> clientAccessMap = (Map<String, Object>) clientAccess;
                        Object clientRoles = clientAccessMap.get("roles");
                        if (clientRoles instanceof List) {
                            roles.addAll((List<String>) clientRoles);
                        }
                    }
                }
            }

            return roles.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error extracting roles from token", e);
            return Collections.emptyList();
        }
    }

    public boolean hasRole(String token, String roleName) {
        List<String> roles = getRolesFromToken(token);
        return roles.contains(roleName) || roles.contains(roleName.toUpperCase());
    }

    public Long getUserIdFromToken(String token) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);

            Object userIdClaim = claimsSet.getClaim("uid");
            if (userIdClaim instanceof Number) {
                return ((Number) userIdClaim).longValue();
            }
            if (userIdClaim instanceof String) {
                try {
                    return Long.parseLong((String) userIdClaim);
                } catch (NumberFormatException ignored) {
                    log.warn("uid claim is not a valid number: {}", userIdClaim);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting user ID from token", e);
            return null;
        }
    }

    public String getSubjectFromToken(String token) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            return claimsSet.getSubject();
        } catch (Exception e) {
            log.error("Error extracting subject from token", e);
            return null;
        }
    }

    public boolean isBearerToken(String token) {
        try {
            String typ = (String) extractClaim(token, "typ");
            return "Bearer".equals(typ);
        } catch (Exception e) {
            log.error("Error extracting token type from token", e);
            return false;
        }
    }

    public JWTClaimsSet validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            if (claimsSet.getExpirationTime() != null && claimsSet.getExpirationTime().before(new Date())) {
                throw new RuntimeException("JWT token has expired");
            }

            if (claimsSet.getIssueTime() != null && claimsSet.getIssueTime().after(new Date())) {
                throw new RuntimeException("JWT token not yet valid");
            }

            String issuer = claimsSet.getIssuer();
            if (issuer == null || issuer.trim().isEmpty()) {
                throw new RuntimeException("JWT token missing issuer");
            }

            if (keycloakProperties.getExpectedIssuer() != null &&
                    !keycloakProperties.getExpectedIssuer().equals(issuer)) {
                throw new RuntimeException("JWT token issuer mismatch. Expected: " +
                        keycloakProperties.getExpectedIssuer() + ", Actual: " + issuer);
            }

            List<String> audiences = claimsSet.getAudience();
            if (audiences == null || audiences.isEmpty()) {
                log.warn("JWT token has no audience claim");
            } else if (keycloakProperties.getExpectedAudience() != null &&
                    !audiences.contains(keycloakProperties.getExpectedAudience())) {
                throw new RuntimeException("JWT token audience mismatch. Expected: " +
                        keycloakProperties.getExpectedAudience() + ", Actual: " + audiences);
            }

            return validateKeycloakToken(signedJWT, claimsSet);

        } catch (Exception e) {
            log.error("Error validating JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private JWTClaimsSet validateKeycloakToken(SignedJWT signedJWT, JWTClaimsSet claimsSet) {
        try {
            String keyId = signedJWT.getHeader().getKeyID();
            if (keyId == null) {
                log.warn("No key ID found in JWT header, skipping signature verification");
                return claimsSet;
            }

            RSAPublicKey keycloakPublicKey = keycloakJwksUtils.getPublicKey(keyId);
            if (keycloakPublicKey == null) {
                log.warn("Could not find public key for key ID: {}, skipping signature verification", keyId);
                return claimsSet;
            }

            RSASSAVerifier verifier = new RSASSAVerifier(keycloakPublicKey);
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Invalid Keycloak JWT signature");
            }

            log.debug("Successfully verified Keycloak token signature with key ID: {}", keyId);
            return claimsSet;

        } catch (Exception e) {
            log.error("Error validating Keycloak token", e);
            throw new RuntimeException("Invalid Keycloak token", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationDate != null && expirationDate.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration", e);
            return true;
        }
    }

    public JWTClaimsSet getClaimsFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            log.error("Error parsing claims from token", e);
            return null;
        }
    }

    public Object extractClaim(String token, String claimName) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            return claimsSet.getClaim(claimName);
        } catch (Exception e) {
            log.debug("Failed to validate token, trying without signature validation", e);
            // Fallback to extraction without signature validation
            return extractClaimWithoutValidation(token, claimName);
        }
    }

    public String getEmailFromToken(String token) {
        try {
            return (String) extractClaim(token, "email");
        } catch (Exception e) {
            log.error("Error extracting email from token", e);
            return null;
        }
    }

    public String getPreferredUsernameFromToken(String token) {
        try {
            return (String) extractClaim(token, "preferred_username");
        } catch (Exception e) {
            log.error("Error extracting preferred_username from token", e);
            return null;
        }
    }

    public String getFullNameFromToken(String token) {
        try {
            return (String) extractClaim(token, "name");
        } catch (Exception e) {
            log.error("Error extracting name from token", e);
            return null;
        }
    }

    public boolean isEmailVerifiedFromToken(String token) {
        try {
            Object emailVerified = extractClaim(token, "email_verified");
            if (emailVerified instanceof Boolean) {
                return (Boolean) emailVerified;
            }
            return false;
        } catch (Exception e) {
            log.error("Error extracting email_verified from token", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromResourceAccess(String token, String clientId) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            Object resourceAccess = claimsSet.getClaim("resource_access");
            if (resourceAccess instanceof Map) {
                Map<String, Object> resourceAccessMap = (Map<String, Object>) resourceAccess;
                Object clientAccess = resourceAccessMap.get(clientId);
                if (clientAccess instanceof Map) {
                    Map<String, Object> clientAccessMap = (Map<String, Object>) clientAccess;
                    Object roles = clientAccessMap.get("roles");
                    if (roles instanceof List) {
                        return new ArrayList<>((List<String>) roles);
                    }
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error extracting roles from resource_access for client: {}", clientId, e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRealmRolesFromToken(String token) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            Object realmAccess = claimsSet.getClaim("realm_access");
            if (realmAccess instanceof Map) {
                Map<String, Object> realmAccessMap = (Map<String, Object>) realmAccess;
                Object roles = realmAccessMap.get("roles");
                if (roles instanceof List) {
                    return new ArrayList<>((List<String>) roles);
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error extracting realm roles from token", e);
            return Collections.emptyList();
        }
    }

    public boolean hasRealmRole(String token, String roleName) {
        List<String> realmRoles = getRealmRolesFromToken(token);
        return realmRoles.contains(roleName);
    }

    public boolean hasResourceRole(String token, String clientId, String roleName) {
        List<String> resourceRoles = getRolesFromResourceAccess(token, clientId);
        return resourceRoles.contains(roleName);
    }

    /**
     * Validates token without signature verification (for development/testing)
     */
    public JWTClaimsSet validateTokenWithoutSignature(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            if (claimsSet.getExpirationTime() != null && claimsSet.getExpirationTime().before(new Date())) {
                throw new RuntimeException("JWT token has expired");
            }

            if (claimsSet.getIssueTime() != null && claimsSet.getIssueTime().after(new Date())) {
                throw new RuntimeException("JWT token not yet valid");
            }

            return claimsSet;

        } catch (Exception e) {
            log.error("Error parsing JWT token", e);
            throw new RuntimeException("Invalid JWT token format", e);
        }
    }

    public Object extractClaimWithoutValidation(String token, String claimName) {
        try {
            JWTClaimsSet claimsSet = validateTokenWithoutSignature(token);
            return claimsSet.getClaim(claimName);
        } catch (Exception e) {
            log.error("Error extracting claim {} from token without validation", claimName, e);
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateIssuer(String token, String expectedIssuer) {
        try {
            JWTClaimsSet claimsSet = getClaimsFromToken(token);
            if (claimsSet == null)
                return false;

            String issuer = claimsSet.getIssuer();
            return expectedIssuer.equals(issuer);
        } catch (Exception e) {
            log.error("Error validating issuer", e);
            return false;
        }
    }

    public boolean validateAudience(String token, String expectedAudience) {
        try {
            JWTClaimsSet claimsSet = getClaimsFromToken(token);
            if (claimsSet == null)
                return false;

            List<String> audiences = claimsSet.getAudience();
            return audiences != null && audiences.contains(expectedAudience);
        } catch (Exception e) {
            log.error("Error validating audience", e);
            return false;
        }
    }


    public String getAuthorizedPartyFromToken(String token) {
        try {
            return (String) extractClaim(token, "azp");
        } catch (Exception e) {
            log.error("Error extracting azp from token", e);
            return null;
        }
    }

    public String getSessionIdFromToken(String token) {
        try {
            return (String) extractClaim(token, "sid");
        } catch (Exception e) {
            log.error("Error extracting sid from token", e);
            return null;
        }
    }
}
