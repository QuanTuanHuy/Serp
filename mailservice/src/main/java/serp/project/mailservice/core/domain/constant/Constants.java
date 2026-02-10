package serp.project.mailservice.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String SERVICE_NAME = "mail-service";

    @UtilityClass
    public static class Security {
        public static final String ROLE_PREFIX = "ROLE_";
        public static final String SERP_SERVICES_ROLE = "SERP_SERVICES";
    }
}