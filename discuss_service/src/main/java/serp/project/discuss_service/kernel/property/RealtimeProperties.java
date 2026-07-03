/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Realtime delivery properties
 */

package serp.project.discuss_service.kernel.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "discuss.realtime")
public class RealtimeProperties {

    private Delivery delivery = new Delivery();
    private Typing typing = new Typing();
    private long payloadCacheTtlSeconds = 300;

    @Getter
    @Setter
    public static class Delivery {
        private int maxConcurrency = 64;
    }

    @Getter
    @Setter
    public static class Typing {
        private long debounceMs = 2000;
    }
}
