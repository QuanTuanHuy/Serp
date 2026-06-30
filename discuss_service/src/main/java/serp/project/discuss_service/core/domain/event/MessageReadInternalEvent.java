/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Internal event for message read receipts
 */

package serp.project.discuss_service.core.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class MessageReadInternalEvent extends ApplicationEvent {

    private final Long channelId;
    private final Long messageId;
    private final Long userId;
    private final List<Long> readBy;
    private final Integer readCount;

    public MessageReadInternalEvent(
            Object source,
            Long channelId,
            Long messageId,
            Long userId,
            List<Long> readBy,
            Integer readCount) {
        super(source);
        this.channelId = channelId;
        this.messageId = messageId;
        this.userId = userId;
        this.readBy = readBy == null ? List.of() : List.copyOf(readBy);
        this.readCount = readCount == null ? 0 : readCount;
    }
}
