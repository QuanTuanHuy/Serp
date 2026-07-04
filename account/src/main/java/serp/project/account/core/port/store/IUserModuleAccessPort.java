/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.UserModuleAccessEntity;

import java.util.List;
import java.util.Map;

public interface IUserModuleAccessPort {
    UserModuleAccessEntity save(UserModuleAccessEntity userModuleAccess);

    UserModuleAccessEntity getUserModuleAccess(Long userId, Long moduleId, Long organizationId);

    List<UserModuleAccessEntity> getUserModuleAccessesByUserId(Long userId);

    List<UserModuleAccessEntity> getUserModuleAccessesByUserIdAndOrgId(Long userId, Long organizationId);

    List<UserModuleAccessEntity> getActiveUsersByModuleAndOrg(Long moduleId, Long organizationId);

    boolean hasAccess(Long userId, Long moduleId, Long organizationId);

    int countActiveUsers(Long moduleId, Long organizationId);

    Map<Long, Integer> countActiveModulesByUserIds(List<Long> userIds);

    void deleteUserModuleAccess(Long id);

    List<UserModuleAccessEntity> saveAll(List<UserModuleAccessEntity> userModuleAccesses);

    List<UserModuleAccessEntity> getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(
            List<Long> userIds,
            Long moduleId,
            Long organizationId);
}
