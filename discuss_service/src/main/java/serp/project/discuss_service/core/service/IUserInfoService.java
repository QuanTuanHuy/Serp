/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Interface for user info enrichment service
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;

import java.util.List;

/**
 * Service interface for enriching channel members with user information.
 */
public interface IUserInfoService {

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
}
