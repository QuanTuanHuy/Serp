/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.workitem.command.transition.internal.ResolvedTransitionExecution;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionWorkItemStatusData;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.enums.TransitionRuleStage;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.IResolutionPort;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemTransitionRuleEvaluatorTest {

    @Mock
    private JsonUtils jsonUtils;
    @Mock
    private IResolutionPort resolutionPort;

    @Test
    void evaluateConditionsShouldAllowWhenUserMatchesAssigneeCondition() {
        WorkItemTransitionRuleEvaluator evaluator = new WorkItemTransitionRuleEvaluator(jsonUtils, resolutionPort);
        ResolvedTransitionExecution execution = executionWithRules(List.of(
                WorkflowTransitionRuleEntity.builder()
                        .id(1L)
                        .ruleStage(TransitionRuleStage.CONDITION)
                        .ruleKey("user_is_assignee")
                        .isEnabled(true)
                        .sequence(1)
                        .build()
        ));
        WorkItemEntity workItem = WorkItemEntity.builder().id(10L).assigneeId(99L).build();
        ProjectEntity project = ProjectEntity.builder().id(20L).leadUserId(7L).build();

        assertDoesNotThrow(() -> evaluator.evaluateConditions(execution, workItem, project, 99L));
    }

    @Test
    void evaluateConditionsShouldRejectWhenUserDoesNotMatchAssigneeCondition() {
        WorkItemTransitionRuleEvaluator evaluator = new WorkItemTransitionRuleEvaluator(jsonUtils, resolutionPort);
        ResolvedTransitionExecution execution = executionWithRules(List.of(
                WorkflowTransitionRuleEntity.builder()
                        .id(1L)
                        .ruleStage(TransitionRuleStage.CONDITION)
                        .ruleKey("user_is_assignee")
                        .isEnabled(true)
                        .sequence(1)
                        .build()
        ));
        WorkItemEntity workItem = WorkItemEntity.builder().id(10L).assigneeId(100L).build();
        ProjectEntity project = ProjectEntity.builder().id(20L).leadUserId(7L).build();

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> evaluator.evaluateConditions(execution, workItem, project, 99L)
        );

        assertEquals(DomainErrorCode.TRANSITION_CONDITION_FAILED, exception.getErrorCode());
    }

    @Test
    void evaluateValidatorsShouldAcceptExistingCustomFieldValueForRequiredField() {
        WorkItemTransitionRuleEvaluator evaluator = new WorkItemTransitionRuleEvaluator(jsonUtils, resolutionPort);
        WorkflowTransitionRuleEntity validatorRule = WorkflowTransitionRuleEntity.builder()
                .id(2L)
                .ruleStage(TransitionRuleStage.VALIDATOR)
                .ruleKey("field_required")
                .configJson("{\"field\":\"customfield_10001\"}")
                .isEnabled(true)
                .sequence(1)
                .build();
        ResolvedTransitionExecution execution = executionWithRules(List.of(validatorRule));
        when(jsonUtils.fromJson(eq(validatorRule.getConfigJson()), any(TypeReference.class)))
                .thenReturn(Map.of("field", "customfield_10001"));

        Long resolutionId = evaluator.evaluateValidatorsAndResolveResolution(
                execution,
                WorkItemEntity.builder().id(10L).build(),
                new TransitionWorkItemStatusData(30L, null, Map.of()),
                null,
                Map.of("customfield_10001", Boolean.TRUE),
                1L
        );

        assertNull(resolutionId);
    }

    private ResolvedTransitionExecution executionWithRules(List<WorkflowTransitionRuleEntity> rules) {
        return new ResolvedTransitionExecution(
                IssueTypeEntity.builder().id(401L).typeKey("task").build(),
                WorkflowVersionEntity.builder().id(501L).build(),
                WorkflowStepEntity.builder().id(601L).statusId(701L).build(),
                WorkflowStepEntity.builder().id(602L).statusId(702L).build(),
                WorkflowTransitionEntity.builder().id(30L).name("Done").build(),
                rules,
                StatusEntity.builder().id(702L).categoryId(801L).build(),
                StatusCategoryEntity.builder().id(801L).key("in_progress").build()
        );
    }
}
