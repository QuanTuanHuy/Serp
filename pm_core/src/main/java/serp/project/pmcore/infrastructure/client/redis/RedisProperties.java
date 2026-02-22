package serp.project.pmcore.infrastructure.client.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
public class RedisProperties {
    private boolean enabled = true;
    private Cache cache = new Cache();
    private Lock lock = new Lock();

    @Setter
    @Getter
    public static class Cache {
        private String prefix = "serp:cache";
        private long defaultTtlSeconds = 300;
        private String separator = ":";

    }

    @Setter
    @Getter
    public static class Lock {
        private String prefix = "serp:lock";
        private long defaultTtlSeconds = 30;
        private String separator = ":";

    }
}
