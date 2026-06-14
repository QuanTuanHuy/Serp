/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchResponse;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.SettingsGlobalSearchType;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.service.IDepartmentService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class SettingsSearchUseCaseTest {
    @Mock
    private IUserService userService;
    @Mock
    private IDepartmentService departmentService;
    @Mock
    private ISubscriptionService subscriptionService;
    @Mock
    private ISubscriptionPlanService subscriptionPlanService;
    @Mock
    private IModuleService moduleService;

    private final ResponseUtils responseUtils = new ResponseUtils();

    @Test
    void searchShouldTrimQueryClampLimitScopeToOrganizationAndReturnThreeGroups() {
        var useCase = new SettingsSearchUseCase(
                userService,
                departmentService,
                subscriptionService,
                subscriptionPlanService,
                moduleService,
                responseUtils);

        when(userService.getUsers(any(GetUserParams.class))).thenReturn(Pair.of(
                1L,
                List.of(UserEntity.builder()
                        .id(10L)
                        .firstName("Mai")
                        .lastName("Nguyen")
                        .email("mai@example.test")
                        .status(UserStatus.ACTIVE)
                        .build())));
        when(departmentService.getDepartments(any(GetDepartmentParams.class))).thenReturn(Pair.of(
                List.of(DepartmentEntity.builder()
                        .id(20L)
                        .organizationId(7L)
                        .name("Marketing")
                        .code("MKT")
                        .isActive(true)
                        .build()),
                1L));
        when(subscriptionService.getActiveOrPendingUpgrade(7L)).thenReturn(
                OrganizationSubscriptionEntity.builder()
                        .id(30L)
                        .organizationId(7L)
                        .subscriptionPlanId(40L)
                        .build());
        when(subscriptionPlanService.getPlanModules(40L)).thenReturn(List.of(
                SubscriptionPlanModuleEntity.builder().moduleId(50L).isIncluded(true).build(),
                SubscriptionPlanModuleEntity.builder().moduleId(51L).isIncluded(false).build()));
        when(moduleService.getModulesByIds(List.of(50L))).thenReturn(List.of(
                ModuleEntity.builder()
                        .id(50L)
                        .moduleName("Marketing Automation")
                        .code("MKT_AUTO")
                        .description("Marketing tools")
                        .status(ModuleStatus.ACTIVE)
                        .build()));

        GeneralResponse<?> response = useCase.search(7L, "  marketing  ", 99);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isInstanceOf(SettingsGlobalSearchResponse.class);
        var data = (SettingsGlobalSearchResponse) response.getData();
        assertThat(data.getQuery()).isEqualTo("marketing");
        assertThat(data.getLimit()).isEqualTo(10);
        assertThat(data.getGroups()).extracting("type").containsExactly(
                SettingsGlobalSearchType.USER,
                SettingsGlobalSearchType.DEPARTMENT,
                SettingsGlobalSearchType.MODULE);
        assertThat(data.getGroups().get(0).getItems().get(0).getUrl())
                .isEqualTo("/settings/users?search=marketing");
        assertThat(data.getGroups().get(1).getItems().get(0).getUrl())
                .isEqualTo("/settings/departments?search=marketing");
        assertThat(data.getGroups().get(2).getItems().get(0).getUrl())
                .isEqualTo("/settings/modules?search=marketing");

        ArgumentCaptor<GetUserParams> userParams = ArgumentCaptor.forClass(GetUserParams.class);
        verify(userService).getUsers(userParams.capture());
        assertThat(userParams.getValue().getOrganizationId()).isEqualTo(7L);
        assertThat(userParams.getValue().getSearch()).isEqualTo("marketing");
        assertThat(userParams.getValue().getPage()).isZero();
        assertThat(userParams.getValue().getPageSize()).isEqualTo(10);

        ArgumentCaptor<GetDepartmentParams> departmentParams = ArgumentCaptor.forClass(GetDepartmentParams.class);
        verify(departmentService).getDepartments(departmentParams.capture());
        assertThat(departmentParams.getValue().getOrganizationId()).isEqualTo(7L);
        assertThat(departmentParams.getValue().getSearch()).isEqualTo("marketing");
        assertThat(departmentParams.getValue().getPage()).isZero();
        assertThat(departmentParams.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void searchShouldReturnBadRequestForBlankQuery() {
        var useCase = new SettingsSearchUseCase(
                userService,
                departmentService,
                subscriptionService,
                subscriptionPlanService,
                moduleService,
                responseUtils);

        GeneralResponse<?> response = useCase.search(7L, "   ", 5);

        assertThat(response.getCode()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo(Constants.ErrorMessage.SEARCH_QUERY_REQUIRED);
    }

    @Test
    void searchShouldUseDefaultLimitWhenLimitIsNullOrTooLow() {
        var useCase = new SettingsSearchUseCase(
                userService,
                departmentService,
                subscriptionService,
                subscriptionPlanService,
                moduleService,
                responseUtils);

        when(userService.getUsers(any(GetUserParams.class))).thenReturn(Pair.of(0L, List.of()));
        when(departmentService.getDepartments(any(GetDepartmentParams.class))).thenReturn(Pair.of(List.of(), 0L));
        when(subscriptionService.getActiveOrPendingUpgrade(7L)).thenReturn(
                OrganizationSubscriptionEntity.builder()
                        .organizationId(7L)
                        .subscriptionPlanId(40L)
                        .build());
        when(subscriptionPlanService.getPlanModules(40L)).thenReturn(List.of());
        when(moduleService.getModulesByIds(List.of())).thenReturn(List.of());

        GeneralResponse<?> response = useCase.search(7L, "sales", 0);

        var data = (SettingsGlobalSearchResponse) response.getData();
        assertThat(data.getLimit()).isEqualTo(5);
        assertThat(data.getGroups()).hasSize(3);
    }
}
