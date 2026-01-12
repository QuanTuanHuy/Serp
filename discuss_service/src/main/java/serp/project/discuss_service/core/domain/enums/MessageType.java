/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message type enumeration
 */

package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;

/**
 * Represents the type of a message in the discuss service.
 */
@Getter
public enum MessageType {
    TEXT("TEXT", "Text Message"),
    IMAGE("IMAGE", "Image"),
    FILE("FILE", "File"),
    SYSTEM("SYSTEM", "System Message"),
    CODE("CODE", "Code Snippet"),
    POLL("POLL", "Poll");

    private final String code;
    private final String displayName;

    MessageType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static MessageType fromCode(String code) {
        for (MessageType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + code);
    }

    public boolean requiresContent() {
        return this == TEXT || this == CODE;
    }

    public boolean canHaveAttachments() {
        return this == IMAGE || this == FILE;
    }

    public boolean isUserGenerated() {
        return this != SYSTEM;
    }
}
