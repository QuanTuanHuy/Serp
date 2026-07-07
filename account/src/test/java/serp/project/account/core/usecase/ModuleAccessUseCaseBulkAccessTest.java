/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.AssignUserToModuleRequest;
import serp.project.account.core.domain.dto.request.BulkAssignUsersRequest;
import serp.project.account.core.domain.dto.request.BulkModuleAccessUsersRequest;
import serp.project.account.core.domain.dto.response.BulkModuleAccessResponse;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.entity.UserModuleAccessEntity;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.exception.AppException;
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
class ModuleAccessUseCaseBulkAccessTest {

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
    @Mock private IOrganizationModuleAccessSettingService moduleAccessSettingService;
    @Mock private ModuleAutoGrantService moduleAutoGrantService;

    @Spy
    private ResponseUtils responseUtils = new ResponseUtils();

    @InjectMocks
    private ModuleAccessUseCase useCase;

    @Test
    void assignUserToModuleShouldFailWhenPlanModuleIsNotAccessible() {
        var request = AssignUserToModuleRequest.builder()
                .userId(1L)
                .moduleId(20L)
                .build();

        when(userService.getUserById(1L)).thenReturn(user(1L, null));
        when(moduleService.getModuleByIdFromCache(20L)).thenReturn(module(20L));
        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription());
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(
                SubscriptionPlanModuleEntity.builder()
                        .subscriptionPlanId(100L)
                        .moduleId(20L)
                        .isIncluded(false)
                        .build()));

        AppException exception = assertThrows(AppException.class,
                () -> useCase.assignUserToModule(10L, request, 99L));

        assertEquals(Constants.ErrorMessage.MODULE_NOT_IN_SUBSCRIPTION_PLAN, exception.getMessage());
        verify(userModuleAccessService, never()).registerUserToModuleWithExpiration(
                any(), any(), any(), any(), any());
    }

    @Test
    void bulkAssignUsersToModuleShouldGrantUntilSlotsRunOutAndSkipExistingAccess() {
        var request = BulkAssignUsersRequest.builder()
                .organizationId(10L)
                .moduleId(20L)
                .userIds(List.of(1L, 2L, 3L, 4L))
                .build();
        RoleEntity defaultRole = defaultRole();
        UserEntity user1 = user(1L, "kc-1");
        UserEntity user2 = user(2L, "kc-2");
        UserEntity user3 = user(3L, null);
        UserEntity user4 = user(4L, "kc-4");

        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription());
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule(20L, 3)));
        when(userModuleAccessService.countActiveUsers(20L, 10L)).thenReturn(1);
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(defaultRole));
        when(userService.getUsersByOrganizationIdAndIds(10L, List.of(1L, 2L, 3L, 4L)))
                .thenReturn(List.of(user1, user2, user3, user4));
        when(userModuleAccessService.getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(
                List.of(1L, 2L, 3L, 4L), 20L, 10L))
                .thenReturn(List.of(
                        access(1L, true),
                        access(2L, false)));
        when(userModuleAccessService.bulkRegisterUsersToModuleWithExpiration(
                List.of(2L, 3L), 20L, 10L, 99L, 999L))
                .thenReturn(List.of(access(2L, true), access(3L, true)));

        var response = useCase.bulkAssignUsersToModule(request, 99L);

        BulkModuleAccessResponse data = (BulkModuleAccessResponse) response.getData();
        assertEquals(200, response.getCode());
        assertEquals(4, data.getRequestedCount());
        assertEquals(2, data.getGrantedCount());
        assertEquals(2, data.getSkippedCount());
        assertEquals(List.of(2L, 3L), data.getGrantedUserIds());
        assertEquals("ALREADY_HAS_ACCESS", data.getSkippedUsers().get(0).getReason());
        assertEquals("MAX_USERS_LIMIT_REACHED", data.getSkippedUsers().get(1).getReason());
        verify(userModuleAccessService).bulkRegisterUsersToModuleWithExpiration(
                List.of(2L, 3L), 20L, 10L, 99L, 999L);
        verify(combineRoleService).assignRolesToUsers(List.of(user2, user3), List.of(defaultRole));
        verify(userSyncPublisher).publishUserSync(10L, 2L);
        verify(userSyncPublisher).publishUserSync(10L, 3L);
        verify(keycloakUserService).logoutUser("kc-2");
        verify(userModuleAccessService, never()).bulkRegisterUsersToModule(anyList(), any(), any(), any());
    }

    @Test
    void bulkAssignUsersToModuleShouldSkipUsersOutsideOrganization() {
        var request = BulkAssignUsersRequest.builder()
                .organizationId(10L)
                .moduleId(20L)
                .userIds(List.of(1L, 2L))
                .build();
        RoleEntity defaultRole = defaultRole();

        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription());
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule(20L, null)));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(defaultRole));
        when(userService.getUsersByOrganizationIdAndIds(10L, List.of(1L, 2L)))
                .thenReturn(List.of(user(1L, null)));
        when(userModuleAccessService.getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(
                List.of(1L, 2L), 20L, 10L))
                .thenReturn(List.of());
        when(userModuleAccessService.bulkRegisterUsersToModuleWithExpiration(
                List.of(1L), 20L, 10L, 99L, 999L))
                .thenReturn(List.of(access(1L, true)));

        var response = useCase.bulkAssignUsersToModule(request, 99L);

        BulkModuleAccessResponse data = (BulkModuleAccessResponse) response.getData();
        assertEquals(List.of(1L), data.getGrantedUserIds());
        assertEquals(1, data.getSkippedCount());
        assertEquals("USER_NOT_FOUND", data.getSkippedUsers().getFirst().getReason());
    }

    @Test
    void bulkAssignUsersToModuleShouldFailWhenNoDefaultModuleRoleExists() {
        var request = BulkAssignUsersRequest.builder()
                .organizationId(10L)
                .moduleId(20L)
                .userIds(List.of(1L))
                .build();

        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription());
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule(20L, null)));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(
                RoleEntity.builder()
                        .id(11L)
                        .moduleId(20L)
                        .isDefault(false)
                        .build()));

        AppException exception = assertThrows(AppException.class,
                () -> useCase.bulkAssignUsersToModule(request, 99L));

        assertEquals(Constants.ErrorMessage.NO_ROLES_FOUND_FOR_MODULE, exception.getMessage());
        verify(userModuleAccessService, never()).bulkRegisterUsersToModuleWithExpiration(
                anyList(), any(), any(), any(), any());
    }

    @Test
    void bulkAssignUsersToModuleShouldUseRequestedRoleWhenRoleIdIsProvided() {
        var request = BulkAssignUsersRequest.builder()
                .organizationId(10L)
                .moduleId(20L)
                .roleId(12L)
                .userIds(List.of(1L))
                .build();
        RoleEntity defaultRole = defaultRole();
        RoleEntity requestedRole = requestedRole();
        UserEntity user1 = user(1L, null);

        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription());
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule(20L, null)));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(defaultRole, requestedRole));
        when(userService.getUsersByOrganizationIdAndIds(10L, List.of(1L)))
                .thenReturn(List.of(user1));
        when(userModuleAccessService.getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(
                List.of(1L), 20L, 10L))
                .thenReturn(List.of());
        when(userModuleAccessService.bulkRegisterUsersToModuleWithExpiration(
                List.of(1L), 20L, 10L, 99L, 999L))
                .thenReturn(List.of(access(1L, true)));

        var response = useCase.bulkAssignUsersToModule(request, 99L);

        BulkModuleAccessResponse data = (BulkModuleAccessResponse) response.getData();
        assertEquals(List.of(1L), data.getGrantedUserIds());
        verify(combineRoleService).assignRolesToUsers(List.of(user1), List.of(requestedRole));
    }

    @Test
    void bulkAssignUsersToModuleShouldFailWhenRoleIdDoesNotBelongToModule() {
        var request = BulkAssignUsersRequest.builder()
                .organizationId(10L)
                .moduleId(20L)
                .roleId(99L)
                .userIds(List.of(1L))
                .build();

        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription());
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule(20L, null)));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(defaultRole()));

        AppException exception = assertThrows(AppException.class,
                () -> useCase.bulkAssignUsersToModule(request, 99L));

        assertEquals(Constants.ErrorMessage.ROLE_NOT_FOUND, exception.getMessage());
        verify(userModuleAccessService, never()).bulkRegisterUsersToModuleWithExpiration(
                anyList(), any(), any(), any(), any());
    }

    private OrganizationSubscriptionEntity subscription() {
        return OrganizationSubscriptionEntity.builder()
                .organizationId(10L)
                .subscriptionPlanId(100L)
                .endDate(999L)
                .build();
    }

    private SubscriptionPlanModuleEntity planModule(Long moduleId, Integer maxUsersPerModule) {
        return SubscriptionPlanModuleEntity.builder()
                .subscriptionPlanId(100L)
                .moduleId(moduleId)
                .isIncluded(true)
                .maxUsersPerModule(maxUsersPerModule)
                .build();
    }

    private RoleEntity defaultRole() {
        return RoleEntity.builder()
                .id(10L)
                .name("MODULE_USER")
                .moduleId(20L)
                .isDefault(true)
                .build();
    }

    private RoleEntity requestedRole() {
        return RoleEntity.builder()
                .id(12L)
                .name("MODULE_MANAGER")
                .moduleId(20L)
                .isDefault(false)
                .build();
    }

    private UserEntity user(Long id, String keycloakId) {
        return UserEntity.builder()
                .id(id)
                .primaryOrganizationId(10L)
                .keycloakId(keycloakId)
                .build();
    }

    private ModuleEntity module(Long id) {
        return ModuleEntity.builder()
                .id(id)
                .moduleName("Module " + id)
                .code("MOD" + id)
                .status(ModuleStatus.ACTIVE)
                .build();
    }

    private UserModuleAccessEntity access(Long userId, boolean active) {
        return UserModuleAccessEntity.builder()
                .userId(userId)
                .moduleId(20L)
                .organizationId(10L)
                .isActive(active)
                .build();
    }

    @Test
    void bulkRevokeUsersFromModuleShouldRevokeExistingAccessAndSkipMissingAccess() {
        var request = BulkModuleAccessUsersRequest.builder()
                .userIds(List.of(1L, 2L, 3L))
                .build();
        RoleEntity moduleRole = RoleEntity.builder()
                .id(10L)
                .name("MODULE_USER")
                .moduleId(20L)
                .build();
        UserEntity user1 = user(1L, "kc-1");
        UserEntity user2 = user(2L, "kc-2");
        UserEntity user3 = user(3L, null);
        UserModuleAccessEntity activeAccess = access(1L, true);
        UserModuleAccessEntity inactiveAccess = access(2L, false);

        when(userService.getUsersByOrganizationIdAndIds(10L, List.of(1L, 2L, 3L)))
                .thenReturn(List.of(user1, user2, user3));
        when(userModuleAccessService.getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(
                List.of(1L, 2L, 3L), 20L, 10L))
                .thenReturn(List.of(activeAccess, inactiveAccess));
        when(userModuleAccessService.saveAll(List.of(activeAccess, inactiveAccess)))
                .thenReturn(List.of(activeAccess, inactiveAccess));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(moduleRole));

        var response = useCase.bulkRevokeUsersFromModule(10L, 20L, request, 99L);

        BulkModuleAccessResponse data = (BulkModuleAccessResponse) response.getData();
        assertEquals(200, response.getCode());
        assertEquals(List.of(1L, 2L), data.getRevokedUserIds());
        assertEquals(1, data.getSkippedCount());
        assertEquals("USER_MODULE_ACCESS_NOT_FOUND", data.getSkippedUsers().get(0).getReason());
        assertEquals(false, activeAccess.getIsActive());
        assertEquals(false, inactiveAccess.getIsActive());
        verify(userModuleAccessService).saveAll(List.of(activeAccess, inactiveAccess));
        verify(combineRoleService).removeRolesFromUsers(List.of(user1, user2), List.of(moduleRole));
        verify(userSyncPublisher).publishUserSync(10L, 1L);
        verify(userSyncPublisher).publishUserSync(10L, 2L);
        verify(keycloakUserService).logoutUser("kc-1");
        verify(keycloakUserService).logoutUser("kc-2");
    }

    @Test
    void bulkRevokeUsersFromModuleShouldRevokeExistingExpiredAccessLikeSingleRevoke() {
        var request = BulkModuleAccessUsersRequest.builder()
                .userIds(List.of(1L))
                .build();
        RoleEntity moduleRole = RoleEntity.builder()
                .id(10L)
                .name("MODULE_USER")
                .moduleId(20L)
                .build();
        UserEntity user1 = user(1L, "kc-1");
        UserModuleAccessEntity expiredAccess = access(1L, true);
        expiredAccess.setExpiresAt(System.currentTimeMillis() - 1_000L);

        when(userService.getUsersByOrganizationIdAndIds(10L, List.of(1L)))
                .thenReturn(List.of(user1));
        when(userModuleAccessService.getUserModuleAccessesByUserIdsAndModuleIdAndOrgId(
                List.of(1L), 20L, 10L))
                .thenReturn(List.of(expiredAccess));
        when(userModuleAccessService.saveAll(List.of(expiredAccess))).thenReturn(List.of(expiredAccess));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(moduleRole));

        var response = useCase.bulkRevokeUsersFromModule(10L, 20L, request, 99L);

        BulkModuleAccessResponse data = (BulkModuleAccessResponse) response.getData();
        assertEquals(List.of(1L), data.getRevokedUserIds());
        assertEquals(0, data.getSkippedCount());
        assertEquals(false, expiredAccess.getIsActive());
        verify(userModuleAccessService).saveAll(List.of(expiredAccess));
        verify(combineRoleService).removeRolesFromUsers(List.of(user1), List.of(moduleRole));
        verify(userSyncPublisher).publishUserSync(10L, 1L);
        verify(keycloakUserService).logoutUser("kc-1");
    }
}
