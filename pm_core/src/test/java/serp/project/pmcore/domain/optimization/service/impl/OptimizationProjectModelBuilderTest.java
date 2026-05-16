/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.domain.issuelink.enums.IssueLinkDependencyBehavior;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkPort;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkTypePort;
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptimizationProjectModelBuilderTest {

    @Mock
    private IProjectReadPort projectReadPort;

    @Mock
    private IWorkItemReadPort workItemReadPort;

    @Mock
    private IWorkItemPlanPort workItemPlanPort;

    @Mock
    private IIssueLinkPort issueLinkPort;

    @Mock
    private IIssueLinkTypePort issueLinkTypePort;

    @InjectMocks
    private OptimizationProjectModelBuilder builder;

    @Test
    void buildShouldCreateDependencyGraphFromLinkBehavior() {
        stubProject();
        when(workItemReadPort.listActiveByWorkItemIds(eq(1L), anyList())).thenReturn(List.of(item(10L), item(20L)));
        when(workItemPlanPort.listActivePlansByWorkItemIds(eq(1L), anyList())).thenReturn(List.of());
        when(issueLinkTypePort.listByTenant(1L)).thenReturn(List.of(linkType(IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET)));
        when(issueLinkPort.listByWorkItemId(1L, 10L)).thenReturn(List.of(link(10L, 20L)));
        when(issueLinkPort.listByWorkItemId(1L, 20L)).thenReturn(List.of(link(10L, 20L)));

        OptimizationProjectModel model = builder.build(input(List.of(10L, 20L)));

        assertEquals(1, model.dependencyGraph().internalEdges().size());
        assertEquals(10L, model.dependencyGraph().internalEdges().get(0).predecessorId());
        assertEquals(20L, model.dependencyGraph().internalEdges().get(0).successorId());
        assertEquals(List.of(10L, 20L), model.dependencyGraph().topologicalOrder());
    }

    @Test
    void buildShouldDetectDependencyCycle() {
        stubProject();
        when(workItemReadPort.listActiveByWorkItemIds(eq(1L), anyList())).thenReturn(List.of(item(10L), item(20L)));
        when(workItemPlanPort.listActivePlansByWorkItemIds(eq(1L), anyList())).thenReturn(List.of());
        when(issueLinkTypePort.listByTenant(1L)).thenReturn(List.of(linkType(IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET)));
        when(issueLinkPort.listByWorkItemId(1L, 10L)).thenReturn(List.of(link(10L, 20L), link(20L, 10L)));
        when(issueLinkPort.listByWorkItemId(1L, 20L)).thenReturn(List.of(link(10L, 20L), link(20L, 10L)));

        OptimizationProjectModel model = builder.build(input(List.of(10L, 20L)));

        assertTrue(model.dependencyGraph().hasCycles());
        assertTrue(model.warnings().stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.DEPENDENCY_CYCLE));
    }

    @Test
    void buildShouldUseDefaultDurationAndWarnWhenEstimateMissing() {
        stubProject();
        when(workItemReadPort.listActiveByWorkItemIds(eq(1L), anyList())).thenReturn(List.of(item(10L)));
        when(workItemPlanPort.listActivePlansByWorkItemIds(eq(1L), anyList())).thenReturn(List.of());
        when(issueLinkTypePort.listByTenant(1L)).thenReturn(List.of());
        when(issueLinkPort.listByWorkItemId(1L, 10L)).thenReturn(List.of());

        OptimizationProjectModel model = builder.build(input(List.of(10L)));

        assertEquals(86_400_000L, model.workItems().get(0).duration().durationMillis());
        assertTrue(model.warnings().stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.DEFAULT_DURATION_USED));
    }

    @Test
    void buildShouldResolveCandidateAssigneesDeterministically() {
        stubProject();
        WorkItemEntity item = item(10L);
        item.setAssigneeId(300L);
        item.setReporterId(200L);
        when(workItemReadPort.listActiveByWorkItemIds(eq(1L), anyList())).thenReturn(List.of(item));
        when(workItemPlanPort.listActivePlansByWorkItemIds(eq(1L), anyList())).thenReturn(List.of());
        when(issueLinkTypePort.listByTenant(1L)).thenReturn(List.of());
        when(issueLinkPort.listByWorkItemId(1L, 10L)).thenReturn(List.of());

        OptimizationProjectModel model = builder.build(input(List.of(10L)));

        assertEquals(List.of(300L, 100L, 200L), model.workItems().get(0).candidateAssignees().stream()
                .map(candidate -> candidate.candidateId())
                .toList());
        assertFalse(model.capacitySlots().isEmpty());
    }

    private void stubProject() {
        when(projectReadPort.getProjectById(100L, 1L)).thenReturn(Optional.of(ProjectEntity.builder()
                .id(100L)
                .tenantId(1L)
                .leadUserId(100L)
                .build()));
    }

    private OptimizationBuilderInput input(List<Long> ids) {
        return new OptimizationBuilderInput(1L, 100L, ids, 1_714_876_800_000L, 1_715_481_600_000L,
                true, true, OptimizationMode.BALANCED_WORKLOAD);
    }

    private WorkItemEntity item(Long id) {
        return WorkItemEntity.builder()
                .id(id)
                .tenantId(1L)
                .projectId(100L)
                .prioritySequence(1)
                .issueTypeHierarchyLevel(0)
                .build();
    }

    private IssueLinkTypeEntity linkType(IssueLinkDependencyBehavior behavior) {
        return IssueLinkTypeEntity.builder()
                .id(1L)
                .tenantId(1L)
                .dependencyBehavior(behavior)
                .build();
    }

    private IssueLinkDetailEntity link(Long sourceId, Long targetId) {
        return IssueLinkDetailEntity.builder()
                .linkId(sourceId * 100 + targetId)
                .sourceId(sourceId)
                .targetId(targetId)
                .linkTypeId(1L)
                .build();
    }
}
