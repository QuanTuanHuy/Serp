/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.entity.UserModuleAccessEntity;

import java.util.List;

public interface IUserModuleAccessService {
    UserModuleAccessEntity registerUserToModule(Long userId, Long moduleId, Long organizationId, Long grantedBy);

    void revokeUserModuleAccess(Long userId, Long moduleId, Long organizationId);

    boolean hasAccess(Long userId, Long moduleId, Long organizationId);

    List<UserModuleAccessEntity> getUserModuleAccesses(Long userId, Long organizationId);
}
