/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.kernel.property;

import lombok.Data;
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
}
