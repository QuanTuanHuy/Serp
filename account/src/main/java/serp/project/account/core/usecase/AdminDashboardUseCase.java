/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardActionQueueResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardConfigurationCoverageResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardMetricsResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardRecentOrganizationResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardStatusCountResponse;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.service.IMenuDisplayService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardUseCase {
    private static final int RECENT_ORGANIZATION_LIMIT = 5;
    private static final int ENDING_SOON_DAYS = 7;
    private static final String NO_SUBSCRIPTION = "NO_SUBSCRIPTION";

    private final IOrganizationService organizationService;
    private final IUserService userService;
    private final ISubscriptionService subscriptionService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final IModuleService moduleService;
    private final IMenuDisplayService menuDisplayService;
    private final ResponseUtils responseUtils;

    @Transactional(readOnly = true)
    public GeneralResponse<?> getDashboard() {
        try {
            long now = Instant.now().toEpochMilli();
            long endingSoon = Instant.now().plus(ENDING_SOON_DAYS, ChronoUnit.DAYS).toEpochMilli();

            long pendingSubscriptions = safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING))
                    + safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING_UPGRADE));

            var metrics = AdminDashboardMetricsResponse.builder()
                    .totalOrganizations(safe(organizationService.countOrganizations()))
                    .activeOrganizations(
                            safe(organizationService.countOrganizationsByStatus(OrganizationStatus.ACTIVE)))
                    .suspendedOrganizations(
                            safe(organizationService.countOrganizationsByStatus(OrganizationStatus.SUSPENDED)))
                    .expiredOrganizations(
                            safe(organizationService.countOrganizationsByStatus(OrganizationStatus.EXPIRED)))
                    .totalUsers(safe(userService.countUsers()))
                    .activeUsers(safe(userService.countUsersByStatus(UserStatus.ACTIVE)))
                    .suspendedUsers(safe(userService.countUsersByStatus(UserStatus.SUSPENDED)))
                    .totalSubscriptions(safe(subscriptionService.countSubscriptions()))
                    .activeSubscriptions(safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.ACTIVE)))
                    .trialSubscriptions(safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.TRIAL)))
                    .pendingSubscriptions(pendingSubscriptions)
                    .expiredSubscriptions(safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.EXPIRED)))
                    .build();

            var actionQueue = AdminDashboardActionQueueResponse.builder()
                    .pendingSubscriptions(pendingSubscriptions)
                    .subscriptionsEndingSoon(safe(subscriptionService.countSubscriptionsEndingSoon(now, endingSoon)))
                    .trialsEndingSoon(safe(subscriptionService.countTrialsEndingSoon(now, endingSoon)))
                    .suspendedOrganizations(metrics.getSuspendedOrganizations())
                    .expiredOrganizations(metrics.getExpiredOrganizations())
                    .build();

            var recentOrganizations = buildRecentOrganizations();
            long totalPlans = safe(subscriptionPlanService.countPlans());
            long activePlans = safe(subscriptionPlanService.countActivePlans());
            long totalModules = safe(moduleService.countModules());
            long availableModules = safe(moduleService.countAvailableModules());
            long totalMenuDisplays = safe(menuDisplayService.countMenuDisplays());
            long visibleMenuDisplays = safe(menuDisplayService.countVisibleMenuDisplays());

            var dashboard = AdminDashboardResponse.builder()
                    .metrics(metrics)
                    .actionQueue(actionQueue)
                    .recentOrganizations(recentOrganizations)
                    .organizationStatuses(buildOrganizationStatuses())
                    .subscriptionStatuses(buildSubscriptionStatuses())
                    .configurationCoverage(AdminDashboardConfigurationCoverageResponse.builder()
                            .totalPlans(totalPlans)
                            .activePlans(activePlans)
                            .inactivePlans(totalPlans - activePlans)
                            .totalModules(totalModules)
                            .availableModules(availableModules)
                            .unavailableModules(totalModules - availableModules)
                            .totalMenuDisplays(totalMenuDisplays)
                            .visibleMenuDisplays(visibleMenuDisplays)
                            .hiddenMenuDisplays(totalMenuDisplays - visibleMenuDisplays)
                            .build())
                    .generatedAt(now)
                    .build();

            return responseUtils.success(dashboard);
        } catch (Exception e) {
            log.error("Error building admin dashboard: {}", e.getMessage(), e);
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    private List<AdminDashboardRecentOrganizationResponse> buildRecentOrganizations() {
        List<OrganizationEntity> organizations = organizationService.getRecentOrganizations(RECENT_ORGANIZATION_LIMIT);
        List<Long> organizationIds = organizations.stream().map(OrganizationEntity::getId).toList();
        Map<Long, Long> userCounts = userService.countUsersByOrganizationIds(organizationIds);
        Map<Long, String> subscriptionStatuses =
                subscriptionService.getLatestSubscriptionStatusByOrganizationIds(organizationIds);

        return organizations.stream()
                .map(organization -> AdminDashboardRecentOrganizationResponse.builder()
                        .id(organization.getId())
                        .name(organization.getName())
                        .code(organization.getCode())
                        .status(organization.getStatus() != null ? organization.getStatus().name() : null)
                        .userCount(userCounts.getOrDefault(organization.getId(), 0L))
                        .subscriptionStatus(subscriptionStatuses.getOrDefault(organization.getId(), NO_SUBSCRIPTION))
                        .createdAt(organization.getCreatedAt())
                        .build())
                .toList();
    }

    private List<AdminDashboardStatusCountResponse> buildOrganizationStatuses() {
        return List.of(
                statusCount(OrganizationStatus.ACTIVE.name(),
                        organizationService.countOrganizationsByStatus(OrganizationStatus.ACTIVE)),
                statusCount(OrganizationStatus.TRIAL.name(),
                        organizationService.countOrganizationsByStatus(OrganizationStatus.TRIAL)),
                statusCount(OrganizationStatus.SUSPENDED.name(),
                        organizationService.countOrganizationsByStatus(OrganizationStatus.SUSPENDED)),
                statusCount(OrganizationStatus.EXPIRED.name(),
                        organizationService.countOrganizationsByStatus(OrganizationStatus.EXPIRED)),
                statusCount(OrganizationStatus.CLOSED.name(),
                        organizationService.countOrganizationsByStatus(OrganizationStatus.CLOSED)));
    }

    private List<AdminDashboardStatusCountResponse> buildSubscriptionStatuses() {
        return List.of(
                statusCount(SubscriptionStatus.ACTIVE.name(),
                        subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.ACTIVE)),
                statusCount(SubscriptionStatus.TRIAL.name(),
                        subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.TRIAL)),
                statusCount(SubscriptionStatus.PENDING.name(),
                        subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING)),
                statusCount(SubscriptionStatus.PENDING_UPGRADE.name(),
                        subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING_UPGRADE)),
                statusCount(SubscriptionStatus.EXPIRED.name(),
                        subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.EXPIRED)),
                statusCount(SubscriptionStatus.CANCELLED.name(),
                        subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.CANCELLED)),
                statusCount(SubscriptionStatus.PAYMENT_FAILED.name(),
                        subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PAYMENT_FAILED)),
                statusCount(SubscriptionStatus.GRACE_PERIOD.name(),
                        subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.GRACE_PERIOD)));
    }

    private AdminDashboardStatusCountResponse statusCount(String status, Long count) {
        return AdminDashboardStatusCountResponse.builder()
                .status(status)
                .count(safe(count))
                .build();
    }

    private long safe(Long value) {
        return value != null ? value : 0L;
    }
}
