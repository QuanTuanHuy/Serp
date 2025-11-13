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
@ConfigurationProperties(prefix = "app.email.retry")
public class RetryProperties {
    private Boolean enabled = true;
    private Integer maxAttempts = 3;
    private Integer initialDelayMinutes = 1;
    private Integer maxDelayMinutes = 60;
    private Double backoffMultiplier = 2.0;
}
