/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Client to call Account Service APIs
 */

package serp.project.discuss_service.core.port.client;

import java.util.Optional;

import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;

public interface IAccountServiceClient {
    /**
     * Fetch user profile by ID from Account Service.
     *
     * @param userId the user ID to fetch
     * @return Optional containing UserInfo if found, empty otherwise
     */
    Optional<ChannelMemberResponse.UserInfo> getUserById(Long userId);
}
