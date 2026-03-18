/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.kernel.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrganizationRoleResolver {
    private final IRoleService roleService;
    private final IOrganizationService organizationService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final ISubscriptionService subscriptionService;

    public List<RoleEntity> getValidRolesForOrganization(Long organizationId) {
        var organization = organizationService.getOrganizationById(organizationId);
        if (organization == null) {
            throw new AppException(Constants.ErrorMessage.ORGANIZATION_NOT_FOUND, Constants.HttpStatusCode.NOT_FOUND);
        }

        var allRoles = new ArrayList<RoleEntity>();
        allRoles.addAll(roleService.getRolesByScope(RoleScope.ORGANIZATION));
        allRoles.addAll(roleService.getRolesByScope(RoleScope.DEPARTMENT));

        if (organization.getSubscriptionId() == null) {
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }

        var subscription = subscriptionService.getSubscriptionById(organization.getSubscriptionId());
        if (subscription == null) {
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }

        var moduleIds = subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId()).stream()
                .filter(SubscriptionPlanModuleEntity::getIsIncluded)
                .map(SubscriptionPlanModuleEntity::getModuleId)
                .filter(Objects::nonNull)
                .toList();

        var moduleRoles = roleService.getRolesByScope(RoleScope.MODULE).stream()
                .filter(role -> role.getModuleId() != null && moduleIds.contains(role.getModuleId()))
                .toList();
        allRoles.addAll(moduleRoles);

        return allRoles;
    }

    public List<RoleEntity> resolveRequestedOrAutoAssignedRoles(Long organizationId, List<Long> requestedRoleIds) {
        if (CollectionUtils.isEmpty(requestedRoleIds)) {
            return roleService.getRolesByScope(RoleScope.ORGANIZATION).stream()
                    .filter(RoleEntity::isAutoAssigned)
                    .toList();
        }

        return getValidRolesForOrganization(organizationId).stream()
                .filter(role -> requestedRoleIds.contains(role.getId()))
                .toList();
    }
}
