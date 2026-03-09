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
@ConfigurationProperties(prefix = "app.email.rate-limit")
public class RateLimitProperties {
    private Boolean enabled = true;
    private Integer defaultPerTenantPerMinute = 100;
    private Integer defaultPerProviderPerMinute = 500;
    private Integer defaultPerUserPerMinute = 20;
    private Boolean useRedis = true;
}
