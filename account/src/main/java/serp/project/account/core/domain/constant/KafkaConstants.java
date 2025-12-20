package serp.project.account.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaConstants {
    public static final String DEFAULT_VERSION = "1.0";
    

    public static class Notification {
        public static final String USER_NOTIFICATION_TOPIC = "serp.notification.user.events";
        public static final String EVENT_CREATE_REQUESTED = "notification.create.requested";
    }
}
