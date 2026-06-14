/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchResponse;
import serp.project.account.core.usecase.AdminSearchUseCase;

@ExtendWith(MockitoExtension.class)
class AdminSearchControllerTest {
    @Mock
    private AdminSearchUseCase adminSearchUseCase;

    @InjectMocks
    private AdminSearchController controller;

    @Test
    void searchShouldDelegateToUseCaseAndUseResponseCode() {
        GeneralResponse<AdminGlobalSearchResponse> body = GeneralResponse.<AdminGlobalSearchResponse>builder()
                .code(200)
                .status("success")
                .message("OK")
                .data(AdminGlobalSearchResponse.builder()
                        .query("acme")
                        .limit(5)
                        .groups(List.of())
                        .build())
                .build();
        when(adminSearchUseCase.search("acme", 5)).thenAnswer(_invocation -> body);

        ResponseEntity<?> response = controller.search("acme", 5);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(body, response.getBody());
        verify(adminSearchUseCase).search("acme", 5);
    }
}
