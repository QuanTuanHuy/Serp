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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.BulkAssignUsersToDepartmentRequest;
import serp.project.account.core.usecase.DepartmentUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    @Mock
    private DepartmentUseCase departmentUseCase;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private ResponseUtils responseUtils;

    @InjectMocks
    private DepartmentController controller;

    @Test
    void bulkAssignUsersToDepartmentShouldAcceptDepartmentIdFromPathWhenBodyOmitsIt() throws Exception {
        when(authUtils.canAccessOrganization(10L)).thenReturn(true);
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("assignedCount", 2))
                .build();
        doReturn(responseBody).when(departmentUseCase).bulkAssignUsersToDepartment(any(), any());

        mockMvc().perform(post("/api/v1/organizations/10/departments/30/users/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"userIds":[1,2],"jobTitle":"Engineer"}
                        """))
                .andExpect(status().isOk());

        ArgumentCaptor<BulkAssignUsersToDepartmentRequest> captor =
                ArgumentCaptor.forClass(BulkAssignUsersToDepartmentRequest.class);
        verify(departmentUseCase).bulkAssignUsersToDepartment(
                org.mockito.Mockito.eq(10L),
                captor.capture());
        assertEquals(30L, captor.getValue().getDepartmentId());
        assertEquals(List.of(1L, 2L), captor.getValue().getUserIds());
        assertEquals("Engineer", captor.getValue().getJobTitle());
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
