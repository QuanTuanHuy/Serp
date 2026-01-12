/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member status enumeration
 */

package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;

/**
 * Represents the status of a member in a channel.
 */
@Getter
public enum MemberStatus {
    ACTIVE("ACTIVE", "Active"),
    MUTED("MUTED", "Muted"),
    LEFT("LEFT", "Left"),
    REMOVED("REMOVED", "Removed");

    private final String code;
    private final String displayName;

    MemberStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static MemberStatus fromCode(String code) {
        for (MemberStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown member status: " + code);
    }

    public boolean canAccessChannel() {
        return this == ACTIVE || this == MUTED;
    }

    public boolean receivesNotifications() {
        return this == ACTIVE;
    }

    public boolean isInactive() {
        return this == LEFT || this == REMOVED;
    }
}
