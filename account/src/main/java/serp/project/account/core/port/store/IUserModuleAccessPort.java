/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.UserModuleAccessEntity;

import java.util.List;

public interface IUserModuleAccessPort {
    UserModuleAccessEntity save(UserModuleAccessEntity userModuleAccess);

    UserModuleAccessEntity getUserModuleAccess(Long userId, Long moduleId, Long organizationId);

    List<UserModuleAccessEntity> getUserModuleAccessesByUserId(Long userId);

    List<UserModuleAccessEntity> getUserModuleAccessesByUserIdAndOrgId(Long userId, Long organizationId);

    List<UserModuleAccessEntity> getActiveUsersByModuleAndOrg(Long moduleId, Long organizationId);

    boolean hasAccess(Long userId, Long moduleId, Long organizationId);

    int countActiveUsers(Long moduleId, Long organizationId);

    void deleteUserModuleAccess(Long id);
}
