/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket event handler contract
 */

package serp.project.discuss_service.ui.messaging.handler;

import java.util.Map;

import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;

public interface IWsEventHandler extends IEventHandler<WsEvent<Map<String, Object>>, WsEventType> {
}
