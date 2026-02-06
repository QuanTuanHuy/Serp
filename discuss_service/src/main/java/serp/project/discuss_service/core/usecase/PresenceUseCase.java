/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Presence use case
 */

package serp.project.discuss_service.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.discuss_service.core.domain.dto.request.UpdatePresenceStatusRequest;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.dto.response.ChannelPresenceResponse;
import serp.project.discuss_service.core.domain.dto.response.UserPresenceResponse;
import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IPresenceService;
import serp.project.discuss_service.core.service.IUserInfoService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceUseCase {

    private final IPresenceService presenceService;
    private final IChannelMemberService memberService;
    private final IUserInfoService userInfoService;

    @Transactional(readOnly = true)
    public ChannelPresenceResponse getChannelPresence(Long channelId, Long userId) {
        if (!memberService.isMember(channelId, userId)) {
            log.warn("User {} is not a member of channel {}", userId, channelId);
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        Set<Long> memberIds = memberService.getMemberIds(channelId);
        Set<Long> onlineIds = presenceService.getOnlineUsers(memberIds);
        Map<Long, UserPresenceEntity> presenceMap = presenceService.getPresenceBatch(memberIds);
        List<UserPresenceEntity> presenceList = presenceMap.values().stream().toList();

        Map<String, List<UserPresenceResponse>> statusGroups = presenceList.stream()
                .map(presence -> {
                    var userInfo = userInfoService.getUserById(presence.getUserId());
                    String name = userInfo.map(ChannelMemberResponse.UserInfo::getName).orElse("Unknown User");
                    String avatar = userInfo.map(ChannelMemberResponse.UserInfo::getAvatarUrl).orElse("");
                    return UserPresenceResponse.fromEntity(presence, name, avatar);
                })
                .collect(Collectors.groupingBy(
                        r -> r.getStatus().getNormalizedName()
                ));

        return ChannelPresenceResponse.of(
                channelId,
                memberIds.size(),
                onlineIds.size(),
                statusGroups
        );
    }

    @Transactional(readOnly = true)
    public UserPresenceResponse getUserPresence(Long userId, Long tenantId) {
        var userInfo = userInfoService.getUserById(userId).orElseThrow(() -> {
            log.warn("User {} not found when fetching presence", userId);
            return new AppException(ErrorCode.MEMBER_NOT_FOUND);
        });
        UserPresenceEntity userPresence = presenceService.getUserPresence(userId)
                .filter(presence -> presence.getTenantId().equals(tenantId))
                .orElseGet(() -> UserPresenceEntity.offline(userId, tenantId));
        return UserPresenceResponse.fromEntity(userPresence, userInfo.getName(), userInfo.getAvatarUrl());
    }

    public UserPresenceEntity updatePresenceStatus(Long userId, Long tenantId, UpdatePresenceStatusRequest request) {
        presenceService.updateUserStatus(
                userId,
                tenantId,
                request.getStatus(),
                request.getStatusMessage()
        );
        return presenceService.getUserPresence(userId)
                .orElseThrow(() -> {
                    log.error("Presence for user {} not found after update", userId);
                    return new AppException(ErrorCode.PRESENCE_NOT_FOUND);
                });
    }
}
