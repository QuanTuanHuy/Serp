/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Presence service contract
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.domain.enums.UserStatus;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IPresenceService {

    void registerSession(Long userId, Long tenantId, String sessionId, String instanceId);

    void unregisterSession(Long userId, String sessionId);

    void unregisterSessionBySessionId(String sessionId);

    void setUserOnline(Long userId, Long tenantId);

    void setUserOffline(Long userId);

    void updateUserStatus(Long userId, UserStatus status, String statusMessage);

    Optional<UserPresenceEntity> getUserPresence(Long userId);

    Map<Long, UserPresenceEntity> getPresenceBatch(Set<Long> userIds);

    boolean isUserOnline(Long userId);

    Set<Long> getOnlineUsers(Set<Long> userIds);
}
