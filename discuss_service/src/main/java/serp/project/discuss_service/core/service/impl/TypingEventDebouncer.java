/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Debounces typing realtime events
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.port.client.ICachePort;
import serp.project.discuss_service.kernel.property.RealtimeProperties;

@Component
@RequiredArgsConstructor
public class TypingEventDebouncer {

    private static final String KEY_PREFIX = "discuss:typing:debounce:";

    private final ICachePort cachePort;
    private final RealtimeProperties realtimeProperties;

    public boolean shouldPublish(Long channelId, Long userId, boolean isTyping) {
        if (channelId == null || userId == null) {
            return false;
        }

        String key = KEY_PREFIX + channelId + ":" + userId + ":" + isTyping;
        if (cachePort.exists(key)) {
            return false;
        }

        long ttlSeconds = Math.max(1, realtimeProperties.getTyping().getDebounceMs() / 1000);
        cachePort.setToCache(key, "1", ttlSeconds);
        return true;
    }
}
