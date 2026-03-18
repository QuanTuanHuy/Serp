/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Component
@ConfigurationProperties(prefix = "app.outbox")
@Getter
public class OutboxProperties {
    private int batchSize = 50;
    private long pollIntervalMs = 1000;
    private int retentionDays = 7;
    private String cleanupCron = "0 0 3 * * *";

}
