/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.constant;

public class RedisKey {
    // Rate limiting keys
    public static final String RATE_LIMIT_TENANT_PREFIX = "serp_mail:rate_limit:tenant:";
    public static final String RATE_LIMIT_PROVIDER_PREFIX = "serp_mail:rate_limit:provider:";
    public static final String RATE_LIMIT_USER_PREFIX = "serp_mail:rate_limit:user:";

    // Provider health status
    public static final String PROVIDER_HEALTH_PREFIX = "serp_mail:provider:health:";
    public static final String PROVIDER_CIRCUIT_BREAKER_PREFIX = "serp_mail:provider:circuit_breaker:";
    // Template cache
    public static final String TEMPLATE_CACHE_PREFIX = "serp_mail:template:";
    public static final String TEMPLATE_CODE_INDEX_PREFIX = "serp_mail:template:code:";

    // Email queue
    public static final String EMAIL_QUEUE_PREFIX = "serp_mail:email:queue:";
    public static final String EMAIL_RETRY_QUEUE = "serp_mail:email:retry_queue";
    // Locks
    public static final String SEND_EMAIL_LOCK_PREFIX = "serp_mail:lock:send:";
    public static final String STATS_AGGREGATION_LOCK = "serp_mail:lock:stats_aggregation";

    private RedisKey() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
