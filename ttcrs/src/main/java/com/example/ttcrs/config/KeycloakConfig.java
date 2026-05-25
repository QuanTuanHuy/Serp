package com.example.ttcrs.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("classpath:keycloak.properties")
public class KeycloakConfig {

    @Value("${keycloak.url}")
    private String url;

    @Value("${keycloak.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.expected-issuer}")
    private String expectedIssuer;

    @Value("${keycloak.expected-audience}")
    private String expectedAudience;

    @Value("${keycloak.constant.realm-access}")
    private String realmAccess;

    @Value("${keycloak.constant.resource-access}")
    private String resourceAccess;

    @Value("${keycloak.constant.roles-attribute}")
    private String rolesAttribute;
}
