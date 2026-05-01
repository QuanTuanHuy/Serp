/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.client;

import serp.project.crm.core.domain.dto.response.user.UserProfileResponse;

import java.util.List;

public interface IUserProfileClient {
    UserProfileResponse getUserProfileById(Long userId);

    List<UserProfileResponse> getUserProfilesByIds(List<Long> userIds);
}
