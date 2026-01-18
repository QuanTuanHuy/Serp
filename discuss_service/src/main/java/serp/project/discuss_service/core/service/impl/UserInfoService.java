/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Service to enrich channel members with user information
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse.UserInfo;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.client.IAccountServiceClient;
import serp.project.discuss_service.core.port.client.ICachePort;
import serp.project.discuss_service.core.service.IUserInfoService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Service to enrich channel member responses with user information.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService implements IUserInfoService {

    private final IAccountServiceClient accountServiceClient;
    private final ICachePort cachePort;

    @Override
    public List<ChannelMemberResponse> enrichMembersWithUserInfo(List<ChannelMemberEntity> members) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Map<Long, Future<Optional<ChannelMemberResponse.UserInfo>>> userInfoFutures = new ConcurrentHashMap<>();

            members.stream()
                    .map(ChannelMemberEntity::getUserId)
                    .distinct()
                    .forEach(userId -> {
                        Future<Optional<ChannelMemberResponse.UserInfo>> future = executor.submit(
                                () -> fetchUserInfo(userId)
                        );
                        userInfoFutures.put(userId, future);
                    });

            return members.stream()
                    .map(member -> buildMemberResponse(member, userInfoFutures))
                    .toList();
        }
    }

    @Override
    public ChannelMemberResponse enrichMemberWithUserInfo(ChannelMemberEntity member) {
        if (member == null) {
            return null;
        }

        ChannelMemberResponse response = ChannelMemberResponse.fromEntity(member);
        Optional<ChannelMemberResponse.UserInfo> userInfo = fetchUserInfo(member.getUserId());
        userInfo.ifPresent(response::setUser);

        return response;
    }

    @Override
    public MessageResponse enrichMessageWithUserInfo(MessageResponse message) {
        if (message == null) {
            return null;
        }

        Optional<ChannelMemberResponse.UserInfo> userInfo = fetchUserInfo(message.getSenderId());
        userInfo.ifPresent(message::setSender);

        return message;
    }

    @Override
    public List<MessageResponse> enrichMessagesWithUserInfo(List<MessageResponse> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        messages.forEach(this::enrichMessageWithUserInfo);
        return messages;
    }

    @Override
    public List<UserInfo> getUsersForTenant(Long tenantId, String query) {
        if (tenantId == null) {
            throw new AppException(ErrorCode.TENANT_ID_REQUIRED);
        }
        try {
            if (query != null && !query.isEmpty()) {
                return accountServiceClient.getUsersForTenant(tenantId, query);
            }
            String cacheKey = String.format(USER_INFO_BY_TENANT_CACHE_PREFIX, tenantId);
            List<UserInfo> cachedUsers = cachePort.getFromCache(cacheKey, List.class);
            if (cachedUsers != null && !cachedUsers.isEmpty()) {
                return cachedUsers;
            }
            List<UserInfo> users = accountServiceClient.getUsersForTenant(tenantId, null);
            if (users != null && !users.isEmpty()) {
                cachePort.setToCache(cacheKey, users, USER_INFO_CACHE_TTL);
            }
            return users;
        } catch (Exception e) {
            log.error("Error fetching users for tenant {}: {}", tenantId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Fetch user info from account service.
     */
    private Optional<ChannelMemberResponse.UserInfo> fetchUserInfo(Long userId) {
        try {
            String cacheKey = USER_INFO_CACHE_PREFIX + userId;
            ChannelMemberResponse.UserInfo cachedInfo = cachePort.getFromCache(cacheKey, ChannelMemberResponse.UserInfo.class);
            if (cachedInfo != null) {
                return Optional.of(cachedInfo);
            }
            Optional<ChannelMemberResponse.UserInfo> userInfo = accountServiceClient.getUserById(userId);
            userInfo.ifPresent(info -> cachePort.setToCache(cacheKey, info, USER_INFO_CACHE_TTL));
            return userInfo;
        } catch (Exception e) {
            log.error("Failed to fetch user info for userId {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Build ChannelMemberResponse with user info from futures.
     */
    private ChannelMemberResponse buildMemberResponse(
            ChannelMemberEntity member,
            Map<Long, Future<Optional<ChannelMemberResponse.UserInfo>>> userInfoFutures) {

        ChannelMemberResponse response = ChannelMemberResponse.fromEntity(member);

        try {
            Future<Optional<ChannelMemberResponse.UserInfo>> future = userInfoFutures.get(member.getUserId());
            if (future != null) {
                Optional<ChannelMemberResponse.UserInfo> userInfo = future.get();
                userInfo.ifPresent(response::setUser);
            }
        } catch (Exception e) {
            log.warn("Failed to get user info for member {}: {}", member.getUserId(), e.getMessage());
        }

        return response;
    }
}
