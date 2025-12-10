/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.client;

import serp.project.crm.core.domain.dto.response.user.UserProfileResponse;

public interface IUserProfileClient {
    UserProfileResponse getUserProfileById(Long userId);
}
