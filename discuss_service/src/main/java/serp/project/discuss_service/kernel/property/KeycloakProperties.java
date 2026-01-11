package serp.project.discuss_service.kernel.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.keycloak")
@Data
public class KeycloakProperties {
    private String url;
    private String jwkSetUri;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String expectedIssuer;
    private String expectedAudience;

    private Admin admin;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Admin {
        private String username;
        private String password;
        private String clientId;
    }
}
