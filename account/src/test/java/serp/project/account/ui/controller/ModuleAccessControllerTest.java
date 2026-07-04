/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.usecase.ModuleAccessUseCase;
import serp.project.account.core.usecase.UserUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class ModuleAccessControllerTest {

    @Mock
    private ModuleAccessUseCase moduleAccessUseCase;

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private ResponseUtils responseUtils;

    @InjectMocks
    private ModuleAccessController controller;

    @Test
    void getUsersWithAccessToModuleShouldPassModuleFilterToUserUseCase() {
        when(authUtils.canAccessOrganization(10L)).thenReturn(true);
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("items", List.of()))
                .build();
        doReturn(responseBody).when(userUseCase).getUsers(any(GetUserParams.class));

        ResponseEntity<?> response = controller.getUsersWithAccessToModule(
                10L,
                20L,
                2,
                25,
                "email",
                "asc",
                "alice",
                "ACTIVE",
                "MEMBER",
                30L,
                40L);

        assertEquals(200, response.getStatusCodeValue());
        ArgumentCaptor<GetUserParams> captor = ArgumentCaptor.forClass(GetUserParams.class);
        verify(userUseCase).getUsers(captor.capture());
        assertEquals(10L, captor.getValue().getOrganizationId());
        assertEquals(20L, captor.getValue().getModuleId());
        assertEquals(2, captor.getValue().getPage());
        assertEquals(25, captor.getValue().getPageSize());
        assertEquals("email", captor.getValue().getSortBy());
        assertEquals("asc", captor.getValue().getSortDirection());
        assertEquals("alice", captor.getValue().getSearch());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        assertEquals("MEMBER", captor.getValue().getUserType());
        assertEquals(30L, captor.getValue().getRoleId());
        assertEquals(40L, captor.getValue().getDepartmentId());
    }

    @Test
    void updateModuleAccessSettingsShouldPassAuthenticatedUserToUseCase() {
        when(authUtils.canAccessOrganization(10L)).thenReturn(true);
        when(authUtils.getCurrentUserId()).thenReturn(java.util.Optional.of(99L));
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("autoGrantToNewUsers", true))
                .build();
        doReturn(responseBody).when(moduleAccessUseCase).updateModuleAccessSettings(
                any(), any(), any(), any());

        var response = controller.updateModuleAccessSettings(
                10L,
                20L,
                serp.project.account.core.domain.dto.request.UpdateModuleAccessSettingsRequest.builder()
                        .autoGrantToNewUsers(true)
                        .build());

        assertEquals(200, response.getStatusCodeValue());
        verify(moduleAccessUseCase).updateModuleAccessSettings(
                org.mockito.Mockito.eq(10L),
                org.mockito.Mockito.eq(20L),
                any(),
                org.mockito.Mockito.eq(99L));
    }

    @Test
    void backfillAutoGrantShouldPassAuthenticatedUserToUseCase() {
        when(authUtils.canAccessOrganization(10L)).thenReturn(true);
        when(authUtils.getCurrentUserId()).thenReturn(java.util.Optional.of(99L));
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("grantedCount", 1))
                .build();
        doReturn(responseBody).when(moduleAccessUseCase).backfillAutoGrant(10L, 20L, 99L);

        var response = controller.backfillAutoGrant(10L, 20L);

        assertEquals(200, response.getStatusCodeValue());
        verify(moduleAccessUseCase).backfillAutoGrant(10L, 20L, 99L);
    }
}
