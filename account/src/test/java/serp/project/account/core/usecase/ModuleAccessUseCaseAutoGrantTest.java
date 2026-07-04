/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.dto.request.UpdateModuleAccessSettingsRequest;
import serp.project.account.core.domain.dto.response.AutoGrantBackfillResponse;
import serp.project.account.core.domain.dto.response.OrgModuleAccessResponse;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.OrganizationModuleAccessSettingEntity;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.INotificationService;
import serp.project.account.core.service.IOrganizationModuleAccessSettingService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.core.service.IUserService;
import serp.project.account.core.usecase.support.ModuleAutoGrantService;
import serp.project.account.core.usecase.support.UserSyncPublisher;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class ModuleAccessUseCaseAutoGrantTest {

    @Mock private ISubscriptionService subscriptionService;
    @Mock private ISubscriptionPlanService subscriptionPlanService;
    @Mock private IUserModuleAccessService userModuleAccessService;
    @Mock private IUserService userService;
    @Mock private IModuleService moduleService;
    @Mock private IRoleService roleService;
    @Mock private ICombineRoleService combineRoleService;
    @Mock private IKeycloakUserService keycloakUserService;
    @Mock private INotificationService notificationService;
    @Mock private UserSyncPublisher userSyncPublisher;
    @Mock private IOrganizationModuleAccessSettingService settingService;
    @Mock private ModuleAutoGrantService moduleAutoGrantService;

    @Spy
    private ResponseUtils responseUtils = new ResponseUtils();

    @InjectMocks
    private ModuleAccessUseCase useCase;

    @Test
    void getAccessibleModulesShouldIncludeAutoGrantSetting() {
        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription());
        when(subscriptionPlanService.getPlanById(100L)).thenReturn(SubscriptionPlanEntity.builder().id(100L).build());
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule(20L, true)));
        when(moduleService.getAllModules()).thenReturn(List.of(module(20L), module(21L)));
        when(roleService.getAllRoles()).thenReturn(List.of());
        when(userService.countUsersByOrganizationId(10L)).thenReturn(3);
        when(userModuleAccessService.countActiveUsers(20L, 10L)).thenReturn(1);
        when(settingService.getByOrganizationId(10L)).thenReturn(List.of(
                OrganizationModuleAccessSettingEntity.builder()
                        .organizationId(10L)
                        .moduleId(20L)
                        .autoGrantToNewUsers(true)
                        .build()));

        var response = useCase.getAccessibleModulesForOrganization(10L);

        @SuppressWarnings("unchecked")
        var modules = (List<OrgModuleAccessResponse>) response.getData();
        Map<Long, OrgModuleAccessResponse> byModuleId = modules.stream()
                .collect(java.util.stream.Collectors.toMap(OrgModuleAccessResponse::getModuleId, m -> m));
        assertEquals(true, byModuleId.get(20L).getIsAutoGrantToNewUsers());
        assertEquals(false, byModuleId.get(21L).getIsAutoGrantToNewUsers());
    }

    @Test
    void updateModuleAccessSettingsShouldPersistPolicyWhenModuleIsInPlan() {
        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription());
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule(20L, true)));
        when(settingService.upsertAutoGrantToNewUsers(10L, 20L, true, 99L)).thenReturn(
                OrganizationModuleAccessSettingEntity.builder()
                        .organizationId(10L)
                        .moduleId(20L)
                        .autoGrantToNewUsers(true)
                        .updatedBy(99L)
                        .build());

        var response = useCase.updateModuleAccessSettings(
                10L,
                20L,
                UpdateModuleAccessSettingsRequest.builder().autoGrantToNewUsers(true).build(),
                99L);

        assertEquals(200, response.getCode());
    }

    @Test
    void backfillAutoGrantShouldReturnSummary() {
        var summary = AutoGrantBackfillResponse.empty(10L, 20L);
        summary.markGranted();
        when(moduleAutoGrantService.backfillExistingUsers(10L, 20L, 99L)).thenReturn(summary);

        var response = useCase.backfillAutoGrant(10L, 20L, 99L);

        assertEquals(200, response.getCode());
        assertEquals(summary, response.getData());
    }

    private OrganizationSubscriptionEntity subscription() {
        return OrganizationSubscriptionEntity.builder()
                .organizationId(10L)
                .subscriptionPlanId(100L)
                .endDate(999L)
                .build();
    }

    private SubscriptionPlanModuleEntity planModule(Long moduleId, Boolean included) {
        return SubscriptionPlanModuleEntity.builder()
                .subscriptionPlanId(100L)
                .moduleId(moduleId)
                .isIncluded(included)
                .build();
    }

    private ModuleEntity module(Long id) {
        return ModuleEntity.builder()
                .id(id)
                .moduleName("Module " + id)
                .code("MOD" + id)
                .description("Module " + id)
                .status(ModuleStatus.ACTIVE)
                .build();
    }
}
