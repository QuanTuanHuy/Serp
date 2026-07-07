/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import serp.project.account.core.domain.dto.request.AddModuleToPlanRequest;
import serp.project.account.core.domain.dto.request.CreateSubscriptionPlanRequest;
import serp.project.account.core.domain.dto.request.UpdateSubscriptionPlanRequest;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.LicenseType;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanUseCaseTest {

    private static final Long SUBSCRIPTION_END_DATE = 4_102_444_800_000L;

    @Mock private ISubscriptionPlanService subscriptionPlanService;
    @Mock private ISubscriptionService subscriptionService;
    @Mock private ICombineRoleService combineRoleService;
    @Mock private IRoleService roleService;
    @Mock private IUserService userService;
    @Mock private IUserModuleAccessService userModuleAccessService;
    @Mock private IOrganizationService organizationService;
    @Mock private IModuleService moduleService;
    @Mock private AsyncTaskExecutor asyncTaskExecutor;

    private SubscriptionPlanUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubscriptionPlanUseCase(
                subscriptionPlanService,
                subscriptionService,
                combineRoleService,
                roleService,
                userService,
                userModuleAccessService,
                organizationService,
                moduleService,
                new ResponseUtils(),
                new PaginationUtils(),
                asyncTaskExecutor);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void createPlanShouldCreateModulesFromRequest() {
        var request = CreateSubscriptionPlanRequest.builder()
                .modules(List.of(
                        CreateSubscriptionPlanRequest.PlanModuleDto.builder()
                                .moduleId(10L)
                                .licenseType(LicenseType.BASIC)
                                .isIncluded(true)
                                .build(),
                        CreateSubscriptionPlanRequest.PlanModuleDto.builder()
                                .moduleId(20L)
                                .licenseType(LicenseType.PROFESSIONAL)
                                .isIncluded(false)
                                .maxUsersPerModule(5)
                                .build()))
                .build();
        when(subscriptionPlanService.createPlan(request, 99L)).thenReturn(plan());
        when(moduleService.getModuleByIdFromCache(10L)).thenReturn(module(10L));
        when(moduleService.getModuleByIdFromCache(20L)).thenReturn(module(20L));

        useCase.createPlan(request, 99L);

        verify(subscriptionPlanService).addModuleToPlan(100L, 10L, "BASIC", true, null, 99L);
        verify(subscriptionPlanService).addModuleToPlan(100L, 20L, "PROFESSIONAL", false, 5, 99L);
        verify(asyncTaskExecutor, never()).execute(any(Runnable.class));
    }

    @Test
    void updatePlanShouldLeaveModulesUnchangedWhenModulesRequestIsNull() {
        var request = UpdateSubscriptionPlanRequest.builder()
                .planName("Updated plan")
                .build();
        when(subscriptionPlanService.updatePlan(100L, request, 99L)).thenReturn(plan());

        useCase.updatePlan(100L, request, 99L);

        verify(subscriptionPlanService, never()).getPlanModules(100L);
        verify(subscriptionPlanService, never()).addModuleToPlan(
                any(), any(), any(), any(), any(), any());
        verify(subscriptionPlanService, never()).removeModuleFromPlan(any(), any());
        verify(subscriptionPlanService, never()).updatePlanModule(any());
    }

    @Test
    void updatePlanShouldReplaceModulesAndScheduleAccessChanges() {
        var request = UpdateSubscriptionPlanRequest.builder()
                .modules(List.of(
                        UpdateSubscriptionPlanRequest.PlanModuleDto.builder()
                                .moduleId(10L)
                                .licenseType(LicenseType.BASIC)
                                .isIncluded(false)
                                .maxUsersPerModule(3)
                                .build(),
                        UpdateSubscriptionPlanRequest.PlanModuleDto.builder()
                                .moduleId(20L)
                                .licenseType(LicenseType.PROFESSIONAL)
                                .isIncluded(true)
                                .maxUsersPerModule(7)
                                .build(),
                        UpdateSubscriptionPlanRequest.PlanModuleDto.builder()
                                .moduleId(30L)
                                .licenseType(LicenseType.ENTERPRISE)
                                .isIncluded(true)
                                .maxUsersPerModule(9)
                                .build()))
                .build();
        when(subscriptionPlanService.updatePlan(100L, request, 99L)).thenReturn(plan());
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(
                planModule(10L, true, LicenseType.BASIC, null),
                planModule(20L, false, LicenseType.BASIC, null),
                planModule(40L, true, LicenseType.BASIC, null)));
        when(moduleService.getModuleByIdFromCache(10L)).thenReturn(module(10L));
        when(moduleService.getModuleByIdFromCache(20L)).thenReturn(module(20L));
        when(moduleService.getModuleByIdFromCache(30L)).thenReturn(module(30L));

        useCase.updatePlan(100L, request, 99L);

        verify(subscriptionPlanService).addModuleToPlan(100L, 30L, "ENTERPRISE", true, 9, 99L);
        verify(subscriptionPlanService).removeModuleFromPlan(100L, 40L);
        verify(subscriptionPlanService).updatePlanModule(argThat(planModule ->
                planModule.getModuleId().equals(10L)
                        && Boolean.FALSE.equals(planModule.getIsIncluded())
                        && planModule.getLicenseType() == LicenseType.BASIC
                        && planModule.getMaxUsersPerModule().equals(3)));
        verify(subscriptionPlanService).updatePlanModule(argThat(planModule ->
                planModule.getModuleId().equals(20L)
                        && Boolean.TRUE.equals(planModule.getIsIncluded())
                        && planModule.getLicenseType() == LicenseType.PROFESSIONAL
                        && planModule.getMaxUsersPerModule().equals(7)));
        verify(asyncTaskExecutor, times(4)).execute(any(Runnable.class));
    }

    @Test
    void addModuleToPlanShouldScheduleExistingSubscriptionBackfillAfterCommit() {
        when(moduleService.getModuleByIdFromCache(20L)).thenReturn(module(20L));
        when(subscriptionPlanService.addModuleToPlan(100L, 20L, "BASIC", true, null, 99L))
                .thenReturn(planModule());
        TransactionSynchronizationManager.initSynchronization();

        useCase.addModuleToPlan(100L, addModuleRequest(), 99L);

        verify(asyncTaskExecutor, never()).execute(any(Runnable.class));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(asyncTaskExecutor).execute(any(Runnable.class));
    }

    @Test
    void addModuleAccessForExistedSubscriptionsShouldAssignOnlyAutoAssignedRoles() {
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(asyncTaskExecutor).execute(any(Runnable.class));
        var owner = UserEntity.builder()
                .id(1L)
                .status(UserStatus.ACTIVE)
                .build();
        RoleEntity defaultRole = RoleEntity.builder()
                .id(10L)
                .moduleId(20L)
                .isDefault(true)
                .build();
        RoleEntity adminRole = RoleEntity.builder()
                .id(11L)
                .moduleId(20L)
                .isDefault(false)
                .build();
        when(subscriptionService.getSubscriptionsByPlanId(100L)).thenReturn(List.of(subscription()));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(defaultRole, adminRole));
        when(organizationService.getOrganizationById(10L)).thenReturn(organization());
        when(userService.getUserById(1L)).thenReturn(owner);

        useCase.addModuleAccessForExistedSubscriptions(100L, 20L);

        verify(userModuleAccessService).registerUserToModuleWithExpiration(
                1L,
                20L,
                10L,
                1L,
                subscription().getEndDate());
        verify(combineRoleService).assignRolesToUser(owner, List.of(defaultRole));
    }

    private AddModuleToPlanRequest addModuleRequest() {
        return AddModuleToPlanRequest.builder()
                .moduleId(20L)
                .licenseType(LicenseType.BASIC)
                .isIncluded(true)
                .build();
    }

    private SubscriptionPlanModuleEntity planModule() {
        return SubscriptionPlanModuleEntity.builder()
                .subscriptionPlanId(100L)
                .moduleId(20L)
                .isIncluded(true)
                .licenseType(LicenseType.BASIC)
                .build();
    }

    private SubscriptionPlanEntity plan() {
        return SubscriptionPlanEntity.builder()
                .id(100L)
                .build();
    }

    private SubscriptionPlanModuleEntity planModule(
            Long moduleId,
            Boolean isIncluded,
            LicenseType licenseType,
            Integer maxUsersPerModule) {
        return SubscriptionPlanModuleEntity.builder()
                .subscriptionPlanId(100L)
                .moduleId(moduleId)
                .isIncluded(isIncluded)
                .licenseType(licenseType)
                .maxUsersPerModule(maxUsersPerModule)
                .build();
    }

    private ModuleEntity module(Long id) {
        return ModuleEntity.builder()
                .id(id)
                .status(ModuleStatus.ACTIVE)
                .build();
    }

    private OrganizationSubscriptionEntity subscription() {
        return OrganizationSubscriptionEntity.builder()
                .id(500L)
                .organizationId(10L)
                .subscriptionPlanId(100L)
                .status(SubscriptionStatus.ACTIVE)
                .endDate(SUBSCRIPTION_END_DATE)
                .build();
    }

    private OrganizationEntity organization() {
        return OrganizationEntity.builder()
                .id(10L)
                .ownerId(1L)
                .build();
    }
}
