package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.domain.enums.UserStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IPresenceService {

    void setUserOnline(Long userId, Long tenantId);

    void setUserOffline(Long userId);

    void updateUserStatus(Long userId, UserStatus status, String statusMessage);

    Optional<UserPresenceEntity> getUserPresence(Long userId);

    Map<Long, UserPresenceEntity> getPresenceBatch(Set<Long> userIds);

    boolean isUserOnline(Long userId);

    void userJoinedChannel(Long userId, Long channelId);

    void userLeftChannel(Long userId, Long channelId);

    Set<Long> getOnlineUsersInChannel(Long channelId);

    List<UserPresenceEntity> getOnlineUsersWithPresence(Long channelId);
}
