/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.entity.OrganizationModuleAccessSettingEntity;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.entity.UserModuleAccessEntity;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IOrganizationModuleAccessSettingService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.core.service.IUserService;

@ExtendWith(MockitoExtension.class)
class ModuleAutoGrantServiceTest {

    @Mock
    private IOrganizationModuleAccessSettingService settingService;

    @Mock
    private ISubscriptionService subscriptionService;

    @Mock
    private ISubscriptionPlanService subscriptionPlanService;

    @Mock
    private IUserModuleAccessService userModuleAccessService;

    @Mock
    private IUserService userService;

    @Mock
    private IRoleService roleService;

    @Mock
    private ICombineRoleService combineRoleService;

    @Mock
    private UserSyncPublisher userSyncPublisher;

    @InjectMocks
    private ModuleAutoGrantService service;

    private OrganizationSubscriptionEntity subscription;
    private SubscriptionPlanModuleEntity planModule;
    private RoleEntity defaultRole;

    @BeforeEach
    void setUp() {
        subscription = OrganizationSubscriptionEntity.builder()
                .organizationId(10L)
                .subscriptionPlanId(100L)
                .endDate(999999999L)
                .build();
        planModule = SubscriptionPlanModuleEntity.builder()
                .moduleId(20L)
                .isIncluded(true)
                .maxUsersPerModule(2)
                .build();
        defaultRole = RoleEntity.builder()
                .id(30L)
                .moduleId(20L)
                .name("ROLE_MODULE_USER")
                .isDefault(true)
                .build();
    }

    @Test
    void backfillExistingUsersShouldGrantUntilQuotaIsFullWithBoundedQueries() {
        UserEntity alice = activeUser(1L);
        List<UserEntity> grantedUsers = List.of(alice);

        when(settingService.isAutoGrantEnabled(10L, 20L)).thenReturn(true);
        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription);
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(defaultRole));
        when(userModuleAccessService.countActiveUsers(20L, 10L)).thenReturn(1);
        when(userService.countActiveUsersWithoutModuleAccess(10L, 20L)).thenReturn(2L);
        when(userService.getActiveUsersWithoutModuleAccess(10L, 20L, 1)).thenReturn(grantedUsers);
        when(userModuleAccessService.bulkRegisterUsersToModuleWithExpiration(
                List.of(1L), 20L, 10L, 99L, 999999999L))
                .thenReturn(List.of(UserModuleAccessEntity.builder().userId(1L).moduleId(20L).build()));

        var response = service.backfillExistingUsers(10L, 20L, 99L);

        assertEquals(1, response.getGrantedCount());
        assertEquals(1, response.getSkippedCount());
        assertEquals(1, response.getSkippedReasons().get(Constants.ErrorMessage.MAX_USERS_LIMIT_REACHED));
        verify(combineRoleService).assignRolesToUsers(grantedUsers, List.of(defaultRole));
        verify(userSyncPublisher).publishUserSync(10L, 1L);
        verify(userService, never()).getUsersByOrganizationId(10L);
    }

    @Test
    void backfillExistingUsersShouldFailWhenDefaultRoleIsMissing() {
        when(settingService.isAutoGrantEnabled(10L, 20L)).thenReturn(true);
        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription);
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of());

        AppException exception = assertThrows(AppException.class,
                () -> service.backfillExistingUsers(10L, 20L, 99L));

        assertEquals(Constants.ErrorMessage.AUTO_GRANT_REQUIRES_DEFAULT_MODULE_ROLE, exception.getMessage());
    }

    @Test
    void grantConfiguredModulesToNewUserShouldAssignCollectedRolesOnce() {
        var secondPlanModule = SubscriptionPlanModuleEntity.builder()
                .moduleId(21L)
                .isIncluded(true)
                .maxUsersPerModule(5)
                .build();
        var secondRole = RoleEntity.builder()
                .id(31L)
                .moduleId(21L)
                .name("ROLE_SECOND_MODULE_USER")
                .isDefault(true)
                .build();
        var user = activeUser(1L);

        when(settingService.getEnabledByOrganizationId(10L)).thenReturn(List.of(
                enabledSetting(20L),
                enabledSetting(21L)));
        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription);
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule, secondPlanModule));
        when(userService.getUserById(1L)).thenReturn(user);
        when(userModuleAccessService.hasAccess(1L, 20L, 10L)).thenReturn(false);
        when(userModuleAccessService.hasAccess(1L, 21L, 10L)).thenReturn(false);
        when(userModuleAccessService.countActiveUsers(20L, 10L)).thenReturn(0);
        when(userModuleAccessService.countActiveUsers(21L, 10L)).thenReturn(0);
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(defaultRole));
        when(roleService.getRolesByModuleId(21L)).thenReturn(List.of(secondRole));
        when(userModuleAccessService.registerUserToModuleWithExpiration(1L, 20L, 10L, 99L, 999999999L))
                .thenReturn(UserModuleAccessEntity.builder().userId(1L).moduleId(20L).build());
        when(userModuleAccessService.registerUserToModuleWithExpiration(1L, 21L, 10L, 99L, 999999999L))
                .thenReturn(UserModuleAccessEntity.builder().userId(1L).moduleId(21L).build());

        var response = service.grantConfiguredModulesToNewUser(10L, 1L, 99L);

        assertEquals(2, response.getGrantedCount());
        assertEquals(0, response.getSkippedCount());
        verify(combineRoleService).assignRolesToUser(user, List.of(defaultRole, secondRole));
    }

    @Test
    void grantConfiguredModulesToNewUserShouldSkipQuotaFullModuleWithoutThrowing() {
        when(settingService.getEnabledByOrganizationId(10L)).thenReturn(List.of(enabledSetting(20L)));
        when(subscriptionService.getActiveOrPendingUpgrade(10L)).thenReturn(subscription);
        when(subscriptionPlanService.getPlanModules(100L)).thenReturn(List.of(planModule));
        when(roleService.getRolesByModuleId(20L)).thenReturn(List.of(defaultRole));
        when(userService.getUserById(1L)).thenReturn(activeUser(1L));
        when(userModuleAccessService.hasAccess(1L, 20L, 10L)).thenReturn(false);
        when(userModuleAccessService.countActiveUsers(20L, 10L)).thenReturn(2);

        var response = service.grantConfiguredModulesToNewUser(10L, 1L, 99L);

        assertEquals(0, response.getGrantedCount());
        assertEquals(1, response.getSkippedCount());
        verify(combineRoleService, never()).assignRolesToUser(any(), any());
    }

    private OrganizationModuleAccessSettingEntity enabledSetting(Long moduleId) {
        return OrganizationModuleAccessSettingEntity.builder()
                .organizationId(10L)
                .moduleId(moduleId)
                .autoGrantToNewUsers(true)
                .build();
    }

    private UserEntity activeUser(Long id) {
        return UserEntity.builder()
                .id(id)
                .primaryOrganizationId(10L)
                .status(UserStatus.ACTIVE)
                .keycloakId("kc-" + id)
                .build();
    }
}
