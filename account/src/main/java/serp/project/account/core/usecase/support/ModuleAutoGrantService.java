/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.response.AutoGrantBackfillResponse;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IOrganizationModuleAccessSettingService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleAutoGrantService {

    private final IOrganizationModuleAccessSettingService settingService;
    private final ISubscriptionService subscriptionService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final IUserModuleAccessService userModuleAccessService;
    private final IUserService userService;
    private final IRoleService roleService;
    private final ICombineRoleService combineRoleService;
    private final UserSyncPublisher userSyncPublisher;

    @Transactional(rollbackFor = Exception.class)
    public AutoGrantBackfillResponse backfillExistingUsers(Long organizationId, Long moduleId, Long grantedBy) {
        if (!settingService.isAutoGrantEnabled(organizationId, moduleId)) {
            throw new AppException(Constants.ErrorMessage.AUTO_GRANT_POLICY_NOT_ENABLED);
        }

        var subscription = subscriptionService.getActiveOrPendingUpgrade(organizationId);
        var planModule = getIncludedPlanModule(subscription, moduleId);
        var defaultRoles = getDefaultModuleRoles(moduleId);
        if (CollectionUtils.isEmpty(defaultRoles)) {
            throw new AppException(Constants.ErrorMessage.AUTO_GRANT_REQUIRES_DEFAULT_MODULE_ROLE);
        }

        var result = AutoGrantBackfillResponse.empty(organizationId, moduleId);
        long candidateCount = userService.countActiveUsersWithoutModuleAccess(organizationId, moduleId);
        int grantLimit = getGrantLimit(planModule, moduleId, organizationId, candidateCount);
        if (grantLimit <= 0) {
            result.markSkipped(Constants.ErrorMessage.MAX_USERS_LIMIT_REACHED, candidateCount);
            return result;
        }

        var usersToGrant = userService.getActiveUsersWithoutModuleAccess(organizationId, moduleId, grantLimit);
        if (CollectionUtils.isEmpty(usersToGrant)) {
            return result;
        }
        var userIds = usersToGrant.stream().map(UserEntity::getId).toList();

        userModuleAccessService.bulkRegisterUsersToModuleWithExpiration(
                userIds,
                moduleId,
                organizationId,
                grantedBy,
                subscription.getEndDate());
        combineRoleService.assignRolesToUsers(usersToGrant, defaultRoles);
        usersToGrant.forEach(user -> userSyncPublisher.publishUserSync(organizationId, user.getId()));

        result.markGranted(usersToGrant.size());
        result.markSkipped(Constants.ErrorMessage.MAX_USERS_LIMIT_REACHED, candidateCount - usersToGrant.size());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public AutoGrantBackfillResponse grantConfiguredModulesToNewUser(Long organizationId, Long userId, Long grantedBy) {
        var result = AutoGrantBackfillResponse.empty(organizationId, null);
        var user = userService.getUserById(userId);
        if (user == null || !user.isActive()) {
            result.markSkipped(Constants.ErrorMessage.USER_NOT_FOUND);
            return result;
        }

        var subscription = subscriptionService.getActiveOrPendingUpgrade(organizationId);
        Map<Long, SubscriptionPlanModuleEntity> includedPlanModules = subscriptionPlanService
                .getPlanModules(subscription.getSubscriptionPlanId())
                .stream()
                .filter(SubscriptionPlanModuleEntity::isAccessible)
                .collect(Collectors.toMap(SubscriptionPlanModuleEntity::getModuleId, Function.identity()));
        List<RoleEntity> rolesToAssign = new ArrayList<>();

        for (var setting : settingService.getEnabledByOrganizationId(organizationId)) {
            Long moduleId = setting.getModuleId();
            var planModule = includedPlanModules.get(moduleId);
            if (planModule == null) {
                result.markSkipped(Constants.ErrorMessage.MODULE_NOT_IN_SUBSCRIPTION_PLAN);
                continue;
            }
            if (userModuleAccessService.hasAccess(userId, moduleId, organizationId)) {
                continue;
            }
            var defaultRoles = getDefaultModuleRoles(moduleId);
            if (CollectionUtils.isEmpty(defaultRoles)) {
                log.warn("Skip auto-grant for user {} module {} because no default module role exists",
                        userId, moduleId);
                result.markSkipped(Constants.ErrorMessage.AUTO_GRANT_REQUIRES_DEFAULT_MODULE_ROLE);
                continue;
            }
            if (getRemainingSlots(planModule, moduleId, organizationId) == 0) {
                result.markSkipped(Constants.ErrorMessage.MAX_USERS_LIMIT_REACHED);
                continue;
            }
            try {
                userModuleAccessService.registerUserToModuleWithExpiration(
                        userId,
                        moduleId,
                        organizationId,
                        grantedBy,
                        subscription.getEndDate());
                rolesToAssign.addAll(defaultRoles);
                result.markGranted();
            } catch (Exception e) {
                log.warn("Skip auto-grant for user {} module {} because grant failed", userId, moduleId, e);
                result.markSkipped(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }
        }
        if (!CollectionUtils.isEmpty(rolesToAssign)) {
            combineRoleService.assignRolesToUser(user, rolesToAssign);
        }
        return result;
    }

    private SubscriptionPlanModuleEntity getIncludedPlanModule(
            OrganizationSubscriptionEntity subscription, Long moduleId) {
        return subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId()).stream()
                .filter(SubscriptionPlanModuleEntity::isAccessible)
                .filter(planModule -> planModule.getModuleId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new AppException(Constants.ErrorMessage.MODULE_NOT_IN_SUBSCRIPTION_PLAN));
    }

    private List<RoleEntity> getDefaultModuleRoles(Long moduleId) {
        return roleService.getRolesByModuleId(moduleId).stream()
                .filter(RoleEntity::isAutoAssigned)
                .toList();
    }

    private int getRemainingSlots(SubscriptionPlanModuleEntity planModule, Long moduleId, Long organizationId) {
        if (!planModule.hasUserLimit()) {
            return -1;
        }
        int currentUsers = userModuleAccessService.countActiveUsers(moduleId, organizationId);
        return Math.max(0, planModule.getMaxUsersPerModule() - currentUsers);
    }

    private int getGrantLimit(
            SubscriptionPlanModuleEntity planModule,
            Long moduleId,
            Long organizationId,
            long candidateCount) {
        if (candidateCount <= 0) {
            return 0;
        }
        if (!planModule.hasUserLimit()) {
            return candidateCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) candidateCount;
        }
        int remainingSlots = getRemainingSlots(planModule, moduleId, organizationId);
        return (int) Math.min(candidateCount, remainingSlots);
    }
}
