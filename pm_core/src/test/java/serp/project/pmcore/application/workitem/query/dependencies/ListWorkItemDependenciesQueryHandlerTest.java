/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.dependencies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.workitem.query.dependencies.support.WorkItemDependencyGraphBuilder;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.enums.IssueLinkDependencyBehavior;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkPort;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.WorkItemDependencyCriteria;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListWorkItemDependenciesQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long PROJECT_ID = 10L;

    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IIssueLinkPort issueLinkPort;
    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private IWorkItemPlanPort workItemPlanPort;

    private ListWorkItemDependenciesQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListWorkItemDependenciesQueryHandler(
                workItemReadPort,
                issueLinkPort,
                projectService,
                projectPermissionEvaluationService,
                workItemPlanPort,
                new WorkItemDependencyGraphBuilder()
        );
    }

    @Test
    void handleShouldNormalizeBlockAndDependsOnDirections() {
        WorkItemDependencyCriteria criteria = baseCriteria();
        stubProject();
        when(workItemReadPort.searchWorkItems(eq(TENANT_ID), any())).thenReturn(new PageResult<>(
                List.of(item(101L), item(102L), item(103L)),
                3
        ));
        when(issueLinkPort.listByWorkItemIds(TENANT_ID, List.of(101L, 102L, 103L))).thenReturn(List.of(
                link(501L, 101L, 102L, IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET),
                link(502L, 103L, 102L, IssueLinkDependencyBehavior.SOURCE_DEPENDS_ON_TARGET)
        ));
        when(workItemPlanPort.listActivePlansByWorkItemIds(TENANT_ID, List.of(101L, 102L, 103L)))
                .thenReturn(List.of());

        WorkItemDependenciesPageView result = handler.handle(query(criteria));

        assertEquals(2, result.edges().size());
        WorkItemDependencyEdgeView blocksEdge = result.edges().stream()
                .filter(edge -> edge.linkId().equals(501L))
                .findFirst()
                .orElseThrow();
        assertEquals(101L, blocksEdge.predecessorId());
        assertEquals(102L, blocksEdge.successorId());
        assertFalse(blocksEdge.relatedLink());

        WorkItemDependencyEdgeView dependsOnEdge = result.edges().stream()
                .filter(edge -> edge.linkId().equals(502L))
                .findFirst()
                .orElseThrow();
        assertEquals(102L, dependsOnEdge.predecessorId());
        assertEquals(103L, dependsOnEdge.successorId());
        assertEquals(2, result.summary().dependencyCount());
        verify(projectPermissionEvaluationService).checkPermission(any(), any(), eq(ProjectPermissionKeys.BROWSE_PROJECTS));
    }

    @Test
    void handleShouldIncludeOutsideBlockersByDefault() {
        WorkItemDependencyCriteria criteria = baseCriteria();
        stubProject();
        when(workItemReadPort.searchWorkItems(eq(TENANT_ID), any())).thenReturn(new PageResult<>(
                List.of(item(102L)),
                1
        ));
        when(issueLinkPort.listByWorkItemIds(TENANT_ID, List.of(102L))).thenReturn(List.of(
                link(501L, 101L, 102L, IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET)
        ));
        when(workItemReadPort.listActiveByWorkItemIds(TENANT_ID, List.of(101L))).thenReturn(List.of(item(101L)));
        when(issueLinkPort.listByWorkItemIds(TENANT_ID, List.of(101L))).thenReturn(List.of());
        when(workItemPlanPort.listActivePlansByWorkItemIds(TENANT_ID, List.of(102L, 101L))).thenReturn(List.of(
                WorkItemPlanEntity.builder()
                        .id(900L)
                        .tenantId(TENANT_ID)
                        .projectId(PROJECT_ID)
                        .workItemId(101L)
                        .plannedStart(1_700_000_000_000L)
                        .plannedEnd(1_700_086_400_000L)
                        .build()
        ));

        WorkItemDependenciesPageView result = handler.handle(query(criteria));

        assertEquals(2, result.nodes().size());
        WorkItemDependencyNodeView outsideNode = result.nodes().stream()
                .filter(node -> node.id().equals(101L))
                .findFirst()
                .orElseThrow();
        assertTrue(outsideNode.outsideFilter());
        assertEquals(1_700_000_000_000L, outsideNode.plannedStart());
        assertEquals(1, outsideNode.blocksCount());
        assertEquals(1, result.summary().outsideDependencyCount());
        assertEquals(1, result.summary().blockerCount());
        assertEquals(1, result.summary().blockedItemCount());
    }

    @Test
    void handleShouldExcludeNoneLinksUnlessRelatedLinksIsEnabled() {
        WorkItemDependencyCriteria criteria = baseCriteria();
        stubProject();
        when(workItemReadPort.searchWorkItems(eq(TENANT_ID), any())).thenReturn(new PageResult<>(
                List.of(item(101L), item(102L)),
                2
        ));
        when(issueLinkPort.listByWorkItemIds(TENANT_ID, List.of(101L, 102L))).thenReturn(List.of(
                link(501L, 101L, 102L, IssueLinkDependencyBehavior.NONE)
        ));
        when(workItemPlanPort.listActivePlansByWorkItemIds(TENANT_ID, List.of(101L, 102L))).thenReturn(List.of());

        WorkItemDependenciesPageView defaultResult = handler.handle(query(criteria));

        assertEquals(0, defaultResult.edges().size());
        assertEquals(0, defaultResult.summary().dependencyCount());
        assertEquals(0, defaultResult.summary().relatedLinkCount());

        criteria.setIncludeRelatedLinks(true);
        WorkItemDependenciesPageView includeRelatedResult = handler.handle(query(criteria));

        assertEquals(1, includeRelatedResult.edges().size());
        assertTrue(includeRelatedResult.edges().getFirst().relatedLink());
        assertEquals(0, includeRelatedResult.summary().dependencyCount());
        assertEquals(1, includeRelatedResult.summary().relatedLinkCount());
    }

    @Test
    void handleShouldMarkCyclesWithinTheExpandedDepth() {
        WorkItemDependencyCriteria criteria = baseCriteria();
        stubProject();
        when(workItemReadPort.searchWorkItems(eq(TENANT_ID), any())).thenReturn(new PageResult<>(
                List.of(item(101L), item(102L), item(103L)),
                3
        ));
        when(issueLinkPort.listByWorkItemIds(TENANT_ID, List.of(101L, 102L, 103L))).thenReturn(List.of(
                link(501L, 101L, 102L, IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET),
                link(502L, 102L, 103L, IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET),
                link(503L, 103L, 101L, IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET)
        ));
        when(workItemPlanPort.listActivePlansByWorkItemIds(TENANT_ID, List.of(101L, 102L, 103L)))
                .thenReturn(List.of());

        WorkItemDependenciesPageView result = handler.handle(query(criteria));

        assertEquals(3, result.summary().cycleCount());
        assertTrue(result.edges().stream().allMatch(WorkItemDependencyEdgeView::cycle));
        assertTrue(result.nodes().stream().allMatch(WorkItemDependencyNodeView::hasCycle));
        assertEquals(2, result.depth());
        assertTrue(result.includeOutside());
        assertFalse(result.includeRelatedLinks());
    }

    private ListWorkItemDependenciesQuery query(WorkItemDependencyCriteria criteria) {
        return new ListWorkItemDependenciesQuery(
                TENANT_ID,
                USER_ID,
                Set.of("dev-team"),
                criteria
        );
    }

    private WorkItemDependencyCriteria baseCriteria() {
        return WorkItemDependencyCriteria.builder()
                .projectId(PROJECT_ID)
                .page(0)
                .pageSize(20)
                .build();
    }

    private void stubProject() {
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .build());
    }

    private WorkItemEntity item(Long id) {
        return WorkItemEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .issueTypeId(20L)
                .key("SERP-" + id)
                .summary("Work item " + id)
                .statusId(30L)
                .statusName("In Progress")
                .priorityId(40L)
                .priorityName("High")
                .assigneeId(50L)
                .dueDate(1_800_000_000_000L)
                .build();
    }

    private IssueLinkDetailEntity link(Long linkId,
                                       Long sourceId,
                                       Long targetId,
                                       IssueLinkDependencyBehavior dependencyBehavior) {
        return IssueLinkDetailEntity.builder()
                .linkId(linkId)
                .sourceId(sourceId)
                .targetId(targetId)
                .linkTypeId(70L)
                .linkTypeName(dependencyBehavior.name())
                .dependencyBehavior(dependencyBehavior)
                .build();
    }
}
