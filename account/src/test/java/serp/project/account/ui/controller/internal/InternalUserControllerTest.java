/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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
import serp.project.account.core.usecase.UserUseCase;

@ExtendWith(MockitoExtension.class)
class InternalUserControllerTest {

    @Mock
    private UserUseCase userUseCase;

    @InjectMocks
    private InternalUserController controller;

    @Test
    void getUsersShouldPassModuleIdAndFiltersToUserUseCase() {
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("items", List.of()))
                .build();
        doReturn(responseBody).when(userUseCase).getUsers(any(GetUserParams.class));

        ResponseEntity<?> response = controller.getUsers(
                1,
                10,
                "email",
                "desc",
                "alice",
                "ACTIVE",
                "MEMBER",
                30L,
                40L,
                20L,
                10L);

        assertEquals(200, response.getStatusCodeValue());
        ArgumentCaptor<GetUserParams> captor = ArgumentCaptor.forClass(GetUserParams.class);
        verify(userUseCase).getUsers(captor.capture());
        assertEquals(1, captor.getValue().getPage());
        assertEquals(10, captor.getValue().getPageSize());
        assertEquals("email", captor.getValue().getSortBy());
        assertEquals("desc", captor.getValue().getSortDirection());
        assertEquals("alice", captor.getValue().getSearch());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        assertEquals("MEMBER", captor.getValue().getUserType());
        assertEquals(30L, captor.getValue().getRoleId());
        assertEquals(40L, captor.getValue().getDepartmentId());
        assertEquals(20L, captor.getValue().getModuleId());
        assertEquals(10L, captor.getValue().getOrganizationId());
    }
}
