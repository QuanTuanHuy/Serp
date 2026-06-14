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
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchResponse;
import serp.project.account.core.usecase.SettingsSearchUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class SettingsSearchControllerTest {
    @Mock
    private SettingsSearchUseCase settingsSearchUseCase;
    @Mock
    private AuthUtils authUtils;

    private final ResponseUtils responseUtils = new ResponseUtils();

    @InjectMocks
    private SettingsSearchController controller;

    @Test
    void searchShouldCheckOrganizationAccessDelegateToUseCaseAndUseResponseCode() {
        controller = new SettingsSearchController(settingsSearchUseCase, authUtils, responseUtils);
        GeneralResponse<SettingsGlobalSearchResponse> body = GeneralResponse.<SettingsGlobalSearchResponse>builder()
                .code(200)
                .status("success")
                .message("OK")
                .data(SettingsGlobalSearchResponse.builder()
                        .query("mai")
                        .limit(5)
                        .groups(List.of())
                        .build())
                .build();
        when(authUtils.canAccessOrganization(7L)).thenReturn(true);
        when(settingsSearchUseCase.search(7L, "mai", 5)).thenAnswer(_invocation -> body);

        ResponseEntity<?> response = controller.search(7L, "mai", 5);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(body, response.getBody());
        verify(authUtils).canAccessOrganization(7L);
        verify(settingsSearchUseCase).search(7L, "mai", 5);
    }

    @Test
    void searchShouldReturnForbiddenWhenOrganizationCannotBeAccessed() {
        controller = new SettingsSearchController(settingsSearchUseCase, authUtils, responseUtils);
        when(authUtils.canAccessOrganization(7L)).thenReturn(false);

        ResponseEntity<?> response = controller.search(7L, "mai", 5);

        assertEquals(403, response.getStatusCodeValue());
        verify(authUtils).canAccessOrganization(7L);
    }
}
