/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member role enumeration
 */

package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;

/**
 * Represents the role of a member in a channel.
 */
@Getter
public enum MemberRole {
    OWNER("OWNER", "Owner", 100),
    ADMIN("ADMIN", "Admin", 80),
    MEMBER("MEMBER", "Member", 50),
    GUEST("GUEST", "Guest", 10);

    private final String code;
    private final String displayName;
    private final int priority;

    MemberRole(String code, String displayName, int priority) {
        this.code = code;
        this.displayName = displayName;
        this.priority = priority;
    }

    public static MemberRole fromCode(String code) {
        for (MemberRole role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown member role: " + code);
    }

    public boolean canManageChannel() {
        return this == OWNER || this == ADMIN;
    }

    public boolean canManageMembers() {
        return this == OWNER || this == ADMIN;
    }

    public boolean canSendMessages() {
        return this != GUEST;
    }

    public boolean canDeleteOthersMessages() {
        return this == OWNER || this == ADMIN;
    }

    public boolean hasHigherPriorityThan(MemberRole other) {
        return this.priority > other.priority;
    }
}
