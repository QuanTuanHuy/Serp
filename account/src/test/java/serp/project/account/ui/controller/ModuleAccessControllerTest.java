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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.AssignUserToModuleRequest;
import serp.project.account.core.domain.dto.request.BulkAssignUsersRequest;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.request.BulkModuleAccessUsersRequest;
import serp.project.account.core.domain.dto.response.BulkModuleAccessResponse;
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
    void assignUserToModuleShouldAcceptModuleIdFromPathWhenBodyOmitsIt() throws Exception {
        when(authUtils.canAccessOrganization(10L)).thenReturn(true);
        when(authUtils.getCurrentUserId()).thenReturn(java.util.Optional.of(99L));
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("id", 1))
                .build();
        doReturn(responseBody).when(moduleAccessUseCase).assignUserToModule(any(), any(), any());

        mockMvc().perform(post("/api/v1/organizations/10/modules/20/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"userId":1,"roleId":30}
                        """))
                .andExpect(status().isOk());

        ArgumentCaptor<AssignUserToModuleRequest> captor =
                ArgumentCaptor.forClass(AssignUserToModuleRequest.class);
        verify(moduleAccessUseCase).assignUserToModule(
                org.mockito.Mockito.eq(10L),
                captor.capture(),
                org.mockito.Mockito.eq(99L));
        assertEquals(20L, captor.getValue().getModuleId());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(30L, captor.getValue().getRoleId());
    }

    @Test
    void bulkAssignUsersToModuleShouldAcceptIdsFromPathWhenBodyOmitsThem() throws Exception {
        when(authUtils.canAccessOrganization(10L)).thenReturn(true);
        when(authUtils.getCurrentUserId()).thenReturn(java.util.Optional.of(99L));
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("grantedCount", 2))
                .build();
        doReturn(responseBody).when(moduleAccessUseCase).bulkAssignUsersToModule(any(), any());

        mockMvc().perform(post("/api/v1/organizations/10/modules/20/users/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"userIds":[1,2],"roleId":30}
                        """))
                .andExpect(status().isOk());

        ArgumentCaptor<BulkAssignUsersRequest> captor =
                ArgumentCaptor.forClass(BulkAssignUsersRequest.class);
        verify(moduleAccessUseCase).bulkAssignUsersToModule(captor.capture(), org.mockito.Mockito.eq(99L));
        assertEquals(10L, captor.getValue().getOrganizationId());
        assertEquals(20L, captor.getValue().getModuleId());
        assertEquals(List.of(1L, 2L), captor.getValue().getUserIds());
        assertEquals(30L, captor.getValue().getRoleId());
    }

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

    @Test
    void bulkRevokeUsersFromModuleShouldPassAuthenticatedUserToUseCase() {
        when(authUtils.canAccessOrganization(10L)).thenReturn(true);
        when(authUtils.getCurrentUserId()).thenReturn(java.util.Optional.of(99L));

        BulkModuleAccessResponse summary = BulkModuleAccessResponse.empty(20L, 2);
        summary.markRevoked(1L);
        summary.markSkipped(2L, "USER_MODULE_ACCESS_NOT_FOUND");

        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(summary)
                .build();
        doReturn(responseBody).when(moduleAccessUseCase).bulkRevokeUsersFromModule(any(), any(), any(), any());

        var response = controller.bulkRevokeUsersFromModule(
                10L,
                20L,
                BulkModuleAccessUsersRequest.builder()
                        .userIds(List.of(1L, 2L))
                        .build());

        assertEquals(200, response.getStatusCodeValue());
        ArgumentCaptor<BulkModuleAccessUsersRequest> captor =
                ArgumentCaptor.forClass(BulkModuleAccessUsersRequest.class);
        verify(moduleAccessUseCase).bulkRevokeUsersFromModule(
                org.mockito.Mockito.eq(10L),
                org.mockito.Mockito.eq(20L),
                captor.capture(),
                org.mockito.Mockito.eq(99L));
        assertEquals(List.of(1L, 2L), captor.getValue().getUserIds());
    }

    private MockMvc mockMvc() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(new ResponseUtils()))
                .setValidator(validator)
                .build();
    }
}
