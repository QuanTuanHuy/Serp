/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "serp.redis")
public class SerpRedisProperties {
    private boolean enabled = true;
    private Cache cache = new Cache();
    private Lock lock = new Lock();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    public static class Cache {
        private String prefix = "serp:cache";
        private long defaultTtlSeconds = 300;
        private String separator = ":";

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public long getDefaultTtlSeconds() {
            return defaultTtlSeconds;
        }

        public void setDefaultTtlSeconds(long defaultTtlSeconds) {
            this.defaultTtlSeconds = defaultTtlSeconds;
        }

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }
    }

    public static class Lock {
        private String prefix = "serp:lock";
        private long defaultTtlSeconds = 30;
        private String separator = ":";

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public long getDefaultTtlSeconds() {
            return defaultTtlSeconds;
        }

        public void setDefaultTtlSeconds(long defaultTtlSeconds) {
            this.defaultTtlSeconds = defaultTtlSeconds;
        }

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }
    }
}
