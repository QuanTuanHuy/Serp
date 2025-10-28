/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import java.util.List;

import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;

public interface ICombineRoleService {
    void assignRolesToUser(UserEntity user, List<RoleEntity> roles);

    void removeRolesFromUser(UserEntity user, List<RoleEntity> roles);
}
