package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.domain.enums.UserStatus;
import serp.project.discuss_service.core.port.client.ICachePort;
import serp.project.discuss_service.core.service.IPresenceService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService implements IPresenceService {

    public static final String USER_PRESENCE_HASH_PREFIX = "discuss:presence:user:";
    public static final String CHANNEL_PRESENCE_SET_PREFIX = "discuss:presence:channel:";
    public static final String USER_CHANNELS_PREFIX = "discuss:presence:user_channels:";

    private final ICachePort cachePort;

    @Override
    public void setUserOnline(Long userId, Long tenantId) {

    }

    @Override
    public void setUserOffline(Long userId) {

    }

    @Override
    public void updateUserStatus(Long userId, UserStatus status, String statusMessage) {

    }

    @Override
    public Optional<UserPresenceEntity> getUserPresence(Long userId) {
        return Optional.empty();
    }

    @Override
    public Map<Long, UserPresenceEntity> getPresenceBatch(Set<Long> userIds) {
        return Map.of();
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return false;
    }

    @Override
    public void userJoinedChannel(Long userId, Long channelId) {

    }

    @Override
    public void userLeftChannel(Long userId, Long channelId) {

    }

    @Override
    public Set<Long> getOnlineUsersInChannel(Long channelId) {
        return Set.of();
    }

    @Override
    public List<UserPresenceEntity> getOnlineUsersWithPresence(Long channelId) {
        return List.of();
    }
}
