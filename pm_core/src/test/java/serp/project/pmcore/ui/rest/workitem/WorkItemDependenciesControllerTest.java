/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import serp.project.pmcore.application.workitem.query.dependencies.ListWorkItemDependenciesQuery;
import serp.project.pmcore.application.workitem.query.dependencies.ListWorkItemDependenciesQueryHandler;
import serp.project.pmcore.application.workitem.query.dependencies.WorkItemDependenciesPageView;
import serp.project.pmcore.application.workitem.query.dependencies.WorkItemDependencySummaryView;
import serp.project.pmcore.domain.workitem.dto.WorkItemDependencyCriteria;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemDependenciesControllerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;

    @Mock
    private AuthUtils authUtils;
    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private ListWorkItemDependenciesQueryHandler listWorkItemDependenciesQueryHandler;

    private WorkItemDependenciesController controller;

    @BeforeEach
    void setUp() {
        controller = new WorkItemDependenciesController(
                authUtils,
                responseUtils,
                listWorkItemDependenciesQueryHandler
        );
    }

    @Test
    void listWorkItemDependenciesShouldDelegateWithDefaultDepthAndFlags() {
        WorkItemDependencyCriteria criteria = new WorkItemDependencyCriteria();
        WorkItemDependenciesPageView result = new WorkItemDependenciesPageView(
                PROJECT_ID,
                List.of(),
                List.of(),
                new WorkItemDependencySummaryView(0, 0, 0, 0, 0, 0, 0),
                0,
                0,
                0,
                10,
                2,
                true,
                false
        );
        GeneralResponse<WorkItemDependenciesPageView> body =
                GeneralResponse.<WorkItemDependenciesPageView>builder()
                        .data(result)
                        .build();
        when(authUtils.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(authUtils.getCurrentTenantId()).thenReturn(Optional.of(TENANT_ID));
        when(authUtils.getCurrentGroups()).thenReturn(Set.of("pm"));
        when(listWorkItemDependenciesQueryHandler.handle(any())).thenReturn(result);
        when(responseUtils.success(result)).thenReturn(body);

        ResponseEntity<GeneralResponse<WorkItemDependenciesPageView>> response =
                controller.listWorkItemDependencies(PROJECT_ID, criteria);

        assertSame(body, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        ArgumentCaptor<ListWorkItemDependenciesQuery> captor =
                ArgumentCaptor.forClass(ListWorkItemDependenciesQuery.class);
        verify(listWorkItemDependenciesQueryHandler).handle(captor.capture());
        ListWorkItemDependenciesQuery query = captor.getValue();
        assertEquals(TENANT_ID, query.tenantId());
        assertEquals(USER_ID, query.userId());
        assertEquals(Set.of("pm"), query.groupKeys());
        assertEquals(PROJECT_ID, query.criteria().getProjectId());
        assertEquals(2, query.criteria().getEffectiveDepth());
        assertTrue(query.criteria().isIncludeOutside());
        assertFalse(query.criteria().isIncludeRelatedLinks());
    }
}
