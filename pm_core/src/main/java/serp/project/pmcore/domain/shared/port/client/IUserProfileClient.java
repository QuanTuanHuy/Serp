/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.port.client;

import java.util.List;

import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;

public interface IUserProfileClient {
    UserProfileDto getUserProfileById(Long userId);

    List<UserProfileDto> getUserProfilesByIds(List<Long> userIds);
}
