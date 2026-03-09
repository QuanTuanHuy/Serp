/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel type enumeration
 */

package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;

/**
 * Represents the type of a channel in the discuss service.
 */
@Getter
public enum ChannelType {
    /**
     * Direct message between two users (1-on-1)
     */
    DIRECT("DIRECT", "Direct Message"),

    /**
     * Group channel for team discussions
     */
    GROUP("GROUP", "Group Channel"),

    /**
     * Topic channel linked to a business entity (Customer, Task, Order, etc.)
     */
    TOPIC("TOPIC", "Topic Channel");

    private final String code;
    private final String displayName;

    ChannelType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static ChannelType fromCode(String code) {
        for (ChannelType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown channel type: " + code);
    }

    /**
     * Check if this channel type requires entity linking
     */
    public boolean requiresEntity() {
        return this == TOPIC;
    }

    /**
     * Check if this channel type supports multiple members
     */
    public boolean supportsMultipleMembers() {
        return this != DIRECT;
    }
}
