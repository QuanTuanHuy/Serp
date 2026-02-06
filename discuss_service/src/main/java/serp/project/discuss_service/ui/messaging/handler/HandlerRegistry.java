/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket event handler registry
 */

package serp.project.discuss_service.ui.messaging.handler;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;

@Getter
@Component
@Slf4j
public class HandlerRegistry {

    private final Map<WsEventType, IWsEventHandler> messageHandlers;
    private final Map<WsEventType, IWsEventHandler> reactionHandlers;
    private final Map<WsEventType, IWsEventHandler> presenceHandlers;

    public HandlerRegistry(
            List<IMessageEventHandler> messageHandlers,
            List<IReactionEventHandler> reactionHandlers,
            List<IPresenceEventHandler> presenceHandlers) {
        this.messageHandlers = buildHandlers(messageHandlers, "message");
        this.reactionHandlers = buildHandlers(reactionHandlers, "reaction");
        this.presenceHandlers = buildHandlers(presenceHandlers, "presence");
    }

    private Map<WsEventType, IWsEventHandler> buildHandlers(
            List<? extends IWsEventHandler> handlers,
            String handlerName) {
        Map<WsEventType, IWsEventHandler> map = new EnumMap<>(WsEventType.class);
        if (handlers == null) {
            return map;
        }
        for (IWsEventHandler handler : handlers) {
            if (handler == null || handler.getType() == null) {
                log.warn("Skipped invalid {} handler registration", handlerName);
                continue;
            }
            IWsEventHandler previous = map.put(handler.getType(), handler);
            if (previous != null) {
                log.warn("Duplicate {} handler for type {} replaced {} with {}",
                        handlerName, handler.getType(),
                        previous.getClass().getSimpleName(),
                        handler.getClass().getSimpleName());
            }
        }
        return map;
    }
}
