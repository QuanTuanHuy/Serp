/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/
package serp.project.last_mile.kernel.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryRateLimiter {
    private final ConcurrentMap<String, CounterWindow> counters = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key, int permitsPerSecond) {
        long epochSecond = Instant.now().getEpochSecond();
        CounterWindow counterWindow = counters.computeIfAbsent(key, ignored -> new CounterWindow(epochSecond));

        synchronized (counterWindow) {
            if (counterWindow.epochSecond != epochSecond) {
                counterWindow.epochSecond = epochSecond;
                counterWindow.currentCount = 0;
            }

            if (counterWindow.currentCount >= permitsPerSecond) {
                return false;
            }

            counterWindow.currentCount++;
            return true;
        }
    }

    private static final class CounterWindow {
        private long epochSecond;
        private int currentCount;

        private CounterWindow(long epochSecond) {
            this.epochSecond = epochSecond;
        }
    }
}
