/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Internal Spring event for reaction added
 */

package serp.project.discuss_service.core.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Internal Spring event published when a reaction is added to a message.
 * Triggers post-commit Kafka publishing after transaction success.
 */
@Getter
public class ReactionAddedInternalEvent extends ApplicationEvent {

    private final Long messageId;
    private final Long channelId;
    private final Long userId;
    private final String emoji;

    public ReactionAddedInternalEvent(Object source, Long messageId, Long channelId, 
                                      Long userId, String emoji) {
        super(source);
        this.messageId = messageId;
        this.channelId = channelId;
        this.userId = userId;
        this.emoji = emoji;
    }
}
