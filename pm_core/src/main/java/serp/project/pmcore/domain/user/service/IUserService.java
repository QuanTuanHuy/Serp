/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.user.service;

import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import java.util.List;

public interface IUserService {
    UserProfileDto getUserById(Long userId);
    List<UserProfileDto> getUserProfilesByIds(List<Long> userIds);
}
