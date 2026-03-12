/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.core.domain.entity.IssueTypeEntity;
import serp.project.pmcore.core.domain.entity.PriorityEntity;
import serp.project.pmcore.core.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.core.domain.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.core.domain.entity.StatusCategoryEntity;
import serp.project.pmcore.core.domain.entity.StatusEntity;
import serp.project.pmcore.core.domain.entity.TenantSchemeMappingEntity;
import serp.project.pmcore.core.domain.entity.TenantWorkflowMappingEntity;
import serp.project.pmcore.core.domain.entity.WorkflowEntity;
import serp.project.pmcore.core.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.core.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.core.domain.entity.WorkflowStepEntity;
import serp.project.pmcore.core.domain.entity.WorkflowTransitionEntity;
import serp.project.pmcore.core.domain.entity.WorkflowTransitionRuleEntity;
import serp.project.pmcore.core.domain.enums.SchemeType;
import serp.project.pmcore.core.domain.enums.TransitionRuleStage;
import serp.project.pmcore.core.port.store.IBlueprintSchemeDefaultPort;
import serp.project.pmcore.core.port.store.IIssueTypePort;
import serp.project.pmcore.core.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.core.port.store.IIssueTypeSchemePort;
import serp.project.pmcore.core.port.store.IPriorityPort;
import serp.project.pmcore.core.port.store.IPrioritySchemeItemPort;
import serp.project.pmcore.core.port.store.IPrioritySchemePort;
import serp.project.pmcore.core.port.store.IStatusCategoryPort;
import serp.project.pmcore.core.port.store.IStatusPort;
import serp.project.pmcore.core.port.store.ITenantSchemeMappingPort;
import serp.project.pmcore.core.port.store.ITenantWorkflowMappingPort;
import serp.project.pmcore.core.port.store.IWorkflowPort;
import serp.project.pmcore.core.port.store.IWorkflowSchemeItemPort;
import serp.project.pmcore.core.port.store.IWorkflowSchemePort;
import serp.project.pmcore.core.port.store.IWorkflowStepPort;
import serp.project.pmcore.core.port.store.IWorkflowTransitionPort;
import serp.project.pmcore.core.port.store.IWorkflowTransitionRulePort;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemeProvisioningServiceTest {

    @Mock
    private IBlueprintSchemeDefaultPort blueprintSchemeDefaultPort;
    @Mock
    private IIssueTypePort issueTypePort;
    @Mock
    private IIssueTypeSchemePort issueTypeSchemePort;
    @Mock
    private IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    @Mock
    private IPriorityPort priorityPort;
    @Mock
    private IPrioritySchemePort prioritySchemePort;
    @Mock
    private IPrioritySchemeItemPort prioritySchemeItemPort;
    @Mock
    private IStatusCategoryPort statusCategoryPort;
    @Mock
    private IStatusPort statusPort;
    @Mock
    private IWorkflowPort workflowPort;
    @Mock
    private IWorkflowStepPort workflowStepPort;
    @Mock
    private IWorkflowTransitionPort workflowTransitionPort;
    @Mock
    private IWorkflowTransitionRulePort workflowTransitionRulePort;
    @Mock
    private IWorkflowSchemePort workflowSchemePort;
    @Mock
    private IWorkflowSchemeItemPort workflowSchemeItemPort;
    @Mock
    private ITenantSchemeMappingPort tenantSchemeMappingPort;
    @Mock
    private ITenantWorkflowMappingPort tenantWorkflowMappingPort;

    @InjectMocks
    private SchemeProvisioningService schemeProvisioningService;

    @Test
    void resolveSharedSchemeBinding_shouldCloneWorkflowSchemeAndRemapGraph() {
        Long tenantId = 1L;
        Long userId = 9L;
        Long sourceSchemeId = 10L;

        WorkflowSchemeEntity sourceScheme = WorkflowSchemeEntity.builder()
                .id(sourceSchemeId)
                .tenantId(0L)
                .name("Default workflow scheme")
                .defaultWorkflowId(100L)
                .build();

        WorkflowSchemeItemEntity sourceItem = WorkflowSchemeItemEntity.builder()
                .id(11L)
                .tenantId(0L)
                .schemeId(sourceSchemeId)
                .issueTypeId(200L)
                .workflowId(100L)
                .build();

        IssueTypeEntity sourceIssueType = IssueTypeEntity.builder()
                .id(200L)
                .tenantId(0L)
                .typeKey("bug")
                .name("Bug")
                .hierarchyLevel(0)
                .isSystem(true)
                .build();

        WorkflowEntity sourceWorkflow = WorkflowEntity.builder()
                .id(100L)
                .tenantId(0L)
                .name("Default workflow")
                .description("System workflow")
                .versionNo(1)
                .isActive(true)
                .isSystem(true)
                .build();

        WorkflowStepEntity sourceStep1 = WorkflowStepEntity.builder()
                .id(3001L)
                .tenantId(0L)
                .workflowId(100L)
                .statusId(300L)
                .sequence(1)
                .isInitial(true)
                .isFinal(false)
                .build();
        WorkflowStepEntity sourceStep2 = WorkflowStepEntity.builder()
                .id(3002L)
                .tenantId(0L)
                .workflowId(100L)
                .statusId(301L)
                .sequence(2)
                .isInitial(false)
                .isFinal(true)
                .build();

        WorkflowTransitionEntity sourceTransition = WorkflowTransitionEntity.builder()
                .id(400L)
                .tenantId(0L)
                .workflowId(100L)
                .name("Start progress")
                .fromStatusId(300L)
                .toStatusId(301L)
                .sequence(1)
                .build();

        WorkflowTransitionRuleEntity sourceRule = WorkflowTransitionRuleEntity.builder()
                .id(401L)
                .tenantId(0L)
                .transitionId(400L)
                .ruleStage(TransitionRuleStage.VALIDATOR)
                .ruleKey("assignee-required")
                .configJson("{}")
                .sequence(1)
                .isEnabled(true)
                .build();

        StatusCategoryEntity sourceCategoryTodo = StatusCategoryEntity.builder()
                .id(500L)
                .tenantId(0L)
                .key("todo")
                .name("To Do")
                .color("#A0A0A0")
                .isSystem(true)
                .build();
        StatusCategoryEntity sourceCategoryDone = StatusCategoryEntity.builder()
                .id(501L)
                .tenantId(0L)
                .key("done")
                .name("Done")
                .color("#00AA66")
                .isSystem(true)
                .build();

        StatusEntity sourceStatusTodo = StatusEntity.builder()
                .id(300L)
                .tenantId(0L)
                .statusKey("to_do")
                .name("To Do")
                .categoryId(500L)
                .isSystem(true)
                .build();
        StatusEntity sourceStatusDone = StatusEntity.builder()
                .id(301L)
                .tenantId(0L)
                .statusKey("done")
                .name("Done")
                .categoryId(501L)
                .isSystem(true)
                .build();

        when(workflowSchemePort.getWorkflowSchemeByIdIncludingSystem(sourceSchemeId, tenantId))
                .thenReturn(Optional.of(sourceScheme));
        when(tenantSchemeMappingPort.getMapping(tenantId, SchemeType.WORKFLOW, sourceSchemeId))
                .thenReturn(Optional.empty());
        when(workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeIdIncludingSystem(sourceSchemeId, tenantId))
                .thenReturn(List.of(sourceItem));

        when(issueTypePort.getIssueTypeByIdIncludingSystem(200L, tenantId)).thenReturn(Optional.of(sourceIssueType));
        when(issueTypePort.getIssueTypeByTypeKey(tenantId, "bug")).thenReturn(Optional.empty());
        when(issueTypePort.createIssueType(any(IssueTypeEntity.class)))
                .thenAnswer(invocation -> {
                    IssueTypeEntity created = invocation.getArgument(0);
                    created.setId(1200L);
                    return created;
                });

        when(workflowPort.getWorkflowByIdIncludingSystem(100L, tenantId)).thenReturn(Optional.of(sourceWorkflow));
        when(tenantWorkflowMappingPort.getMapping(tenantId, 100L)).thenReturn(Optional.empty());
        when(workflowStepPort.getWorkflowStepsByWorkflowIdIncludingSystem(100L, tenantId))
                .thenReturn(List.of(sourceStep1, sourceStep2));
        when(workflowTransitionPort.getWorkflowTransitionsByWorkflowIdIncludingSystem(100L, tenantId))
                .thenReturn(List.of(sourceTransition));
        when(workflowTransitionRulePort.getWorkflowTransitionRulesByTransitionIdIncludingSystem(400L, tenantId))
                .thenReturn(List.of(sourceRule));

        when(statusPort.getStatusByIdIncludingSystem(300L, tenantId)).thenReturn(Optional.of(sourceStatusTodo));
        when(statusPort.getStatusByIdIncludingSystem(301L, tenantId)).thenReturn(Optional.of(sourceStatusDone));
        when(statusPort.getStatusByStatusKey(tenantId, "to_do")).thenReturn(Optional.empty());
        when(statusPort.getStatusByStatusKey(tenantId, "done")).thenReturn(Optional.empty());

        when(statusCategoryPort.getStatusCategoryByIdIncludingSystem(500L, tenantId))
                .thenReturn(Optional.of(sourceCategoryTodo));
        when(statusCategoryPort.getStatusCategoryByIdIncludingSystem(501L, tenantId))
                .thenReturn(Optional.of(sourceCategoryDone));
        when(statusCategoryPort.getStatusCategoryByKey(tenantId, "todo")).thenReturn(Optional.empty());
        when(statusCategoryPort.getStatusCategoryByKey(tenantId, "done")).thenReturn(Optional.empty());

        AtomicLong categoryIdSeq = new AtomicLong(1500L);
        when(statusCategoryPort.createStatusCategory(any(StatusCategoryEntity.class)))
                .thenAnswer(invocation -> {
                    StatusCategoryEntity created = invocation.getArgument(0);
                    created.setId(categoryIdSeq.getAndIncrement());
                    return created;
                });

        AtomicLong statusIdSeq = new AtomicLong(1300L);
        when(statusPort.createStatus(any(StatusEntity.class)))
                .thenAnswer(invocation -> {
                    StatusEntity created = invocation.getArgument(0);
                    created.setId(statusIdSeq.getAndIncrement());
                    return created;
                });

        when(workflowPort.createWorkflow(any(WorkflowEntity.class)))
                .thenAnswer(invocation -> {
                    WorkflowEntity created = invocation.getArgument(0);
                    created.setId(1100L);
                    return created;
                });

        when(workflowTransitionPort.createWorkflowTransitions(anyList()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    List<WorkflowTransitionEntity> transitions = invocation.getArgument(0);
                    for (int i = 0; i < transitions.size(); i++) {
                        transitions.get(i).setId(1400L + i);
                    }
                    return transitions;
                });

        when(workflowSchemePort.createWorkflowScheme(any(WorkflowSchemeEntity.class)))
                .thenAnswer(invocation -> {
                    WorkflowSchemeEntity created = invocation.getArgument(0);
                    created.setId(2100L);
                    return created;
                });
        when(workflowSchemeItemPort.createWorkflowSchemeItems(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(workflowTransitionRulePort.createWorkflowTransitionRules(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tenantSchemeMappingPort.saveMapping(any(TenantSchemeMappingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tenantWorkflowMappingPort.saveMapping(any(TenantWorkflowMappingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Long resolvedSchemeId = schemeProvisioningService
                .resolveSharedSchemeBinding(SchemeType.WORKFLOW, sourceSchemeId, tenantId, userId);

        assertEquals(2100L, resolvedSchemeId);

        ArgumentCaptor<WorkflowSchemeEntity> createdSchemeCaptor = ArgumentCaptor.forClass(WorkflowSchemeEntity.class);
        verify(workflowSchemePort).createWorkflowScheme(createdSchemeCaptor.capture());
        assertEquals(tenantId, createdSchemeCaptor.getValue().getTenantId());
        assertEquals(1100L, createdSchemeCaptor.getValue().getDefaultWorkflowId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkflowStepEntity>> createdStepsCaptor = ArgumentCaptor.forClass(List.class);
        verify(workflowStepPort).createWorkflowSteps(createdStepsCaptor.capture());
        List<WorkflowStepEntity> createdSteps = createdStepsCaptor.getValue();
        assertEquals(2, createdSteps.size());
        assertEquals(1100L, createdSteps.get(0).getWorkflowId());
        assertEquals(1100L, createdSteps.get(1).getWorkflowId());
        assertEquals(1300L, createdSteps.get(0).getStatusId());
        assertEquals(1301L, createdSteps.get(1).getStatusId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkflowTransitionEntity>> createdTransitionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(workflowTransitionPort).createWorkflowTransitions(createdTransitionsCaptor.capture());
        List<WorkflowTransitionEntity> createdTransitions = createdTransitionsCaptor.getValue();
        assertEquals(1, createdTransitions.size());
        assertEquals(1100L, createdTransitions.get(0).getWorkflowId());
        assertEquals(1300L, createdTransitions.get(0).getFromStatusId());
        assertEquals(1301L, createdTransitions.get(0).getToStatusId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkflowTransitionRuleEntity>> createdRulesCaptor = ArgumentCaptor.forClass(List.class);
        verify(workflowTransitionRulePort).createWorkflowTransitionRules(createdRulesCaptor.capture());
        List<WorkflowTransitionRuleEntity> createdRules = createdRulesCaptor.getValue();
        assertEquals(1, createdRules.size());
        assertEquals(1400L, createdRules.get(0).getTransitionId());
        assertEquals(tenantId, createdRules.get(0).getTenantId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkflowSchemeItemEntity>> createdItemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(workflowSchemeItemPort).createWorkflowSchemeItems(createdItemsCaptor.capture());
        List<WorkflowSchemeItemEntity> createdItems = createdItemsCaptor.getValue();
        assertEquals(1, createdItems.size());
        assertEquals(2100L, createdItems.get(0).getSchemeId());
        assertEquals(1200L, createdItems.get(0).getIssueTypeId());
        assertEquals(1100L, createdItems.get(0).getWorkflowId());

        ArgumentCaptor<TenantSchemeMappingEntity> mappingCaptor = ArgumentCaptor.forClass(TenantSchemeMappingEntity.class);
        verify(tenantSchemeMappingPort).saveMapping(mappingCaptor.capture());
        assertEquals(tenantId, mappingCaptor.getValue().getTenantId());
        assertEquals(SchemeType.WORKFLOW, mappingCaptor.getValue().getSchemeType());
        assertEquals(sourceSchemeId, mappingCaptor.getValue().getSourceSchemeId());
        assertEquals(2100L, mappingCaptor.getValue().getTenantSchemeId());

        ArgumentCaptor<TenantWorkflowMappingEntity> workflowMappingCaptor =
                ArgumentCaptor.forClass(TenantWorkflowMappingEntity.class);
        verify(tenantWorkflowMappingPort).saveMapping(workflowMappingCaptor.capture());
        assertEquals(tenantId, workflowMappingCaptor.getValue().getTenantId());
        assertEquals(100L, workflowMappingCaptor.getValue().getSourceWorkflowId());
        assertEquals(1100L, workflowMappingCaptor.getValue().getTenantWorkflowId());
    }

    @Test
    void resolveSharedSchemeBinding_shouldReuseExistingWorkflowCloneByMapping() {
        Long tenantId = 1L;
        Long userId = 9L;
        Long sourceSchemeId = 30L;

        WorkflowSchemeEntity sourceScheme = WorkflowSchemeEntity.builder()
                .id(sourceSchemeId)
                .tenantId(0L)
                .name("Default workflow scheme")
                .defaultWorkflowId(100L)
                .build();
        WorkflowSchemeItemEntity sourceItem = WorkflowSchemeItemEntity.builder()
                .id(31L)
                .tenantId(0L)
                .schemeId(sourceSchemeId)
                .issueTypeId(200L)
                .workflowId(100L)
                .build();

        IssueTypeEntity sourceIssueType = IssueTypeEntity.builder()
                .id(200L)
                .tenantId(0L)
                .typeKey("bug")
                .name("Bug")
                .hierarchyLevel(0)
                .isSystem(true)
                .build();

        WorkflowEntity sourceWorkflow = WorkflowEntity.builder()
                .id(100L)
                .tenantId(0L)
                .name("Default workflow")
                .versionNo(1)
                .isActive(true)
                .isSystem(true)
                .build();
        WorkflowEntity existingTenantWorkflow = WorkflowEntity.builder()
                .id(1100L)
                .tenantId(tenantId)
                .name("Tenant shared clone")
                .versionNo(1)
                .isActive(true)
                .isSystem(false)
                .build();

        when(workflowSchemePort.getWorkflowSchemeByIdIncludingSystem(sourceSchemeId, tenantId))
                .thenReturn(Optional.of(sourceScheme));
        when(tenantSchemeMappingPort.getMapping(tenantId, SchemeType.WORKFLOW, sourceSchemeId))
                .thenReturn(Optional.empty());
        when(workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeIdIncludingSystem(sourceSchemeId, tenantId))
                .thenReturn(List.of(sourceItem));

        when(issueTypePort.getIssueTypeByIdIncludingSystem(200L, tenantId)).thenReturn(Optional.of(sourceIssueType));
        when(issueTypePort.getIssueTypeByTypeKey(tenantId, "bug")).thenReturn(Optional.empty());
        when(issueTypePort.createIssueType(any(IssueTypeEntity.class)))
                .thenAnswer(invocation -> {
                    IssueTypeEntity created = invocation.getArgument(0);
                    created.setId(1200L);
                    return created;
                });

        when(workflowPort.getWorkflowByIdIncludingSystem(100L, tenantId)).thenReturn(Optional.of(sourceWorkflow));
        when(tenantWorkflowMappingPort.getMapping(tenantId, 100L))
                .thenReturn(Optional.of(
                        TenantWorkflowMappingEntity.builder()
                                .id(501L)
                                .tenantId(tenantId)
                                .sourceWorkflowId(100L)
                                .tenantWorkflowId(1100L)
                                .build()
                ));
        when(workflowPort.getWorkflowById(1100L, tenantId)).thenReturn(Optional.of(existingTenantWorkflow));

        when(workflowSchemePort.createWorkflowScheme(any(WorkflowSchemeEntity.class)))
                .thenAnswer(invocation -> {
                    WorkflowSchemeEntity created = invocation.getArgument(0);
                    created.setId(2300L);
                    return created;
                });
        when(workflowSchemeItemPort.createWorkflowSchemeItems(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tenantSchemeMappingPort.saveMapping(any(TenantSchemeMappingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Long resolvedSchemeId = schemeProvisioningService
                .resolveSharedSchemeBinding(SchemeType.WORKFLOW, sourceSchemeId, tenantId, userId);

        assertEquals(2300L, resolvedSchemeId);
        verify(workflowPort, never()).createWorkflow(any(WorkflowEntity.class));
        verify(workflowStepPort, never()).createWorkflowSteps(anyList());
        verify(workflowTransitionPort, never()).createWorkflowTransitions(anyList());
        verify(workflowTransitionRulePort, never()).createWorkflowTransitionRules(anyList());
        verify(tenantWorkflowMappingPort, never()).saveMapping(any(TenantWorkflowMappingEntity.class));

        ArgumentCaptor<WorkflowSchemeEntity> createdSchemeCaptor = ArgumentCaptor.forClass(WorkflowSchemeEntity.class);
        verify(workflowSchemePort).createWorkflowScheme(createdSchemeCaptor.capture());
        assertEquals(1100L, createdSchemeCaptor.getValue().getDefaultWorkflowId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkflowSchemeItemEntity>> createdItemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(workflowSchemeItemPort).createWorkflowSchemeItems(createdItemsCaptor.capture());
        List<WorkflowSchemeItemEntity> createdItems = createdItemsCaptor.getValue();
        assertEquals(1, createdItems.size());
        assertEquals(1100L, createdItems.get(0).getWorkflowId());
        assertEquals(1200L, createdItems.get(0).getIssueTypeId());
    }

    @Test
    void resolveSharedSchemeBinding_shouldReuseExistingPriorityByDerivedKey() {
        Long tenantId = 1L;
        Long userId = 9L;
        Long sourceSchemeId = 20L;

        PrioritySchemeEntity sourceScheme = PrioritySchemeEntity.builder()
                .id(sourceSchemeId)
                .tenantId(0L)
                .name("Default priorities")
                .defaultPriorityId(600L)
                .build();
        PrioritySchemeItemEntity sourceItem = PrioritySchemeItemEntity.builder()
                .id(21L)
                .tenantId(0L)
                .schemeId(sourceSchemeId)
                .priorityId(600L)
                .sequence(1)
                .build();
        PriorityEntity sourcePriority = PriorityEntity.builder()
                .id(600L)
                .tenantId(0L)
                .name("High Urgent")
                .priorityKey(null)
                .sequence(1)
                .isSystem(true)
                .build();

        PriorityEntity existingTenantPriority = PriorityEntity.builder()
                .id(1600L)
                .tenantId(tenantId)
                .priorityKey("high_urgent")
                .name("High Urgent")
                .sequence(1)
                .isSystem(false)
                .build();

        when(prioritySchemePort.getPrioritySchemeByIdIncludingSystem(sourceSchemeId, tenantId))
                .thenReturn(Optional.of(sourceScheme));
        when(tenantSchemeMappingPort.getMapping(tenantId, SchemeType.PRIORITY, sourceSchemeId))
                .thenReturn(Optional.empty());
        when(prioritySchemeItemPort.getPrioritySchemeItemsBySchemeIdIncludingSystem(sourceSchemeId, tenantId))
                .thenReturn(List.of(sourceItem));
        when(priorityPort.getPriorityByIdIncludingSystem(600L, tenantId)).thenReturn(Optional.of(sourcePriority));
        when(priorityPort.getPriorityByPriorityKey(tenantId, "high_urgent"))
                .thenReturn(Optional.of(existingTenantPriority));

        when(prioritySchemePort.createPriorityScheme(any(PrioritySchemeEntity.class)))
                .thenAnswer(invocation -> {
                    PrioritySchemeEntity created = invocation.getArgument(0);
                    created.setId(2200L);
                    return created;
                });
        when(prioritySchemeItemPort.createPrioritySchemeItems(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tenantSchemeMappingPort.saveMapping(any(TenantSchemeMappingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Long resolvedSchemeId = schemeProvisioningService
                .resolveSharedSchemeBinding(SchemeType.PRIORITY, sourceSchemeId, tenantId, userId);

        assertEquals(2200L, resolvedSchemeId);
        verify(priorityPort, never()).createPriority(any(PriorityEntity.class));

        ArgumentCaptor<PrioritySchemeEntity> createdSchemeCaptor = ArgumentCaptor.forClass(PrioritySchemeEntity.class);
        verify(prioritySchemePort).createPriorityScheme(createdSchemeCaptor.capture());
        assertEquals(1600L, createdSchemeCaptor.getValue().getDefaultPriorityId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PrioritySchemeItemEntity>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(prioritySchemeItemPort).createPrioritySchemeItems(itemsCaptor.capture());
        List<PrioritySchemeItemEntity> createdItems = itemsCaptor.getValue();
        assertFalse(createdItems.isEmpty());
        assertEquals(1600L, createdItems.get(0).getPriorityId());
        assertEquals(2200L, createdItems.get(0).getSchemeId());

        verify(priorityPort).getPriorityByPriorityKey(tenantId, "high_urgent");
    }

    @Test
    void resolveClonedSchemeBinding_shouldCloneWorkflowSchemeWithoutReusingSharedWorkflowMapping() {
        Long tenantId = 1L;
        Long userId = 9L;
        Long sourceSchemeId = 40L;

        WorkflowSchemeEntity sourceScheme = WorkflowSchemeEntity.builder()
                .id(sourceSchemeId)
                .tenantId(0L)
                .name("Default workflow scheme")
                .defaultWorkflowId(100L)
                .build();
        WorkflowSchemeItemEntity sourceItem = WorkflowSchemeItemEntity.builder()
                .id(41L)
                .tenantId(0L)
                .schemeId(sourceSchemeId)
                .issueTypeId(200L)
                .workflowId(100L)
                .build();

        IssueTypeEntity sourceIssueType = IssueTypeEntity.builder()
                .id(200L)
                .tenantId(0L)
                .typeKey("bug")
                .name("Bug")
                .hierarchyLevel(0)
                .isSystem(true)
                .build();

        WorkflowEntity sourceWorkflow = WorkflowEntity.builder()
                .id(100L)
                .tenantId(0L)
                .name("Default workflow")
                .description("System workflow")
                .versionNo(1)
                .isActive(true)
                .isSystem(true)
                .build();

        WorkflowStepEntity sourceStep1 = WorkflowStepEntity.builder()
                .id(3001L)
                .tenantId(0L)
                .workflowId(100L)
                .statusId(300L)
                .sequence(1)
                .isInitial(true)
                .isFinal(false)
                .build();
        WorkflowStepEntity sourceStep2 = WorkflowStepEntity.builder()
                .id(3002L)
                .tenantId(0L)
                .workflowId(100L)
                .statusId(301L)
                .sequence(2)
                .isInitial(false)
                .isFinal(true)
                .build();

        WorkflowTransitionEntity sourceTransition = WorkflowTransitionEntity.builder()
                .id(400L)
                .tenantId(0L)
                .workflowId(100L)
                .name("Start progress")
                .fromStatusId(300L)
                .toStatusId(301L)
                .sequence(1)
                .build();

        WorkflowTransitionRuleEntity sourceRule = WorkflowTransitionRuleEntity.builder()
                .id(401L)
                .tenantId(0L)
                .transitionId(400L)
                .ruleStage(TransitionRuleStage.VALIDATOR)
                .ruleKey("assignee-required")
                .configJson("{}")
                .sequence(1)
                .isEnabled(true)
                .build();

        StatusCategoryEntity sourceCategoryTodo = StatusCategoryEntity.builder()
                .id(500L)
                .tenantId(0L)
                .key("todo")
                .name("To Do")
                .color("#A0A0A0")
                .isSystem(true)
                .build();
        StatusCategoryEntity sourceCategoryDone = StatusCategoryEntity.builder()
                .id(501L)
                .tenantId(0L)
                .key("done")
                .name("Done")
                .color("#00AA66")
                .isSystem(true)
                .build();

        StatusEntity sourceStatusTodo = StatusEntity.builder()
                .id(300L)
                .tenantId(0L)
                .statusKey("to_do")
                .name("To Do")
                .categoryId(500L)
                .isSystem(true)
                .build();
        StatusEntity sourceStatusDone = StatusEntity.builder()
                .id(301L)
                .tenantId(0L)
                .statusKey("done")
                .name("Done")
                .categoryId(501L)
                .isSystem(true)
                .build();

        when(workflowSchemePort.getWorkflowSchemeByIdIncludingSystem(sourceSchemeId, tenantId))
                .thenReturn(Optional.of(sourceScheme));
        when(workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeIdIncludingSystem(sourceSchemeId, tenantId))
                .thenReturn(List.of(sourceItem));

        when(issueTypePort.getIssueTypeByIdIncludingSystem(200L, tenantId)).thenReturn(Optional.of(sourceIssueType));
        when(issueTypePort.getIssueTypeByTypeKey(tenantId, "bug")).thenReturn(Optional.empty());
        when(issueTypePort.createIssueType(any(IssueTypeEntity.class)))
                .thenAnswer(invocation -> {
                    IssueTypeEntity created = invocation.getArgument(0);
                    created.setId(1200L);
                    return created;
                });

        when(workflowPort.getWorkflowByIdIncludingSystem(100L, tenantId)).thenReturn(Optional.of(sourceWorkflow));
        when(workflowStepPort.getWorkflowStepsByWorkflowIdIncludingSystem(100L, tenantId))
                .thenReturn(List.of(sourceStep1, sourceStep2));
        when(workflowTransitionPort.getWorkflowTransitionsByWorkflowIdIncludingSystem(100L, tenantId))
                .thenReturn(List.of(sourceTransition));
        when(workflowTransitionRulePort.getWorkflowTransitionRulesByTransitionIdIncludingSystem(400L, tenantId))
                .thenReturn(List.of(sourceRule));

        when(statusPort.getStatusByIdIncludingSystem(300L, tenantId)).thenReturn(Optional.of(sourceStatusTodo));
        when(statusPort.getStatusByIdIncludingSystem(301L, tenantId)).thenReturn(Optional.of(sourceStatusDone));
        when(statusPort.getStatusByStatusKey(tenantId, "to_do")).thenReturn(Optional.empty());
        when(statusPort.getStatusByStatusKey(tenantId, "done")).thenReturn(Optional.empty());

        when(statusCategoryPort.getStatusCategoryByIdIncludingSystem(500L, tenantId))
                .thenReturn(Optional.of(sourceCategoryTodo));
        when(statusCategoryPort.getStatusCategoryByIdIncludingSystem(501L, tenantId))
                .thenReturn(Optional.of(sourceCategoryDone));
        when(statusCategoryPort.getStatusCategoryByKey(tenantId, "todo")).thenReturn(Optional.empty());
        when(statusCategoryPort.getStatusCategoryByKey(tenantId, "done")).thenReturn(Optional.empty());

        AtomicLong categoryIdSeq = new AtomicLong(2500L);
        when(statusCategoryPort.createStatusCategory(any(StatusCategoryEntity.class)))
                .thenAnswer(invocation -> {
                    StatusCategoryEntity created = invocation.getArgument(0);
                    created.setId(categoryIdSeq.getAndIncrement());
                    return created;
                });

        AtomicLong statusIdSeq = new AtomicLong(2300L);
        when(statusPort.createStatus(any(StatusEntity.class)))
                .thenAnswer(invocation -> {
                    StatusEntity created = invocation.getArgument(0);
                    created.setId(statusIdSeq.getAndIncrement());
                    return created;
                });

        when(workflowPort.createWorkflow(any(WorkflowEntity.class)))
                .thenAnswer(invocation -> {
                    WorkflowEntity created = invocation.getArgument(0);
                    created.setId(2100L);
                    return created;
                });

        when(workflowTransitionPort.createWorkflowTransitions(anyList()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    List<WorkflowTransitionEntity> transitions = invocation.getArgument(0);
                    for (int i = 0; i < transitions.size(); i++) {
                        transitions.get(i).setId(2400L + i);
                    }
                    return transitions;
                });

        when(workflowSchemePort.createWorkflowScheme(any(WorkflowSchemeEntity.class)))
                .thenAnswer(invocation -> {
                    WorkflowSchemeEntity created = invocation.getArgument(0);
                    created.setId(3100L);
                    return created;
                });
        when(workflowSchemeItemPort.createWorkflowSchemeItems(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(workflowTransitionRulePort.createWorkflowTransitionRules(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Long resolvedSchemeId = schemeProvisioningService
                .resolveClonedSchemeBinding(SchemeType.WORKFLOW, sourceSchemeId, tenantId, userId);

        assertEquals(3100L, resolvedSchemeId);

        verify(tenantWorkflowMappingPort, never()).saveMapping(any(TenantWorkflowMappingEntity.class));
        verify(tenantSchemeMappingPort, never()).saveMapping(any(TenantSchemeMappingEntity.class));

        ArgumentCaptor<WorkflowSchemeEntity> createdSchemeCaptor = ArgumentCaptor.forClass(WorkflowSchemeEntity.class);
        verify(workflowSchemePort).createWorkflowScheme(createdSchemeCaptor.capture());
        assertEquals(tenantId, createdSchemeCaptor.getValue().getTenantId());
        assertEquals(2100L, createdSchemeCaptor.getValue().getDefaultWorkflowId());
    }
}
