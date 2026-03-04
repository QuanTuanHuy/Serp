/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.http-client")
@Data
public class HttpClientProperties {
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 30000;

    private int idempotentMaxAttempts = 3;
    private int nonIdempotentRetryMaxAttempts = 2;

    private long initialBackoffMs = 300;
    private long maxBackoffMs = 3000;
    private double backoffMultiplier = 2.0;
    private double jitterFactor = 0.2;
}
