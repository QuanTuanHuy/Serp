/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.constant;

public final class NotificationKafkaConstants {
    private NotificationKafkaConstants() {
    }

    public static final String TOPIC = "serp.notification.user.events";
    public static final String SOURCE = "pm_core";
    public static final String VERSION = "1.0";

    public static final String EVENT_NOTIFICATION_CREATE_REQUESTED = "notification.create.requested";

    public static final String DEFAULT_CATEGORY = "PTM";
    public static final String DEFAULT_TYPE = "INFO";
    public static final String DEFAULT_PRIORITY = "MEDIUM";
    public static final String DEFAULT_DELIVERY_CHANNEL = "IN_APP";

    public static final String WORK_ITEM_CREATED_EVENT_KEY = "work_item.created";
    public static final String WORK_ITEM_UPDATED_EVENT_KEY = "work_item.updated";
    public static final String WORK_ITEM_ASSIGNED_EVENT_KEY = "work_item.assigned";
    public static final String WORK_ITEM_RESOLVED_EVENT_KEY = "work_item.resolved";
    public static final String WORK_ITEM_CLOSED_EVENT_KEY = "work_item.closed";
    public static final String WORK_ITEM_REOPENED_EVENT_KEY = "work_item.reopened";
}
