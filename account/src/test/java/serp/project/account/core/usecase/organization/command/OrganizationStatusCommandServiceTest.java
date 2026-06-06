/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.organization.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.dto.request.UpdateOrganizationStatusRequest;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IUserService;

@ExtendWith(MockitoExtension.class)
class OrganizationStatusCommandServiceTest {
    @Mock
    private IOrganizationService organizationService;

    @Mock
    private IUserService userService;

    @InjectMocks
    private OrganizationStatusCommandService commandService;

    private OrganizationEntity organization;
    private UserEntity activeUser;
    private UserEntity invitedUser;
    private UserEntity suspendedUser;

    @BeforeEach
    void setUp() {
        organization = OrganizationEntity.builder()
                .id(10L)
                .name("Acme")
                .status(OrganizationStatus.ACTIVE)
                .build();

        activeUser = UserEntity.builder()
                .id(1L)
                .status(UserStatus.ACTIVE)
                .build();

        invitedUser = UserEntity.builder()
                .id(2L)
                .status(UserStatus.INVITED)
                .build();

        suspendedUser = UserEntity.builder()
                .id(3L)
                .status(UserStatus.SUSPENDED)
                .build();
    }

    @Test
    void suspendOrganizationShouldSuspendOrganizationAndAllUsers() {
        when(organizationService.updateOrganizationStatus(10L, OrganizationStatus.SUSPENDED))
                .thenReturn(OrganizationEntity.builder()
                        .id(10L)
                        .status(OrganizationStatus.SUSPENDED)
                        .build());
        when(userService.getUsersByOrganizationId(10L))
                .thenReturn(List.of(activeUser, invitedUser, suspendedUser));
        when(userService.updateUser(anyLong(), any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(1));

        var response = commandService.updateOrganizationStatus(
                10L,
                1L,
                UpdateOrganizationStatusRequest.builder()
                        .status(OrganizationStatus.SUSPENDED)
                        .build());

        assertEquals(3, response.getAffectedUsers());
        assertEquals(0, response.getActivatedUsers());
        assertEquals(3, response.getSuspendedUsers());
        assertEquals(OrganizationStatus.SUSPENDED, response.getOrganization().getStatus());

        ArgumentCaptor<UserEntity> patchCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService, times(3)).updateUser(anyLong(), patchCaptor.capture());
        patchCaptor.getAllValues().forEach(patch -> {
            assertEquals(UserStatus.SUSPENDED, patch.getStatus());
            assertNotNull(patch.getUpdatedAt());
        });
    }

    @Test
    void activateOrganizationShouldActivateOnlySuspendedUsers() {
        when(organizationService.updateOrganizationStatus(10L, OrganizationStatus.ACTIVE))
                .thenReturn(OrganizationEntity.builder()
                        .id(10L)
                        .status(OrganizationStatus.ACTIVE)
                        .build());
        when(userService.getUsersByOrganizationId(10L))
                .thenReturn(List.of(activeUser, invitedUser, suspendedUser));
        when(userService.updateUser(anyLong(), any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(1));

        var response = commandService.updateOrganizationStatus(
                10L,
                1L,
                UpdateOrganizationStatusRequest.builder()
                        .status(OrganizationStatus.ACTIVE)
                        .build());

        assertEquals(1, response.getAffectedUsers());
        assertEquals(1, response.getActivatedUsers());
        assertEquals(0, response.getSuspendedUsers());

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<UserEntity> patchCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userService, times(1)).updateUser(userIdCaptor.capture(), patchCaptor.capture());
        assertEquals(3L, userIdCaptor.getValue());
        assertEquals(UserStatus.ACTIVE, patchCaptor.getValue().getStatus());
        assertNotNull(patchCaptor.getValue().getUpdatedAt());
    }
}
