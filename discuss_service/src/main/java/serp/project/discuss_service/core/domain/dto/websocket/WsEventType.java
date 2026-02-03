/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket event types enumeration
 */

package serp.project.discuss_service.core.domain.dto.websocket;

/**
 * Enumeration of WebSocket event types for real-time messaging
 */
public enum WsEventType {
    // Message events
    MESSAGE_NEW,
    MESSAGE_UPDATED,
    MESSAGE_DELETED,
    
    // Reaction events
    REACTION_ADDED,
    REACTION_REMOVED,
    
    // Typing indicator events
    TYPING_START,
    TYPING_STOP,
    
    // Presence events
    USER_ONLINE,
    USER_OFFLINE,
    USER_PRESENCE_CHANGED,
    
    // Channel events
    CHANNEL_CREATED,
    CHANNEL_UPDATED,
    CHANNEL_ARCHIVED,
    
    // Member events
    MEMBER_JOINED,
    MEMBER_LEFT,
    MEMBER_REMOVED,
    MEMBER_ROLE_CHANGED,
    
    // Read status events
    MESSAGE_READ,
    
    // Error events
    ERROR
}
