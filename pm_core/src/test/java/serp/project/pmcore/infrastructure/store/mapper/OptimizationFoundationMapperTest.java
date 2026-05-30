/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.domain.issuelink.enums.IssueLinkDependencyBehavior;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationApplyStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.infrastructure.store.model.IssueLinkTypeModel;
import serp.project.pmcore.infrastructure.store.model.OptimizationRunItemModel;
import serp.project.pmcore.infrastructure.store.model.OptimizationRunModel;
import serp.project.pmcore.infrastructure.store.model.OptimizationRunWarningModel;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanModel;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizationFoundationMapperTest {

    @Test
    void issueLinkTypeMapperShouldMapDependencyBehavior() {
        IssueLinkTypeMapper mapper = new IssueLinkTypeMapper();
        IssueLinkTypeEntity entity = IssueLinkTypeEntity.builder()
                .id(600L)
                .tenantId(0L)
                .name("Blocks")
                .outwardDescription("blocks")
                .inwardDescription("is blocked by")
                .dependencyBehavior(IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET)
                .isSystem(true)
                .build();

        IssueLinkTypeModel model = mapper.toModel(entity);
        IssueLinkTypeEntity mapped = mapper.toEntity(model);

        assertThat(model.getDependencyBehavior()).isEqualTo(IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET);
        assertThat(mapped.getDependencyBehavior()).isEqualTo(IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET);
    }

    @Test
    void workItemPlanMapperShouldMapPlanningFields() {
        WorkItemPlanMapper mapper = new WorkItemPlanMapper();
        WorkItemPlanEntity entity = WorkItemPlanEntity.builder()
                .id(1L)
                .tenantId(2L)
                .projectId(3L)
                .workItemId(4L)
                .plannedStart(1715523600000L)
                .plannedEnd(1715610000000L)
                .source(WorkItemPlanSource.OPTIMIZATION)
                .sourceRunId(5L)
                .locked(false)
                .build();

        WorkItemPlanModel model = mapper.toModel(entity);
        WorkItemPlanEntity mapped = mapper.toEntity(model);

        assertThat(mapped.getWorkItemId()).isEqualTo(4L);
        assertThat(mapped.getPlannedStart()).isEqualTo(1715523600000L);
        assertThat(mapped.getPlannedEnd()).isEqualTo(1715610000000L);
        assertThat(mapped.getSource()).isEqualTo(WorkItemPlanSource.OPTIMIZATION);
        assertThat(mapped.getSourceRunId()).isEqualTo(5L);
    }

    @Test
    void optimizationRunMapperShouldMapStatusAndOptions() {
        OptimizationRunMapper mapper = new OptimizationRunMapper();
        OptimizationRunEntity entity = OptimizationRunEntity.builder()
                .id(10L)
                .tenantId(2L)
                .projectId(3L)
                .scope("SELECTED_WORK_ITEMS")
                .objective(OptimizationObjective.BALANCED_WORKLOAD.name())
                .changeScope(OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE.name())
                .status(OptimizationRunStatus.GENERATED)
                .planningStart(1715523600000L)
                .planningEnd(1716733200000L)
                .selectedWorkItemCount(3)
                .summaryJson("{}")
                .algorithmKey("greedy-balanced")
                .algorithmVersion("v1")
                .solverStatus("FEASIBLE")
                .objectiveScore(BigDecimal.valueOf(12.345678))
                .build();

        OptimizationRunModel model = mapper.toModel(entity);
        OptimizationRunEntity mapped = mapper.toEntity(model);

        assertThat(mapped.getStatus()).isEqualTo(OptimizationRunStatus.GENERATED);
        assertThat(mapped.getObjective()).isEqualTo(OptimizationObjective.BALANCED_WORKLOAD.name());
        assertThat(mapped.getChangeScope()).isEqualTo(OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE.name());
        assertThat(mapped.getSelectedWorkItemCount()).isEqualTo(3);
        assertThat(model.getAlgorithmKey()).isEqualTo("greedy-balanced");
        assertThat(model.getAlgorithmVersion()).isEqualTo("v1");
        assertThat(model.getSolverStatus()).isEqualTo("FEASIBLE");
        assertThat(model.getObjectiveScore()).isEqualByComparingTo("12.345678");
        assertThat(mapped.getAlgorithmKey()).isEqualTo("greedy-balanced");
        assertThat(mapped.getAlgorithmVersion()).isEqualTo("v1");
        assertThat(mapped.getSolverStatus()).isEqualTo("FEASIBLE");
        assertThat(mapped.getObjectiveScore()).isEqualByComparingTo("12.345678");
    }

    @Test
    void optimizationRunItemMapperShouldMapDecisionsAndScores() {
        OptimizationRunItemMapper mapper = new OptimizationRunItemMapper();
        OptimizationRunItemEntity entity = OptimizationRunItemEntity.builder()
                .id(20L)
                .tenantId(2L)
                .runId(10L)
                .projectId(3L)
                .workItemId(4L)
                .assignmentDecision(OptimizationDecision.PENDING)
                .scheduleDecision(OptimizationDecision.ACCEPTED)
                .assignmentApplyStatus(OptimizationApplyStatus.NOT_APPLIED)
                .scheduleApplyStatus(OptimizationApplyStatus.NOT_APPLIED)
                .score(BigDecimal.valueOf(12.5))
                .cost(BigDecimal.valueOf(2.25))
                .confidence("HIGH")
                .assignmentReasonsJson("[]")
                .scheduleReasonsJson("[]")
                .violationsJson("[]")
                .build();

        OptimizationRunItemModel model = mapper.toModel(entity);
        OptimizationRunItemEntity mapped = mapper.toEntity(model);

        assertThat(mapped.getAssignmentDecision()).isEqualTo(OptimizationDecision.PENDING);
        assertThat(mapped.getScheduleDecision()).isEqualTo(OptimizationDecision.ACCEPTED);
        assertThat(mapped.getScore()).isEqualByComparingTo(BigDecimal.valueOf(12.5));
        assertThat(mapped.getConfidence()).isEqualTo("HIGH");
    }

    @Test
    void optimizationRunWarningMapperShouldMapWarningFields() {
        OptimizationRunWarningMapper mapper = new OptimizationRunWarningMapper();
        OptimizationRunWarningEntity entity = OptimizationRunWarningEntity.builder()
                .id(30L)
                .tenantId(2L)
                .runId(10L)
                .workItemId(4L)
                .severity("WARN")
                .code("DEFAULT_DURATION_USED")
                .message("Default duration used")
                .detailsJson("{}")
                .build();

        OptimizationRunWarningModel model = mapper.toModel(entity);
        OptimizationRunWarningEntity mapped = mapper.toEntity(model);

        assertThat(mapped.getCode()).isEqualTo("DEFAULT_DURATION_USED");
        assertThat(mapped.getMessage()).isEqualTo("Default duration used");
        assertThat(mapped.getDetailsJson()).isEqualTo("{}");
    }
}
