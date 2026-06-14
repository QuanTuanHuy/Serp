/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchResponse;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.AdminGlobalSearchType;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.RoleType;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.port.store.IModulePort;
import serp.project.account.core.port.store.IOrganizationPort;
import serp.project.account.core.port.store.IRolePort;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class AdminSearchUseCaseTest {
    @Mock
    private IOrganizationPort organizationPort;
    @Mock
    private IUserPort userPort;
    @Mock
    private IRolePort rolePort;
    @Mock
    private IModulePort modulePort;

    private final ResponseUtils responseUtils = new ResponseUtils();

    @Test
    void searchShouldTrimQueryClampLimitAndReturnFourGroups() {
        var useCase = new AdminSearchUseCase(
                organizationPort,
                userPort,
                rolePort,
                modulePort,
                responseUtils);

        when(organizationPort.getOrganizations(any(GetOrganizationParams.class))).thenReturn(Pair.of(
                List.of(OrganizationEntity.builder()
                        .id(1L)
                        .name("Acme Corp")
                        .code("ACME")
                        .email("ops@acme.test")
                        .status(OrganizationStatus.ACTIVE)
                        .build()),
                12L));
        when(userPort.getUsers(any(GetUserParams.class))).thenReturn(Pair.of(
                3L,
                List.of(UserEntity.builder()
                        .id(2L)
                        .firstName("Alice")
                        .lastName("Nguyen")
                        .email("alice@acme.test")
                        .status(UserStatus.ACTIVE)
                        .build())));
        when(rolePort.searchRoles("acme", 10)).thenReturn(Pair.of(
                List.of(RoleEntity.builder()
                        .id(3L)
                        .name("Account Admin")
                        .description("Manages account data")
                        .scope(RoleScope.SYSTEM)
                        .roleType(RoleType.ADMIN)
                        .build()),
                1L));
        when(modulePort.searchModules("acme", 10)).thenReturn(Pair.of(
                List.of(ModuleEntity.builder()
                        .id(4L)
                        .moduleName("Account")
                        .code("ACCOUNT")
                        .description("Account administration")
                        .status(ModuleStatus.ACTIVE)
                        .build()),
                1L));

        GeneralResponse<?> response = useCase.search("  acme  ", 99);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isInstanceOf(AdminGlobalSearchResponse.class);
        var data = (AdminGlobalSearchResponse) response.getData();
        assertThat(data.getQuery()).isEqualTo("acme");
        assertThat(data.getLimit()).isEqualTo(10);
        assertThat(data.getGroups()).extracting("type").containsExactly(
                AdminGlobalSearchType.ORGANIZATION,
                AdminGlobalSearchType.USER,
                AdminGlobalSearchType.ROLE,
                AdminGlobalSearchType.MODULE);
        assertThat(data.getGroups().getFirst().getItems().getFirst().getUrl())
                .isEqualTo("/admin/organizations?search=acme");
        assertThat(data.getGroups().get(1).getItems().getFirst().getTitle())
                .isEqualTo("Alice Nguyen");
        assertThat(data.getGroups().get(2).getItems().getFirst().getSubtitle())
                .isEqualTo("SYSTEM - ADMIN");
        assertThat(data.getGroups().get(3).getItems().getFirst().getSubtitle())
                .isEqualTo("ACCOUNT - ACTIVE");

        ArgumentCaptor<GetOrganizationParams> orgParams = ArgumentCaptor.forClass(GetOrganizationParams.class);
        verify(organizationPort).getOrganizations(orgParams.capture());
        assertThat(orgParams.getValue().getSearch()).isEqualTo("acme");
        assertThat(orgParams.getValue().getPage()).isZero();
        assertThat(orgParams.getValue().getPageSize()).isEqualTo(10);

        ArgumentCaptor<GetUserParams> userParams = ArgumentCaptor.forClass(GetUserParams.class);
        verify(userPort).getUsers(userParams.capture());
        assertThat(userParams.getValue().getSearch()).isEqualTo("acme");
        assertThat(userParams.getValue().getPage()).isZero();
        assertThat(userParams.getValue().getPageSize()).isEqualTo(10);

        verify(rolePort).searchRoles(eq("acme"), eq(10));
        verify(modulePort).searchModules(eq("acme"), eq(10));
    }

    @Test
    void searchShouldReturnBadRequestForBlankQuery() {
        var useCase = new AdminSearchUseCase(
                organizationPort,
                userPort,
                rolePort,
                modulePort,
                responseUtils);

        GeneralResponse<?> response = useCase.search("   ", 5);

        assertThat(response.getCode()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo(Constants.ErrorMessage.SEARCH_QUERY_REQUIRED);
    }

    @Test
    void searchShouldUseDefaultLimitWhenLimitIsNullOrTooLow() {
        var useCase = new AdminSearchUseCase(
                organizationPort,
                userPort,
                rolePort,
                modulePort,
                responseUtils);

        when(organizationPort.getOrganizations(any(GetOrganizationParams.class))).thenReturn(Pair.of(List.of(), 0L));
        when(userPort.getUsers(any(GetUserParams.class))).thenReturn(Pair.of(0L, List.of()));
        when(rolePort.searchRoles("sales", 5)).thenReturn(Pair.of(List.of(), 0L));
        when(modulePort.searchModules("sales", 5)).thenReturn(Pair.of(List.of(), 0L));

        GeneralResponse<?> response = useCase.search("sales", 0);

        var data = (AdminGlobalSearchResponse) response.getData();
        assertThat(data.getLimit()).isEqualTo(5);
        assertThat(data.getGroups()).hasSize(4);
    }
}
