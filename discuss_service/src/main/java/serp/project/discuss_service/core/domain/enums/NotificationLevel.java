/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Notification level enumeration
 */

package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;

/**
 * Represents the notification preference for a channel member.
 */
@Getter
public enum NotificationLevel {
    ALL("ALL", "All Messages"),
    MENTIONS("MENTIONS", "Mentions Only"),
    NONE("NONE", "None");

    private final String code;
    private final String displayName;

    NotificationLevel(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static NotificationLevel fromCode(String code) {
        for (NotificationLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown notification level: " + code);
    }

    public boolean shouldNotifyForMessage() {
        return this == ALL;
    }

    public boolean shouldNotifyForMention() {
        return this == ALL || this == MENTIONS;
    }
}
