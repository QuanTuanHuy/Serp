/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.pmcore.kernel.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.outbox")
@Getter
@Setter
public class OutboxProperties {
    private int batchSize = 50;
    private long pollIntervalMs = 1000;
    private int retentionDays = 7;
    private String cleanupCron = "0 0 3 * * *";
}