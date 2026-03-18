/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.user.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.IUserService;
import serp.project.account.core.usecase.support.OrganizationRoleResolver;
import serp.project.account.kernel.utils.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserRoleCoordinator {
    private final IUserService userService;
    private final IRoleService roleService;
    private final ICombineRoleService combineRoleService;
    private final OrganizationRoleResolver organizationRoleResolver;

    @Transactional(rollbackFor = Exception.class)
    public void assignRolesAsSystemAdmin(Long userId, List<Long> roleIds) {
        var user = getUserOrThrow(userId);
        var roles = roleService.getAllRoles().stream()
                .filter(role -> roleIds.contains(role.getId()))
                .toList();
        if (CollectionUtils.isEmpty(roles)) {
            throw new AppException(Constants.ErrorMessage.ROLE_NOT_FOUND);
        }

        combineRoleService.assignRolesToUser(user, roles);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrganizationRoles(Long organizationId, Long userId, List<Long> newRoleIds) {
        var user = getUserOrThrow(userId);
        ensureUserInOrganization(user, organizationId);

        var validRoles = organizationRoleResolver.getValidRolesForOrganization(organizationId).stream()
                .filter(role -> newRoleIds.contains(role.getId()))
                .filter(role -> !role.isSystemRole())
                .toList();
        if (validRoles.isEmpty()) {
            throw new AppException(Constants.ErrorMessage.ROLE_NOT_FOUND);
        }

        var currentRoleIds = user.getRoles() != null
                ? user.getRoles().stream().map(RoleEntity::getId).toList()
                : List.<Long>of();

        var rolesToAdd = validRoles.stream()
                .filter(role -> !currentRoleIds.contains(role.getId()))
                .toList();
        var rolesToRemove = user.getRoles() != null
                ? user.getRoles().stream()
                        .filter(role -> !newRoleIds.contains(role.getId()))
                        .toList()
                : List.<RoleEntity>of();

        if (!rolesToAdd.isEmpty()) {
            combineRoleService.assignRolesToUser(user, rolesToAdd);
        }
        if (!rolesToRemove.isEmpty()) {
            combineRoleService.removeRolesFromUser(user, rolesToRemove);
        }
    }

    private UserEntity getUserOrThrow(Long userId) {
        var user = userService.getUserById(userId);
        if (user == null) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND);
        }
        return user;
    }

    private void ensureUserInOrganization(UserEntity user, Long organizationId) {
        if (!Objects.equals(user.getPrimaryOrganizationId(), organizationId)) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_IN_ORGANIZATION,
                    Constants.HttpStatusCode.FORBIDDEN);
        }
    }
}
