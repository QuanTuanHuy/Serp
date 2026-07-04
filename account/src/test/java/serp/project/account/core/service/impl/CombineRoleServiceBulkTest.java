/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.port.store.IUserRolePort;
import serp.project.account.core.service.IKeycloakUserService;

@ExtendWith(MockitoExtension.class)
class CombineRoleServiceBulkTest {

    @Mock
    private IKeycloakUserService keycloakUserService;

    @Mock
    private IUserRolePort userRolePort;

    @InjectMocks
    private CombineRoleService service;

    @Test
    void removeRolesFromUsersShouldDeleteUserRolesAndRevokeClientRolesForEachUser() {
        UserEntity alice = UserEntity.builder()
                .id(1L)
                .keycloakId("kc-alice")
                .build();
        UserEntity bob = UserEntity.builder()
                .id(2L)
                .keycloakId("kc-bob")
                .build();
        RoleEntity clientRole = RoleEntity.builder()
                .id(10L)
                .name("CRM_USER")
                .keycloakClientId("crm-client")
                .build();
        RoleEntity realmRole = RoleEntity.builder()
                .id(11L)
                .name("REALM_ROLE")
                .keycloakClientId(null)
                .build();

        service.removeRolesFromUsers(List.of(alice, bob), List.of(clientRole, realmRole));

        verify(userRolePort).deleteUserRolesByUserIdsAndRoleIds(
                List.of(1L, 2L),
                List.of(10L, 11L));
        verify(keycloakUserService).revokeClientRoles("kc-alice", "crm-client", List.of("CRM_USER"));
        verify(keycloakUserService).revokeClientRoles("kc-bob", "crm-client", List.of("CRM_USER"));
    }
}
