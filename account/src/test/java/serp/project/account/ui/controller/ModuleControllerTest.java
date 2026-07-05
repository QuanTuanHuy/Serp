/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetModulesParams;
import serp.project.account.core.usecase.ModuleUseCase;

@ExtendWith(MockitoExtension.class)
class ModuleControllerTest {

    @Mock
    private ModuleUseCase moduleUseCase;

    @InjectMocks
    private ModuleV2Controller controller;

    @Test
    void getModulesPaginatedShouldCallUseCaseWithQueryParams() throws Exception {
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .build();
        doReturn(responseBody).when(moduleUseCase).getModulesPaginated(any());

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v2/modules")
                .param("page", "1")
                .param("pageSize", "20")
                .param("sortBy", "code")
                .param("sortDirection", "desc")
                .param("search", "crm")
                .param("status", "ACTIVE")
                .param("moduleType", "SYSTEM"))
                .andExpect(status().isOk());

        ArgumentCaptor<GetModulesParams> captor = ArgumentCaptor.forClass(GetModulesParams.class);
        verify(moduleUseCase).getModulesPaginated(captor.capture());
        assertEquals(1, captor.getValue().getPage());
        assertEquals(20, captor.getValue().getPageSize());
        assertEquals("code", captor.getValue().getSortBy());
        assertEquals("desc", captor.getValue().getSortDirection());
        assertEquals("crm", captor.getValue().getSearch());
        assertEquals(serp.project.account.core.domain.enums.ModuleStatus.ACTIVE, captor.getValue().getStatus());
        assertEquals(serp.project.account.core.domain.enums.ModuleType.SYSTEM, captor.getValue().getModuleType());
    }
}
