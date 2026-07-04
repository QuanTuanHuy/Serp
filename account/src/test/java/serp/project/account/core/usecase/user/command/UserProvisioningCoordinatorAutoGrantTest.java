/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.user.command;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IUserService;
import serp.project.account.core.usecase.support.ModuleAutoGrantService;
import serp.project.account.core.usecase.support.OrganizationRoleResolver;
import serp.project.account.core.usecase.support.UserSyncPublisher;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.core.domain.dto.request.CreateKeycloakUserDto;

@ExtendWith(MockitoExtension.class)
class UserProvisioningCoordinatorAutoGrantTest {

    @Mock private IUserService userService;
    @Mock private IKeycloakUserService keycloakUserService;
    @Mock private IOrganizationService organizationService;
    @Mock private ICombineRoleService combineRoleService;
    @Mock private OrganizationRoleResolver organizationRoleResolver;
    @Mock private UserMapper userMapper;
    @Mock private UserSyncPublisher userSyncPublisher;
    @Mock private ModuleAutoGrantService moduleAutoGrantService;

    @InjectMocks
    private UserProvisioningCoordinator coordinator;

    @Test
    void createOrganizationUserShouldAutoGrantBeforeFinalSync() {
        var organization = OrganizationEntity.builder()
                .id(10L)
                .ownerId(77L)
                .build();
        var request = CreateUserForOrgRequest.builder()
                .email("new@example.com")
                .password("secret")
                .userType(UserType.ADMIN)
                .build();
        var user = UserEntity.builder()
                .id(1L)
                .email("new@example.com")
                .status(UserStatus.INACTIVE)
                .primaryOrganizationId(10L)
                .build();
        var activeUser = UserEntity.builder()
                .id(1L)
                .email("new@example.com")
                .status(UserStatus.ACTIVE)
                .primaryOrganizationId(10L)
                .keycloakId("kc-1")
                .build();
        var orgRole = RoleEntity.builder()
                .id(100L)
                .name("ROLE_ORG_MEMBER")
                .isDefault(true)
                .build();

        when(userService.createUser(10L, request)).thenReturn(user);
        when(userMapper.createUserMapper(user, 10L, "secret")).thenReturn(CreateKeycloakUserDto.builder().build());
        when(keycloakUserService.createUser(org.mockito.ArgumentMatchers.any())).thenReturn("kc-1");
        when(userService.updateUser(org.mockito.Mockito.eq(1L), org.mockito.ArgumentMatchers.any(UserEntity.class)))
                .thenReturn(activeUser);
        when(organizationRoleResolver.resolveRequestedOrAutoAssignedRoles(10L, request.getRoleIds()))
                .thenReturn(List.of(orgRole));

        coordinator.createOrganizationUser(organization, request);

        InOrder order = inOrder(combineRoleService, organizationService, moduleAutoGrantService, userSyncPublisher);
        order.verify(combineRoleService).assignRolesToUser(activeUser, List.of(orgRole));
        order.verify(moduleAutoGrantService).grantConfiguredModulesToNewUser(10L, 1L, 77L);
        order.verify(userSyncPublisher).publishUserSync(10L, 1L);
    }
}
