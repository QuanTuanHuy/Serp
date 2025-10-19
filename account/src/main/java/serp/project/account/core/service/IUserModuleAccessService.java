/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.entity.UserModuleAccessEntity;

import java.util.List;

public interface IUserModuleAccessService {
    /**
     * Register user to module (grant access)
     */
    UserModuleAccessEntity registerUserToModule(Long userId, Long moduleId, Long organizationId, Long grantedBy);

    /**
     * Register user to module with expiration
     */
    UserModuleAccessEntity registerUserToModuleWithExpiration(Long userId, Long moduleId, Long organizationId,
            Long grantedBy, Long expiresAt);

    /**
     * Bulk register users to module
     */
    List<UserModuleAccessEntity> bulkRegisterUsersToModule(List<Long> userIds, Long moduleId, Long organizationId,
            Long grantedBy);

    /**
     * Revoke user access to module
     */
    void revokeUserModuleAccess(Long userId, Long moduleId, Long organizationId);

    /**
     * Check if user has access to module
     */
    boolean hasAccess(Long userId, Long moduleId, Long organizationId);

    /**
     * Get user module accesses
     */
    List<UserModuleAccessEntity> getUserModuleAccesses(Long userId, Long organizationId);

    /**
     * Get users with access to module in organization
     */
    List<UserModuleAccessEntity> getUsersWithModuleAccess(Long moduleId, Long organizationId);

    /**
     * Count active users for module in organization
     */
    int countActiveUsers(Long moduleId, Long organizationId);
}
