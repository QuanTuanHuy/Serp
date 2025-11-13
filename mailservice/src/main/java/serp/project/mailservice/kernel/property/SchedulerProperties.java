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
@ConfigurationProperties(prefix = "app.email.scheduler")
public class SchedulerProperties {
    private String retryFailedEmailsCron = "0 */5 * * * *"; // Every 5 minutes
    private String aggregateStatsCron = "0 0 * * * *";      // Every hour
    private String cleanupAttachmentsCron = "0 0 2 * * *";  // 2 AM daily
    private Long providerHealthCheckFixedDelay = 60000L;    // 60 seconds
}
