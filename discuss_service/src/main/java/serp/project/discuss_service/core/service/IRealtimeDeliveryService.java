/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Realtime delivery boundary
 */

package serp.project.discuss_service.core.service;

import java.util.Set;

public interface IRealtimeDeliveryService {

    void deliverToUser(Long userId, Object payload);

    void deliverToUsers(Set<Long> userIds, Object payload);

    void deliverToChannel(Long channelId, Object payload);

    void deliverToChannelExcept(Long channelId, Long excludedUserId, Object payload);

    void deliverPresenceChange(Long changedUserId, Object payload);
}
