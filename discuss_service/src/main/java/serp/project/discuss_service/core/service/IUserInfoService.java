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

/**
 * Service interface for enriching channel members with user information.
 */
public interface IUserInfoService {

    String USER_INFO_CACHE_PREFIX = "discuss:user_info:";
    String USER_INFO_BY_TENANT_CACHE_PREFIX = "discuss:tenant:%d:user_info";
    long USER_INFO_CACHE_TTL = 3600; // 1 hour

    /**
     * Enrich a list of channel members with user information.
     *
     * @param members list of channel member entities
     * @return list of enriched ChannelMemberResponse with user info
     */
    List<ChannelMemberResponse> enrichMembersWithUserInfo(List<ChannelMemberEntity> members);

    /**
     * Enrich a single channel member with user information.
     *
     * @param member the channel member entity
     * @return enriched ChannelMemberResponse with user info
     */
    ChannelMemberResponse enrichMemberWithUserInfo(ChannelMemberEntity member);

    /**
     * Enrich a message with sender user information.
     *
     * @param message the message response
     * @return enriched MessageResponse with sender user info
     */
    MessageResponse enrichMessageWithUserInfo(MessageResponse message);

    /**
     * Enrich a list of messages with sender user information.
     *
     * @param messages list of message responses
     * @return list of enriched MessageResponse with sender user info
     */
    List<MessageResponse> enrichMessagesWithUserInfo(List<MessageResponse> messages);

    Optional<ChannelMemberResponse.UserInfo> getUserById(Long userId);

    /**
     * Get user info for a tenant based on a search query.
     * @param tenantId
     * @param query
     * @return list of UserInfo matching the query
     */
    List<ChannelMemberResponse.UserInfo> getUsersForTenant(Long tenantId, String query);
}
