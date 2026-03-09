/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity action type enumeration
 */

package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;

/**
 * Represents the type of activity in the activity feed.
 */
@Getter
public enum ActionType {
    // Message actions
    MESSAGE_SENT("MESSAGE_SENT", "sent a message"),
    MESSAGE_EDITED("MESSAGE_EDITED", "edited a message"),
    MESSAGE_DELETED("MESSAGE_DELETED", "deleted a message"),
    
    // Mention actions
    MENTION_RECEIVED("MENTION_RECEIVED", "mentioned you"),
    
    // Reaction actions
    REACTION_ADDED("REACTION_ADDED", "reacted to your message"),
    
    // Channel actions
    CHANNEL_CREATED("CHANNEL_CREATED", "created a channel"),
    CHANNEL_UPDATED("CHANNEL_UPDATED", "updated a channel"),
    CHANNEL_ARCHIVED("CHANNEL_ARCHIVED", "archived a channel"),
    
    // Member actions
    MEMBER_JOINED("MEMBER_JOINED", "joined the channel"),
    MEMBER_LEFT("MEMBER_LEFT", "left the channel"),
    MEMBER_ADDED("MEMBER_ADDED", "was added to the channel"),
    MEMBER_REMOVED("MEMBER_REMOVED", "was removed from the channel"),
    
    // Thread actions
    REPLY_RECEIVED("REPLY_RECEIVED", "replied to your message"),
    
    // Entity actions (from other services)
    TASK_ASSIGNED("TASK_ASSIGNED", "assigned a task"),
    CUSTOMER_CREATED("CUSTOMER_CREATED", "created a customer"),
    ORDER_CREATED("ORDER_CREATED", "created an order");

    private final String code;
    private final String description;

    ActionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ActionType fromCode(String code) {
        for (ActionType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown action type: " + code);
    }

    public boolean isMessageAction() {
        return this == MESSAGE_SENT || this == MESSAGE_EDITED || this == MESSAGE_DELETED;
    }

    public boolean requiresNotification() {
        return this == MENTION_RECEIVED || this == REPLY_RECEIVED || 
               this == TASK_ASSIGNED || this == MEMBER_ADDED;
    }
}
