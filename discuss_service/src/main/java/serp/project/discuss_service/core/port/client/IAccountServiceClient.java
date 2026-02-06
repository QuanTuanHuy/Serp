/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Client to call Account Service APIs
 */

package serp.project.discuss_service.core.port.client;

import java.util.List;
import java.util.Optional;

import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;

public interface IAccountServiceClient {

    Optional<ChannelMemberResponse.UserInfo> getUserById(Long userId);

    List<ChannelMemberResponse.UserInfo> getUsersForTenant(Long tenantId, String query);
}
