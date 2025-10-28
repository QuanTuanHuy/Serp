/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import java.util.List;

import serp.project.account.core.domain.entity.UserRoleEntity;

public interface IUserRolePort {
    void saveAll(List<UserRoleEntity> userRoles);
    List<UserRoleEntity> getUserRolesByUserId(Long userId);
    List<UserRoleEntity> getUserRolesByUserIds(List<Long> userIds);
    void deleteUserRolesByUserIdAndRoleIds(Long userId, List<Long> roleIds);
}
