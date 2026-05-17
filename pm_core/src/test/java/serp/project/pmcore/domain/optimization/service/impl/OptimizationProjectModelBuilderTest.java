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
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.model.WorkItemComponentLink;
import serp.project.pmcore.domain.optimization.port.IResourceCapacityPort;
import serp.project.pmcore.domain.optimization.port.IWorkItemComponentReadPort;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.IProjectComponentPort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.service.IProjectMemberService;
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

    @Mock
    private IProjectComponentPort projectComponentPort;

    @Mock
    private IWorkItemComponentReadPort workItemComponentReadPort;

    @Mock
    private IProjectMemberService projectMemberService;

    @Mock
    private IResourceCapacityPort resourceCapacityPort;

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
        stubResourcePorts(List.of(10L, 20L), List.of(100L));

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
        stubResourcePorts(List.of(10L, 20L), List.of(100L));

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
        stubResourcePorts(List.of(10L), List.of(100L));

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
        stubResourcePorts(List.of(10L), List.of(100L));

        OptimizationProjectModel model = builder.build(input(List.of(10L)));

        assertEquals(List.of(300L, 100L, 200L), model.workItems().get(0).candidateAssignees().stream()
                .map(candidate -> candidate.candidateId())
                .toList());
        assertFalse(model.capacitySlots().isEmpty());
    }

    @Test
    void buildShouldIncludeComponentLeadAndAssignableProjectMembers() {
        stubProject();
        WorkItemEntity item = item(10L);
        item.setAssigneeId(300L);
        item.setReporterId(200L);
        when(workItemReadPort.listActiveByWorkItemIds(eq(1L), anyList())).thenReturn(List.of(item));
        when(workItemPlanPort.listActivePlansByWorkItemIds(eq(1L), anyList())).thenReturn(List.of());
        when(issueLinkTypePort.listByTenant(1L)).thenReturn(List.of());
        when(issueLinkPort.listByWorkItemId(1L, 10L)).thenReturn(List.of());
        when(workItemComponentReadPort.listActiveByWorkItemIds(eq(1L), anyList()))
                .thenReturn(List.of(new WorkItemComponentLink(10L, 50L)));
        when(projectComponentPort.getComponentsByIds(anyList(), eq(100L), eq(1L)))
                .thenReturn(List.of(ProjectComponentEntity.builder()
                        .id(50L)
                        .tenantId(1L)
                        .projectId(100L)
                        .leadUserId(400L)
                        .build()));
        when(projectMemberService.listAssignableMembers(org.mockito.ArgumentMatchers.any(ProjectEntity.class)))
                .thenReturn(List.of(500L));
        when(resourceCapacityPort.resolveCapacity(eq(1L), eq(100L), anyList(), eq(1_714_876_800_000L), eq(1_715_481_600_000L), anyList()))
                .thenReturn(capacityResolution(List.of(new ResourceCapacitySlot(400L, 1_714_876_800_000L, 1_714_963_200_000L, 28_800_000L))));

        OptimizationProjectModel model = builder.build(input(List.of(10L)));

        assertEquals(List.of(300L, 400L, 100L, 200L, 500L), model.workItems().get(0).candidateAssignees().stream()
                .map(candidate -> candidate.candidateId())
                .toList());
        assertTrue(model.workItems().get(0).candidateAssignees().stream()
                .anyMatch(candidate -> candidate.candidateId().equals(400L) && candidate.componentLead()));
        assertTrue(model.workItems().get(0).candidateAssignees().stream()
                .anyMatch(candidate -> candidate.candidateId().equals(500L) && candidate.projectMember()));
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

    private void stubResourcePorts(List<Long> workItemIds, List<Long> memberIds) {
        when(workItemComponentReadPort.listActiveByWorkItemIds(eq(1L), eq(workItemIds))).thenReturn(List.of());
        when(projectMemberService.listAssignableMembers(org.mockito.ArgumentMatchers.any(ProjectEntity.class))).thenReturn(memberIds);
        when(resourceCapacityPort.resolveCapacity(eq(1L), eq(100L), anyList(), eq(1_714_876_800_000L), eq(1_715_481_600_000L), anyList()))
                .thenReturn(capacityResolution(List.of(new ResourceCapacitySlot(100L, 1_714_876_800_000L, 1_714_963_200_000L, 28_800_000L))));
    }

    private CapacityResolutionResult capacityResolution(List<ResourceCapacitySlot> slots) {
        return new CapacityResolutionResult(slots, CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC,
                CapacityCoverageStatus.MISSING, CapacityCoverageStatus.NOT_REQUIRED, List.of(100L), null,
                1_714_876_800_000L, 0L, 0L, 0L, List.of(), List.of());
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
