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
@ConfigurationProperties(prefix = "app.email.java-mail")
public class JavaMailProperties {
    private Boolean enabled = true;
    private String from;
    private String fromName;
    private String replyTo;
    private Integer rateLimitPerMinute = 60;
    private Integer timeoutSeconds = 10;
    private Integer maxRetryAttempts = 3;
    private Integer retryDelayMinutes = 5;
}
