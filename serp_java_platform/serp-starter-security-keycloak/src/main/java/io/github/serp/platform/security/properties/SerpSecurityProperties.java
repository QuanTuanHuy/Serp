/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "serp.security")
public class SerpSecurityProperties {
    private Jwt jwt = new Jwt();
    private Service service = new Service();
    private List<RouteRule> publicUrls = new ArrayList<>();
    private List<ProtectedRouteRule> protectedUrls = new ArrayList<>();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public List<RouteRule> getPublicUrls() {
        return publicUrls;
    }

    public void setPublicUrls(List<RouteRule> publicUrls) {
        this.publicUrls = publicUrls;
    }

    public List<ProtectedRouteRule> getProtectedUrls() {
        return protectedUrls;
    }

    public void setProtectedUrls(List<ProtectedRouteRule> protectedUrls) {
        this.protectedUrls = protectedUrls;
    }

    public static class Jwt {
        private String jwkSetUri;
        private String principalClaimName = "sub";
        private String userIdClaim = "uid";
        private String tenantIdClaim = "tid";

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }

        public String getPrincipalClaimName() {
            return principalClaimName;
        }

        public void setPrincipalClaimName(String principalClaimName) {
            this.principalClaimName = principalClaimName;
        }

        public String getUserIdClaim() {
            return userIdClaim;
        }

        public void setUserIdClaim(String userIdClaim) {
            this.userIdClaim = userIdClaim;
        }

        public String getTenantIdClaim() {
            return tenantIdClaim;
        }

        public void setTenantIdClaim(String tenantIdClaim) {
            this.tenantIdClaim = tenantIdClaim;
        }
    }

    public static class Service {
        private String requiredRole = "SERP_SERVICES";
        private String azpClaim = "azp";
        private String clientIdClaim = "client_id";
        private Set<String> allowedClientIds = new HashSet<>();

        public String getRequiredRole() {
            return requiredRole;
        }

        public void setRequiredRole(String requiredRole) {
            this.requiredRole = requiredRole;
        }

        public String getAzpClaim() {
            return azpClaim;
        }

        public void setAzpClaim(String azpClaim) {
            this.azpClaim = azpClaim;
        }

        public String getClientIdClaim() {
            return clientIdClaim;
        }

        public void setClientIdClaim(String clientIdClaim) {
            this.clientIdClaim = clientIdClaim;
        }

        public Set<String> getAllowedClientIds() {
            return allowedClientIds;
        }

        public void setAllowedClientIds(Set<String> allowedClientIds) {
            this.allowedClientIds = allowedClientIds;
        }
    }

    public static class RouteRule {
        private String method;
        private String pattern;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }

    public static class ProtectedRouteRule {
        private String method;
        private String urlPattern;
        private List<String> roles = new ArrayList<>();
        private List<String> permissions = new ArrayList<>();

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getUrlPattern() {
            return urlPattern;
        }

        public void setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }
    }
}
