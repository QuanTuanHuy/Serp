/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationApplyStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateSkillFit;
import serp.project.pmcore.domain.optimization.model.OptimizationRunSummary;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OptimizationRunReviewAssemblerTest {
    private final JsonUtils jsonUtils = new JsonUtils(new ObjectMapper());
    private final OptimizationRunReviewAssembler assembler = new OptimizationRunReviewAssembler(jsonUtils);

    @Test
    void toViewShouldExposeSelectedCandidateSkillFitOnly() {
        OptimizationRunSummary summary = OptimizationRunSummary.builder()
                .itemsWithSkillRequirements(1)
                .itemsMissingSkillRequirements(1)
                .candidatesWithSkillProfiles(1)
                .candidatesMissingSkillProfiles(0)
                .requiredSkillMismatchCount(0)
                .skillRankingConfidence(OptimizationConfidence.HIGH.name())
                .selectedCandidateSkillFits(List.of(new OptimizationCandidateSkillFit(
                        10L,
                        200L,
                        1,
                        1,
                        1,
                        1,
                        100D,
                        100D,
                        4D,
                        List.of(),
                        List.of(),
                        List.of(501L, 601L),
                        List.of(501L),
                        List.of(601L),
                        OptimizationConfidence.HIGH)))
                .build();
        OptimizationRunReviewView view = assembler.toView(run(summary), List.of(item(10L, 200L), item(20L, 300L)), List.of());

        assertEquals(1, view.getSummary().getItemsWithSkillRequirements());
        assertEquals(OptimizationConfidence.HIGH.name(), view.getSummary().getSkillRankingConfidence());
        assertEquals("greedy-balanced", view.getAlgorithmKey());
        assertEquals(OptimizationObjective.BALANCED_WORKLOAD.name(), view.getObjective());
        assertEquals(OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE.name(), view.getChangeScope());
        assertEquals("v1", view.getAlgorithmVersion());
        assertEquals("FEASIBLE", view.getSolverStatus());
        assertEquals(0, BigDecimal.valueOf(5.250000).compareTo(view.getObjectiveScore()));
        OptimizationRunItemView first = view.getItems().get(0);
        assertNotNull(first.getCandidateSkillFit());
        assertEquals(200L, first.getCandidateSkillFit().getSuggestedAssigneeId());
        assertEquals(List.of(501L), first.getCandidateSkillFit().getMatchedRequiredSkills());
        assertEquals(List.of(601L), first.getCandidateSkillFit().getMatchedPreferredSkills());
        assertNull(view.getItems().get(1).getCandidateSkillFit());
    }

    @Test
    void toViewShouldExposeScheduleAllocationChunks() {
        OptimizationRunItemEntity item = item(10L, 200L);
        item.setAllocationChunksJson("""
                [{"assigneeId":200,"start":1000,"end":2000,"effortMillis":1000},
                 {"assigneeId":200,"start":3000,"end":4000,"effortMillis":1000}]
                """);

        OptimizationRunReviewView view = assembler.toView(run(OptimizationRunSummary.builder().build()), List.of(item), List.of());

        assertEquals(2, view.getItems().get(0).getAllocationChunks().size());
        assertEquals(200L, view.getItems().get(0).getAllocationChunks().get(0).getAssigneeId());
        assertEquals(1000L, view.getItems().get(0).getAllocationChunks().get(0).getStart());
        assertEquals(4000L, view.getItems().get(0).getAllocationChunks().get(1).getEnd());
    }

    @Test
    void toViewShouldEnrichWorkItemAndAssigneeSummaries() {
        OptimizationRunItemEntity item = item(10L, 200L);
        item.setCurrentAssigneeId(100L);
        item.setOverrideAssigneeId(300L);

        OptimizationRunReviewView view = assembler.toView(
                run(OptimizationRunSummary.builder().build()),
                List.of(item),
                List.of(),
                Map.of(10L, new OptimizationWorkItemSummaryView(
                        10L,
                        "SERP-10",
                        "Implement scheduling optimization review",
                        "Task",
                        "In Progress",
                        "High"
                )),
                Map.of(
                        100L, new UserSummary(100L, "Current User", "/current.png"),
                        200L, new UserSummary(200L, "Suggested User", "/suggested.png"),
                        300L, new UserSummary(300L, "Override User", "/override.png")
                )
        );

        OptimizationRunItemView enrichedItem = view.getItems().get(0);
        assertEquals("SERP-10", enrichedItem.getWorkItem().key());
        assertEquals("Implement scheduling optimization review", enrichedItem.getWorkItem().summary());
        assertEquals("Current User", enrichedItem.getCurrentAssignee().displayName());
        assertEquals("Suggested User", enrichedItem.getSuggestedAssignee().displayName());
        assertEquals("Override User", enrichedItem.getOverrideAssignee().displayName());
    }

    private OptimizationRunEntity run(OptimizationRunSummary summary) {
        return OptimizationRunEntity.builder()
                .id(1L)
                .tenantId(1L)
                .projectId(100L)
                .scope("SELECTED_WORK_ITEMS")
                .objective(OptimizationObjective.BALANCED_WORKLOAD.name())
                .changeScope(OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE.name())
                .status(OptimizationRunStatus.GENERATED)
                .planningStart(1L)
                .planningEnd(2L)
                .selectedWorkItemCount(2)
                .summaryJson(jsonUtils.toJson(summary))
                .algorithmKey("greedy-balanced")
                .algorithmVersion("v1")
                .solverStatus("FEASIBLE")
                .objectiveScore(BigDecimal.valueOf(5.250000))
                .build();
    }

    private OptimizationRunItemEntity item(Long workItemId, Long suggestedAssigneeId) {
        return OptimizationRunItemEntity.builder()
                .id(workItemId)
                .workItemId(workItemId)
                .suggestedAssigneeId(suggestedAssigneeId)
                .assignmentDecision(OptimizationDecision.PENDING)
                .scheduleDecision(OptimizationDecision.PENDING)
                .assignmentApplyStatus(OptimizationApplyStatus.NOT_APPLIED)
                .scheduleApplyStatus(OptimizationApplyStatus.NOT_APPLIED)
                .score(BigDecimal.ONE)
                .cost(BigDecimal.ONE)
                .confidence(OptimizationConfidence.HIGH.name())
                .assignmentReasonsJson("[]")
                .scheduleReasonsJson("[]")
                .violationsJson("[]")
                .build();
    }
}
