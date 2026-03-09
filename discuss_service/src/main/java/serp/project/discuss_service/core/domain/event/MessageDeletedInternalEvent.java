/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Internal Spring event for message deleted
 */

package serp.project.discuss_service.core.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import serp.project.discuss_service.core.domain.entity.MessageEntity;

@Getter
public class MessageDeletedInternalEvent extends ApplicationEvent {

    private final MessageEntity message;
    private final Long channelId;
    private final Long parentId;

    public MessageDeletedInternalEvent(Object source, MessageEntity message) {
        super(source);
        this.message = message;
        this.channelId = message.getChannelId();
        this.parentId = message.getParentId();
    }
}
