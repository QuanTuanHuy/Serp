/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import java.util.List;

import serp.project.account.core.domain.entity.UserRoleEntity;

public interface IUserRolePort {
    List<UserRoleEntity> saveAll(List<UserRoleEntity> userRoles);
    List<UserRoleEntity> getUserRolesByUserId(Long userId);
}
