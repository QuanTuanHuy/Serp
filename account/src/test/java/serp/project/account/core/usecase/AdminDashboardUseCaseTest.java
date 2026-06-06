/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardResponse;
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

@ExtendWith(MockitoExtension.class)
class AdminDashboardUseCaseTest {
    @Mock
    private IOrganizationService organizationService;
    @Mock
    private IUserService userService;
    @Mock
    private ISubscriptionService subscriptionService;
    @Mock
    private ISubscriptionPlanService subscriptionPlanService;
    @Mock
    private IModuleService moduleService;
    @Mock
    private IMenuDisplayService menuDisplayService;

    private final ResponseUtils responseUtils = new ResponseUtils();

    @Test
    void getDashboardAggregatesAccountOperationsData() {
        var adminDashboardUseCase = new AdminDashboardUseCase(
                organizationService,
                userService,
                subscriptionService,
                subscriptionPlanService,
                moduleService,
                menuDisplayService,
                responseUtils);

        var recentOrganization = OrganizationEntity.builder()
                .id(10L)
                .name("Acme")
                .code("ACME")
                .status(OrganizationStatus.ACTIVE)
                .createdAt(1_700_000_000_000L)
                .build();

        when(organizationService.countOrganizations()).thenReturn(4L);
        when(organizationService.countOrganizationsByStatus(OrganizationStatus.ACTIVE)).thenReturn(3L);
        when(organizationService.countOrganizationsByStatus(OrganizationStatus.TRIAL)).thenReturn(0L);
        when(organizationService.countOrganizationsByStatus(OrganizationStatus.SUSPENDED)).thenReturn(1L);
        when(organizationService.countOrganizationsByStatus(OrganizationStatus.EXPIRED)).thenReturn(0L);
        when(organizationService.countOrganizationsByStatus(OrganizationStatus.CLOSED)).thenReturn(0L);
        when(organizationService.getRecentOrganizations(5)).thenReturn(List.of(recentOrganization));

        when(userService.countUsers()).thenReturn(12L);
        when(userService.countUsersByStatus(UserStatus.ACTIVE)).thenReturn(10L);
        when(userService.countUsersByStatus(UserStatus.SUSPENDED)).thenReturn(2L);
        when(userService.countUsersByOrganizationIds(List.of(10L))).thenReturn(Map.of(10L, 5L));

        when(subscriptionService.countSubscriptions()).thenReturn(6L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.ACTIVE)).thenReturn(3L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.TRIAL)).thenReturn(1L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING)).thenReturn(1L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING_UPGRADE)).thenReturn(1L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.EXPIRED)).thenReturn(0L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.CANCELLED)).thenReturn(0L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PAYMENT_FAILED)).thenReturn(0L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.GRACE_PERIOD)).thenReturn(0L);
        when(subscriptionService.countSubscriptionsEndingSoon(anyLong(), anyLong())).thenReturn(2L);
        when(subscriptionService.countTrialsEndingSoon(anyLong(), anyLong())).thenReturn(1L);
        when(subscriptionService.getLatestSubscriptionStatusByOrganizationIds(List.of(10L)))
                .thenReturn(Map.of(10L, SubscriptionStatus.ACTIVE.name()));

        when(subscriptionPlanService.countPlans()).thenReturn(3L);
        when(subscriptionPlanService.countActivePlans()).thenReturn(2L);
        when(moduleService.countModules()).thenReturn(5L);
        when(moduleService.countAvailableModules()).thenReturn(4L);
        when(menuDisplayService.countMenuDisplays()).thenReturn(9L);
        when(menuDisplayService.countVisibleMenuDisplays()).thenReturn(7L);

        GeneralResponse<?> response = adminDashboardUseCase.getDashboard();

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isInstanceOf(AdminDashboardResponse.class);
        var dashboard = (AdminDashboardResponse) response.getData();
        assertThat(dashboard.getMetrics().getTotalOrganizations()).isEqualTo(4L);
        assertThat(dashboard.getMetrics().getPendingSubscriptions()).isEqualTo(2L);
        assertThat(dashboard.getActionQueue().getSubscriptionsEndingSoon()).isEqualTo(2L);
        assertThat(dashboard.getRecentOrganizations()).hasSize(1);
        assertThat(dashboard.getRecentOrganizations().getFirst().getUserCount()).isEqualTo(5L);
        assertThat(dashboard.getRecentOrganizations().getFirst().getSubscriptionStatus()).isEqualTo("ACTIVE");
        assertThat(dashboard.getConfigurationCoverage().getInactivePlans()).isEqualTo(1L);
        assertThat(dashboard.getConfigurationCoverage().getUnavailableModules()).isEqualTo(1L);
        assertThat(dashboard.getConfigurationCoverage().getHiddenMenuDisplays()).isEqualTo(2L);
    }
}
