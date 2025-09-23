/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.crm.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheConstants {
    public static final Long DEFAULT_EXPIRATION = 3600L; // 1 hour
    public static final Long SHORT_EXPIRATION = 300L; // 5 minutes
    public static final Long LONG_EXPIRATION = 86400L; // 24 hours
}
