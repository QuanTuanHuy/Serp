/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.*;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.core.service.IUserService;
import serp.project.account.infrastructure.store.mapper.OrganizationSubscriptionMapper;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionUseCase {

    private final ISubscriptionService subscriptionService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final IOrganizationService organizationService;
    private final IUserModuleAccessService userModuleAccessService;
    private final IRoleService roleService;
    private final IUserService userService;
    private final ICombineRoleService combineRoleService;
    private final IModuleService moduleService;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

    private final OrganizationSubscriptionMapper subscriptionMapper;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> subscribe(Long organizationId, SubscribeRequest request, Long requestedBy) {
        try {
            if (organizationId == null || requestedBy == null) {
                return responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            }
            log.info("[UseCase] Organization {} subscribing to plan {}", organizationId, request.getPlanId());

            if (subscriptionService.hasActiveSubscription(organizationId)) {
                throw new AppException(Constants.ErrorMessage.ORGANIZATION_ALREADY_HAS_ACTIVE_SUBSCRIPTION);
            }
            var plan = subscriptionPlanService.getPlanById(request.getPlanId());
            if (!plan.isAvailable()) {
                throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
            }

            var subscription = subscriptionService.subscribe(organizationId, request, requestedBy, plan);
            organizationService.updateSubscription(organizationId, subscription);

            // Implement later: Send notification

            log.info("[UseCase] Organization {} successfully subscribed to plan {}", organizationId,
                    request.getPlanId());
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error subscribing organization {}: {}", organizationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when subscribing organization {}: {}", organizationId, e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> startTrial(Long organizationId, Long planId, Long requestedBy) {
        try {
            if (organizationId == null || requestedBy == null) {
                return responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            }
            log.info("[UseCase] Organization {} starting trial for plan {}", organizationId, planId);

            if (!subscriptionService.hasActiveSubscription(organizationId)) {
                throw new AppException(Constants.ErrorMessage.ORGANIZATION_ALREADY_HAS_ACTIVE_SUBSCRIPTION);
            }
            var plan = subscriptionPlanService.getPlanById(planId);
            if (!plan.isAvailable()) {
                throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
            }
            if (!plan.hasTrial()) {
                throw new AppException(Constants.ErrorMessage.PLAN_DOES_NOT_SUPPORT_TRIAL);
            }

            var user = userService.getUserById(requestedBy);
            if (user == null || !user.getPrimaryOrganizationId().equals(organizationId)) {
                return responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            }

            var subscription = subscriptionService.startTrial(organizationId, plan, requestedBy);
            organizationService.updateSubscription(organizationId, subscription);

            // Auto-grant module access to organization owner
            try {
                var planModules = subscriptionPlanService.getPlanModules(plan.getId());

                for (var planModule : planModules) {
                    var module = moduleService.getModuleByIdFromCache(planModule.getModuleId());
                    if (module == null) {
                        log.error("Module ID {} not found. Skipping auto-grant.", planModule.getModuleId());
                        continue;
                    }
                    if (planModule.getIsIncluded()) {
                        userModuleAccessService.registerUserToModuleWithExpiration(
                                requestedBy,
                                planModule.getModuleId(),
                                organizationId,
                                requestedBy,
                                subscription.getEndDate());

                        List<RoleEntity> roles = roleService.getRolesByModuleId(planModule.getModuleId());
                        combineRoleService.assignRolesToUser(user, roles);
                    }
                }
                log.info("Auto-granted {} modules to organization owner", planModules.size());
            } catch (Exception e) {
                log.error("Error auto-granting modules to owner: {}", e.getMessage());
            }

            log.info("[UseCase] Organization {} successfully started trial", organizationId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error starting trial for organization {}: {}", organizationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when starting trial for organization {}: {}", organizationId, e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> upgradeSubscription(Long organizationId, UpgradeSubscriptionRequest request,
            Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} upgrading subscription to plan {}", organizationId,
                    request.getNewPlanId());
            if (organizationId == null || requestedBy == null) {
                return responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            }

            var currentSubscription = subscriptionService.getActiveSubscription(organizationId);
            var currentPlan = subscriptionPlanService.getPlanById(currentSubscription.getSubscriptionPlanId());
            var newPlan = subscriptionPlanService.getPlanById(request.getNewPlanId());
            if (!newPlan.isAvailable()) {
                throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
            }

            var newSubscription = subscriptionService.upgradeSubscription(organizationId, request,
                    requestedBy, currentPlan, newPlan);
            organizationService.updateSubscription(organizationId, newSubscription);

            // Implement later: Process payment with proration

            log.info("[UseCase] Organization {} successfully upgraded to plan {}", organizationId,
                    request.getNewPlanId());
            return responseUtils.success(newSubscription);
        } catch (AppException e) {
            log.error("Error upgrading subscription for organization {}: {}", organizationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when upgrading subscription for organization {}: {}", organizationId,
                    e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> downgradeSubscription(Long organizationId, DowngradeSubscriptionRequest request,
            Long requestedBy) {
        try {
            log.info("[UseCase] Organization {} downgrading subscription to plan {}", organizationId,
                    request.getNewPlanId());
            if (organizationId == null || requestedBy == null) {
                return responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            }

            var currentSubscription = subscriptionService.getActiveSubscription(organizationId);
            var currentPlan = subscriptionPlanService.getPlanById(currentSubscription.getSubscriptionPlanId());
            var newPlan = subscriptionPlanService.getPlanById(request.getNewPlanId());
            if (!newPlan.isAvailable()) {
                throw new AppException(Constants.ErrorMessage.PLAN_NOT_ACTIVE);
            }

            var newSubscription = subscriptionService.downgradeSubscription(organizationId, request,
                    requestedBy, currentPlan, newPlan);
            organizationService.updateSubscription(organizationId, newSubscription);

            // Implement later: Send notification

            log.info("[UseCase] Organization {} scheduled downgrade to plan {}", organizationId,
                    request.getNewPlanId());
            return responseUtils.success(newSubscription);
        } catch (AppException e) {
            log.error("Error downgrading subscription for organization {}: {}", organizationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when downgrading subscription for organization {}: {}", organizationId,
                    e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> cancelSubscription(Long organizationId, CancelSubscriptionRequest request,
            Long cancelledBy) {
        try {
            log.info("[UseCase] Organization {} cancelling subscription", organizationId);
            if (organizationId == null || cancelledBy == null) {
                return responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            }

            subscriptionService.cancelSubscription(organizationId, request, cancelledBy);

            // Implement later: Send notification

            log.info("[UseCase] Organization {} successfully cancelled subscription", organizationId);
            return responseUtils.success("Subscription cancelled successfully");
        } catch (AppException e) {
            log.error("Error cancelling subscription for organization {}: {}", organizationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when cancelling subscription for organization {}: {}", organizationId,
                    e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> renewSubscription(Long organizationId, Long renewedBy) {
        try {
            log.info("[UseCase] Organization {} renewing subscription", organizationId);
            if (organizationId == null || renewedBy == null) {
                return responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            }

            var organization = organizationService.getOrganizationById(organizationId);
            var newSubscription = subscriptionService.renewSubscription(
                    organizationId,
                    organization.getSubscriptionId(),
                    renewedBy);

            organizationService.updateSubscription(organizationId, newSubscription);

            // Implement later: Process payment
            // Send notification

            log.info("[UseCase] Organization {} successfully renewed subscription", organizationId);
            return responseUtils.success(newSubscription);
        } catch (AppException e) {
            log.error("Error renewing subscription for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when renewing subscription for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> activateSubscription(Long subscriptionId, Long activatedBy) {
        try {
            log.info("[UseCase] Activating subscription {}", subscriptionId);

            var subscription = subscriptionService.activateSubscription(subscriptionId, activatedBy);
            var organization = organizationService.getOrganizationById(subscription.getOrganizationId());
            organizationService.updateSubscription(organization.getId(), subscription);

            var orgOwner = userService.getUserById(organization.getOwnerId());

            // Auto-grant module access to organization owner
            try {
                var planModules = subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId());

                for (var planModule : planModules) {
                    var module = moduleService.getModuleByIdFromCache(planModule.getModuleId());
                    if (module == null) {
                        log.error("Module ID {} not found. Skipping auto-grant.", planModule.getModuleId());
                        continue;
                    }

                    if (planModule.getIsIncluded()) {
                        userModuleAccessService.registerUserToModuleWithExpiration(
                                organization.getOwnerId(),
                                planModule.getModuleId(),
                                subscription.getOrganizationId(),
                                activatedBy,
                                subscription.getEndDate());
                    }

                    List<RoleEntity> roles = roleService.getRolesByModuleId(planModule.getModuleId());
                    if (CollectionUtils.isEmpty(roles)) {
                        log.error("No roles found for module ID {}. Check why?", planModule.getModuleId());
                        continue;
                    }
                    combineRoleService.assignRolesToUser(orgOwner, roles);
                }
                log.info("Auto-granted {} modules to organization owner", planModules.size());
            } catch (Exception e) {
                log.error("Error auto-granting modules to owner: {}", e.getMessage());
            }

            log.info("[UseCase] Successfully activated subscription {}", subscriptionId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error activating subscription {}: {}", subscriptionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when activating subscription {}: {}", subscriptionId, e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> rejectSubscription(Long subscriptionId, RejectSubscriptionRequest request,
            Long rejectedBy) {
        try {
            log.info("[UseCase] Rejecting subscription {}", subscriptionId);

            subscriptionService.rejectSubscription(subscriptionId, request.getReason(), rejectedBy);

            log.info("[UseCase] Successfully rejected subscription {}", subscriptionId);
            return responseUtils.success("Subscription rejected successfully");
        } catch (AppException e) {
            log.error("Error rejecting subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when rejecting subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> extendTrial(Long subscriptionId, ExtendTrialRequest request, Long extendedBy) {
        try {
            log.info("[UseCase] Extending trial for subscription {} by {} days", subscriptionId,
                    request.getAdditionalDays());
            if (extendedBy == null) {
                return responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            }

            var subscription = subscriptionService.extendTrial(subscriptionId, request.getAdditionalDays(),
                    extendedBy);

            log.info("[UseCase] Successfully extended trial for subscription {}", subscriptionId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error extending trial for subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when extending trial for subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> expireSubscription(Long subscriptionId) {
        try {
            log.info("[UseCase] Expiring subscription {}", subscriptionId);

            var subscription = subscriptionService.getSubscriptionById(subscriptionId);
            subscriptionService.expireSubscription(subscriptionId);

            var planModules = subscriptionPlanService.getPlanModules(subscription.getSubscriptionPlanId());

            // Revoke module access for all users in organization
            try {
                int usersRevoked = 0;
                for (var planModule : planModules) {
                    if (planModule.getIsIncluded()) {
                        var users = userModuleAccessService.getUsersWithModuleAccess(
                                planModule.getModuleId(), subscription.getOrganizationId());
                        List<RoleEntity> roles = roleService.getRolesByModuleId(planModule.getModuleId());

                        for (var userAccess : users) {
                            userModuleAccessService.revokeUserModuleAccess(
                                    userAccess.getUserId(),
                                    planModule.getModuleId(),
                                    subscription.getOrganizationId());
                            var user = userService.getUserById(userAccess.getUserId());
                            if (user != null) {
                                combineRoleService.removeRolesFromUser(user, roles);
                            }
                            usersRevoked++;
                        }

                    }
                }
                log.info("Revoked module access for {} users", usersRevoked);
            } catch (Exception e) {
                log.error("Error revoking module access: {}", e.getMessage());
            }

            log.info("[UseCase] Successfully expired subscription {}", subscriptionId);
            return responseUtils.success("Subscription expired successfully");
        } catch (AppException e) {
            log.error("Error expiring subscription {}: {}", subscriptionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when expiring subscription {}: {}", subscriptionId, e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getActiveSubscription(Long organizationId) {
        try {
            if (organizationId == null) {
                return responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            }
            var subscription = subscriptionService.getActiveSubscription(organizationId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error getting active subscription for organization {}: {}", organizationId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting active subscription for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getSubscriptionById(Long subscriptionId) {
        try {
            var subscription = subscriptionService.getSubscriptionById(subscriptionId);
            return responseUtils.success(subscription);
        } catch (AppException e) {
            log.error("Error getting subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting subscription {}: {}", subscriptionId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getSubscriptionHistory(Long organizationId) {
        try {
            var subscriptions = subscriptionService.getSubscriptionHistory(organizationId);
            return responseUtils.success(subscriptions);
        } catch (Exception e) {
            log.error("Unexpected error when getting subscription history for organization {}: {}", organizationId,
                    e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getAllSubscriptions(GetSubscriptionParams params) {
        try {
            var pairSubscriptions = subscriptionService.getAllSubscriptions(params);
            var subscriptionEntities = pairSubscriptions.getFirst();

            var allPlans = subscriptionPlanService.getAllPlans();
            List<Long> orgIds = subscriptionEntities.stream()
                    .map(OrganizationSubscriptionEntity::getOrganizationId)
                    .distinct()
                    .toList();
            var allOrgs = organizationService.getOrganizationsByIds(orgIds);

            var subscriptionDtos = subscriptionEntities.stream()
                    .map(sub -> {
                        var org = allOrgs.stream()
                                .filter(o -> o.getId().equals(sub.getOrganizationId()))
                                .findFirst()
                                .orElse(null);
                        var plan = allPlans.stream()
                                .filter(p -> p.getId().equals(sub.getSubscriptionPlanId()))
                                .findFirst()
                                .orElse(null);
                        var dto = subscriptionMapper.toSubscriptionResponse(sub,
                                org != null ? org.getName() : "Unknown Organization",
                                plan != null ? plan.getPlanName() : "Unknown Plan");
                        return dto;
                    }).toList();

            var result = paginationUtils.getResponse(pairSubscriptions.getSecond(), params.getPage(),
                    params.getPageSize(), subscriptionDtos);
            return responseUtils.success(result);
        } catch (Exception e) {
            log.error("Unexpected error when getting all subscriptions: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

}
