/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Presence service implementation
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.domain.enums.UserStatus;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.core.service.IPresenceService;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService implements IPresenceService {

    private final IDiscussCacheService cacheService;
    private final IDiscussEventPublisher eventPublisher;

    @Override
    public void registerSession(Long userId, Long tenantId, String sessionId, String instanceId) {
        cacheService.storeSession(sessionId, userId, instanceId);
        int activeSessions = cacheService.getActiveSessionCount(userId);
        if (activeSessions == 1) {
            setUserOnline(userId, tenantId);
            log.info("User {} registered first active session {}", userId, sessionId);
            eventPublisher.publishUserOnline(userId);
        } else {
            log.info("User {} registered additional session {} (total active sessions: {})", userId, sessionId, activeSessions);
        }
    }

    @Override
    public void unregisterSession(Long userId, String sessionId) {
        cacheService.removeSession(sessionId, userId);
        int activeSessions = cacheService.getActiveSessionCount(userId);
        if (activeSessions == 0) {
            setUserOffline(userId);
            cacheService.removeAllUserSubscriptions(userId);
            log.info("User {} has no more active sessions after unregistering {}", userId, sessionId);
            eventPublisher.publishUserOffline(userId);
        } else {
            log.info("User {} unregistered session {} (remaining active sessions: {})", userId, sessionId, activeSessions);
        }
    }

    @Override
    public void unregisterSessionBySessionId(String sessionId) {
        if (sessionId == null) {
            return;
        }
        cacheService.getSession(sessionId)
                .map(IDiscussCacheService.SessionInfo::userId)
                .ifPresentOrElse(
                        userId -> unregisterSession(userId, sessionId),
                        () -> log.debug("No session info found for sessionId {}", sessionId)
                );
    }

    @Override
    public void setUserOnline(Long userId, Long tenantId) {
        Optional<UserPresenceEntity> presenceOpt = getUserPresence(userId);
        UserPresenceEntity presence;
        if (presenceOpt.isEmpty()) {
            presence = UserPresenceEntity.online(userId, tenantId);
        } else {
            presence = presenceOpt.get();
            presence.goOnline();
        }
        cacheService.setUserPresence(presence);
        log.info("User {} is now ONLINE", userId);
    }

    @Override
    public void setUserOffline(Long userId) {
        Optional<UserPresenceEntity> presenceOpt = getUserPresence(userId);
        if (presenceOpt.isEmpty()) {
            UserPresenceEntity presence = UserPresenceEntity.offline(userId);
            cacheService.setUserPresence(presence);
            return;
        }
        UserPresenceEntity presence = presenceOpt.get();
        presence.goOffline();
        cacheService.setUserPresence(presence);
        log.info("User {} is now OFFLINE", userId);
    }

    @Override
    public void updateUserStatus(Long userId, UserStatus status, String statusMessage) {
        Optional<UserPresenceEntity> presenceOpt = getUserPresence(userId);
        if (presenceOpt.isEmpty()) {
            log.warn("Cannot update status for non-existing presence of userId {}", userId);
            return;
        }
        UserPresenceEntity presence = presenceOpt.get();
        presence.setCustomStatus(status, statusMessage);
        cacheService.setUserPresence(presence);
    }

    @Override
    public Optional<UserPresenceEntity> getUserPresence(Long userId) {
        return cacheService.getUserPresence(userId);
    }

    @Override
    public Map<Long, UserPresenceEntity> getPresenceBatch(Set<Long> userIds) {
        return cacheService.getUserPresenceBatch(userIds);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        Optional<UserPresenceEntity> presenceOpt = getUserPresence(userId);
        return presenceOpt.map(UserPresenceEntity::isOnline).orElse(false);
    }

    @Override
    public Set<Long> getOnlineUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Set.of();
        }
        Map<Long, UserPresenceEntity> presenceMap = cacheService.getUserPresenceBatch(userIds);
        return userIds.stream()
                .filter(userId -> {
                    UserPresenceEntity presence = presenceMap.get(userId);
                    return presence != null && presence.isOnline();
                })
                .collect(Collectors.toSet());
    }
}
