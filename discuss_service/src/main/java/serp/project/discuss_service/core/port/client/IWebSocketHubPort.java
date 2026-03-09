/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket hub port for real-time messaging
 */

package serp.project.discuss_service.core.port.client;

import java.util.Set;

public interface IWebSocketHubPort {

    void sendToUser(Long userId, Object payload);

    void sendErrorToUser(Long userId, Object payload);

    void sendToUsers(Set<Long> userIds, Object payload);

}
