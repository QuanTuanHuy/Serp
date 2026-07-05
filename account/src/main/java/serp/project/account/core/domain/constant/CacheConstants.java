package serp.project.account.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheConstants {
    public static final Long DEFAULT_EXPIRATION = 3600L; // 1 hour
    public static final Long SHORT_EXPIRATION = 300L; // 5 minutes
    public static final Long LONG_EXPIRATION = 86400L; // 24 hours

    public static final String ALL_ROLES = "account:roles:all";
    public static final String ALL_MODULES = "account:modules:all";
    public static final String PAGINATED_MODULES_PATTERN = "account:modules:paginated:*";
    public static final String PAGINATED_MODULES_KEY_FORMAT = "account:modules:paginated:%d:%d";
    public static final String MODULES_STATS = "account:modules:stats";
}
