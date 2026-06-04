/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.dto.request.UpdateOrganizationStatusRequest;
import serp.project.account.core.domain.dto.response.OrganizationStatusUpdateResponse;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.usecase.OrganizationUseCase;
import serp.project.account.core.usecase.RoleUseCase;
import serp.project.account.core.usecase.UserUseCase;
import serp.project.account.kernel.utils.AuthUtils;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {
    @Mock
    private OrganizationUseCase organizationUseCase;

    @Mock
    private RoleUseCase roleUseCase;

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private OrganizationController controller;

    @Test
    void getOrganizationsShouldPassSortParams() {
        GeneralResponse responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("items", List.of()))
                .build();
        when(organizationUseCase.getOrganizations(any())).thenReturn(responseBody);

        ResponseEntity<?> response = controller.getOrganizations("acme", "ACTIVE", "ENTERPRISE", 2, 20, "name",
                "asc");

        assertEquals(200, response.getStatusCodeValue());

        ArgumentCaptor<GetOrganizationParams> captor = ArgumentCaptor.forClass(GetOrganizationParams.class);
        verify(organizationUseCase).getOrganizations(captor.capture());
        assertEquals("acme", captor.getValue().getSearch());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        assertEquals("ENTERPRISE", captor.getValue().getType());
        assertEquals(2, captor.getValue().getPage());
        assertEquals(20, captor.getValue().getPageSize());
        assertEquals("name", captor.getValue().getSortBy());
        assertEquals("asc", captor.getValue().getSortDirection());
    }

    @Test
    void updateOrganizationStatusShouldDelegateRequest() {
        when(authUtils.getCurrentUserId()).thenReturn(Optional.of(99L));
        GeneralResponse responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(OrganizationStatusUpdateResponse.builder()
                        .organization(OrganizationEntity.builder().id(10L).status(OrganizationStatus.SUSPENDED).build())
                        .affectedUsers(3)
                        .activatedUsers(0)
                        .suspendedUsers(3)
                        .build())
                .build();
        when(organizationUseCase.updateOrganizationStatus(eq(10L), eq(99L), any(UpdateOrganizationStatusRequest.class)))
                .thenReturn(responseBody);

        ResponseEntity<?> response = controller.updateOrganizationStatus(10L,
                UpdateOrganizationStatusRequest.builder().status(OrganizationStatus.SUSPENDED).build());

        assertEquals(200, response.getStatusCodeValue());
        ArgumentCaptor<UpdateOrganizationStatusRequest> captor =
                ArgumentCaptor.forClass(UpdateOrganizationStatusRequest.class);
        verify(organizationUseCase).updateOrganizationStatus(eq(10L), eq(99L), captor.capture());
        assertEquals(OrganizationStatus.SUSPENDED, captor.getValue().getStatus());
    }

    @Test
    void getOrganizationUserStatsShouldDelegateToUserUseCase() {
        GeneralResponse responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("totalUsers", 5))
                .build();
        when(userUseCase.getUserStats(10L)).thenReturn(responseBody);

        ResponseEntity<?> response = controller.getOrganizationUserStats(10L);

        assertEquals(200, response.getStatusCodeValue());
        verify(userUseCase).getUserStats(10L);
    }
}
