/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.kernel.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.email.brevo")
public class BrevoProperties {
    private Boolean enabled = true;
    private String apiKey;
    private String apiUrl = "https://api.brevo.com/v3";
    private String from;
    private String fromName;
    private Integer rateLimitPerMinute = 100;
    private Integer timeoutSeconds = 10;
    private Integer maxRetryAttempts = 3;
    private Integer retryDelayMinutes = 5;
}
