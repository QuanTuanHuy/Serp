/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Interface for user info enrichment service
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;

import java.util.List;
import java.util.Optional;

public interface IUserInfoService {

    String USER_INFO_CACHE_PREFIX = "discuss:user_info:";
    String USER_INFO_BY_TENANT_CACHE_PREFIX = "discuss:tenant:%d:user_info";
    long USER_INFO_CACHE_TTL = 3600;

    List<ChannelMemberResponse> enrichMembersWithUserInfo(List<ChannelMemberEntity> members);

    ChannelMemberResponse enrichMemberWithUserInfo(ChannelMemberEntity member);

    MessageResponse enrichMessageWithUserInfo(MessageResponse message);

    List<MessageResponse> enrichMessagesWithUserInfo(List<MessageResponse> messages);

    Optional<ChannelMemberResponse.UserInfo> getUserById(Long userId);

    List<ChannelMemberResponse.UserInfo> getUsersForTenant(Long tenantId, String query);
}
