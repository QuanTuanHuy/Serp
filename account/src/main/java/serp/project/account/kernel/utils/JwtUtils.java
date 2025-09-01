/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.entity.PermissionEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.kernel.property.JwtProperties;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {
    private final JwtProperties jwtProperties;
    
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey(jwtProperties.getPrivateKey());
            this.publicKey = loadPublicKey(jwtProperties.getPublicKey());
            log.info("JWT keys loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load JWT keys", e);
            throw new RuntimeException("Failed to initialize JWT keys", e);
        }
    }

    public String generateAccessTokenFromUser(UserEntity user) {
        return generateAccessTokenFromUser(user, null);
    }

    public String generateAccessTokenFromUser(UserEntity user, Map<String, Object> additionalClaims) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

            // Extract roles and permissions
            List<String> roles = extractRoles(user);
            List<String> permissions = extractPermissions(user);
            List<String> authorities = buildAuthorities(roles, permissions);

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .claim("email", user.getEmail())
                    .claim("full_name", user.getFullName())
                    .claim("type", Constants.TokenType.ACCESS_TOKEN)
                    .claim("user_id", user.getId())
                    .issuer("serp-account-service")
                    .audience("serp-services")
                    .issueTime(now)
                    .expirationTime(expiration)
                    .claim("scope", String.join(" ", authorities))
            ;

            // Add custom claims
            if (additionalClaims != null && !additionalClaims.isEmpty()) {
                additionalClaims.forEach(claimsBuilder::claim);
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                    claimsSet
            );

            signedJWT.sign(new RSASSASigner(privateKey));
            return signedJWT.serialize();

        } catch (Exception e) {
            log.error("Error generating access token from user", e);
            throw new RuntimeException("Failed to generate access token from user", e);
        }
    }


    public String generateRefreshTokenFromUser(UserEntity user) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .claim("email", user.getEmail())
                    .claim("type", Constants.TokenType.REFRESH_TOKEN)
                    .claim("user_id", user.getId())
                    .issuer(Constants.SERVICE_NAME)
                    .audience("serp-services")
                    .issueTime(now)
                    .expirationTime(expiration)
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                    claimsSet
            );

            signedJWT.sign(new RSASSASigner(privateKey));
            return signedJWT.serialize();

        } catch (Exception e) {
            log.error("Error generating refresh token from user", e);
            throw new RuntimeException("Failed to generate refresh token from user", e);
        }
    }

    private List<String> extractRoles(UserEntity user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.emptyList();
        }
        
        return user.getRoles().stream()
                .map(RoleEntity::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> extractPermissions(UserEntity user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.emptyList();
        }

        return user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> buildAuthorities(List<String> roles, List<String> permissions) {
        List<String> authorities = new ArrayList<>();

        roles.stream()
                .map(role -> "ROLE_" + role.toUpperCase())
                .forEach(authorities::add);

        authorities.addAll(permissions);
        
        return authorities;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            Object scope = claimsSet.getClaim("scope");
            if (scope instanceof List) {
                return ((List<String>) scope).stream()
                        .filter(role -> role.startsWith("ROLE_"))
                        .map(role -> role.substring(5))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error extracting roles from token", e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            Object scope = claimsSet.getClaim("scope");
            if (scope instanceof List) {
                return ((List<String>) scope).stream()
                        .filter(permission -> !permission.startsWith("ROLE_"))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error extracting permissions from token", e);
            return Collections.emptyList();
        }
    }

    public boolean hasRole(String token, String roleName) {
        List<String> roles = getRolesFromToken(token);
        return roles.contains(roleName) || roles.contains(roleName.toUpperCase());
    }

    public boolean hasPermission(String token, String permissionName) {
        List<String> permissions = getPermissionsFromToken(token);
        return permissions.contains(permissionName);
    }


    public JWTClaimsSet validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            // Verify signature
            RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Invalid JWT signature");
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            
            if (claimsSet.getExpirationTime() != null && claimsSet.getExpirationTime().before(new Date())) {
                throw new RuntimeException("JWT token has expired");
            }

            return claimsSet;

        } catch (Exception e) {
            log.error("Error validating JWT token", e);
            throw new RuntimeException("Invalid JWT token", e);
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
            log.error("Error extracting claim {} from token", claimName, e);
            return null;
        }
    }

    public RSAPrivateKey loadPrivateKey(String privateKeyString) throws Exception {
        String cleanPrivateKey = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleanPrivateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }

    public RSAPublicKey loadPublicKey(String publicKeyString) throws Exception {
        String cleanPublicKey = publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleanPublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }

    public Optional<String> getTokenType(String token) {
        try {
            String type = (String) extractClaim(token, "type");
            return Optional.ofNullable(type);
        } catch (Exception e) {
            log.error("Error extracting token type from token", e);
            return Optional.empty();
        }
    }

    public boolean isAccessToken(String token) {
        return getTokenType(token)
                .map(Constants.TokenType.ACCESS_TOKEN::equals)
                .orElse(false);
    }

    public boolean isRefreshToken(String token) {
        return getTokenType(token)
                .map(Constants.TokenType.REFRESH_TOKEN::equals)
                .orElse(false);
    }
}
