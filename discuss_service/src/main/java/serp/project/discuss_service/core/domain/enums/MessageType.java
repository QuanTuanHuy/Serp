/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Simplified message type enumeration
 */

package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;

/**
 * Represents the type of a message in the discuss service.
 * Simplified to two types for internal team chat:
 * - STANDARD: Regular user messages (text and/or attachments)
 * - SYSTEM: System-generated notifications
 */
@Getter
public enum MessageType {
    STANDARD("STANDARD", "Standard Message"),
    SYSTEM("SYSTEM", "System Message");

    private final String code;
    private final String displayName;

    MessageType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * Parse message type from string code.
     * Provides backward compatibility for old message types.
     * 
     * @param code The message type code
     * @return The corresponding MessageType
     */
    public static MessageType fromCode(String code) {
        if (code == null) {
            return STANDARD;
        }
        
        return switch (code.toUpperCase()) {
            case "STANDARD" -> STANDARD;
            case "SYSTEM" -> SYSTEM;
            // Backward compatibility: map old types to STANDARD
            case "TEXT", "IMAGE", "FILE", "CODE", "POLL" -> STANDARD;
            default -> STANDARD;
        };
    }

    /**
     * Check if this message type requires content.
     * SYSTEM messages always require content.
     * STANDARD messages can have content, attachments, or both.
     * 
     * @return true if content is required
     */
    public boolean requiresContent() {
        return this == SYSTEM;
    }

    /**
     * Check if this message type is user-generated.
     * 
     * @return true if the message is from a user
     */
    public boolean isUserGenerated() {
        return this != SYSTEM;
    }
}
