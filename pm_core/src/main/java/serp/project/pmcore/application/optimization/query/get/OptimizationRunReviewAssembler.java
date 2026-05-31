/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateSkillFit;
import serp.project.pmcore.domain.optimization.model.OptimizationRunSummary;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleAllocation;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OptimizationRunReviewAssembler {
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };
    private static final TypeReference<List<OptimizationScheduleAllocation>> ALLOCATION_LIST = new TypeReference<>() {
    };

    private final JsonUtils jsonUtils;

    public OptimizationRunReviewView toView(OptimizationRunEntity run,
                                            List<OptimizationRunItemEntity> items,
                                            List<OptimizationRunWarningEntity> warnings) {
        OptimizationRunSummary summary = parseSummary(run.getSummaryJson());
        Map<Long, OptimizationCandidateSkillFit> skillFitByWorkItemId = selectedSkillFitByWorkItemId(summary);
        return OptimizationRunReviewView.builder()
                .id(run.getId())
                .tenantId(run.getTenantId())
                .projectId(run.getProjectId())
                .scope(run.getScope())
                .objective(run.getObjective())
                .changeScope(run.getChangeScope())
                .status(run.getStatus())
                .planningStart(run.getPlanningStart())
                .planningEnd(run.getPlanningEnd())
                .selectedWorkItemCount(run.getSelectedWorkItemCount())
                .summary(summary)
                .algorithmKey(run.getAlgorithmKey())
                .algorithmVersion(run.getAlgorithmVersion())
                .solverStatus(run.getSolverStatus())
                .objectiveScore(run.getObjectiveScore())
                .createdAt(run.getCreatedAt())
                .createdBy(run.getCreatedBy())
                .updatedAt(run.getUpdatedAt())
                .updatedBy(run.getUpdatedBy())
                .items(items.stream()
                        .sorted(Comparator.comparing(OptimizationRunItemEntity::getWorkItemId))
                        .map(item -> toItemView(item, skillFitByWorkItemId.get(item.getWorkItemId())))
                        .toList())
                .warnings(warnings.stream()
                        .sorted(Comparator.comparing(OptimizationRunWarningEntity::getId, Comparator.nullsLast(Long::compareTo)))
                        .map(this::toWarningView)
                        .toList())
                .build();
    }

    private OptimizationRunItemView toItemView(OptimizationRunItemEntity item, OptimizationCandidateSkillFit skillFit) {
        return OptimizationRunItemView.builder()
                .id(item.getId())
                .workItemId(item.getWorkItemId())
                .workItemUpdatedAtSnapshot(item.getWorkItemUpdatedAtSnapshot())
                .planUpdatedAtSnapshot(item.getPlanUpdatedAtSnapshot())
                .currentAssigneeId(item.getCurrentAssigneeId())
                .suggestedAssigneeId(item.getSuggestedAssigneeId())
                .overrideAssigneeId(item.getOverrideAssigneeId())
                .currentPlannedStart(item.getCurrentPlannedStart())
                .currentPlannedEnd(item.getCurrentPlannedEnd())
                .suggestedPlannedStart(item.getSuggestedPlannedStart())
                .suggestedPlannedEnd(item.getSuggestedPlannedEnd())
                .overridePlannedStart(item.getOverridePlannedStart())
                .overridePlannedEnd(item.getOverridePlannedEnd())
                .currentDueDate(item.getCurrentDueDate())
                .assignmentDecision(item.getAssignmentDecision())
                .scheduleDecision(item.getScheduleDecision())
                .assignmentApplyStatus(item.getAssignmentApplyStatus())
                .scheduleApplyStatus(item.getScheduleApplyStatus())
                .score(item.getScore())
                .cost(item.getCost())
                .confidence(item.getConfidence())
                .candidateSkillFit(toSkillFitView(skillFit))
                .assignmentReasons(parseStringList(item.getAssignmentReasonsJson()))
                .scheduleReasons(parseStringList(item.getScheduleReasonsJson()))
                .violations(parseStringList(item.getViolationsJson()))
                .allocationChunks(parseAllocationChunks(item.getAllocationChunksJson()))
                .appliedAt(item.getAppliedAt())
                .assignmentSkippedReason(item.getAssignmentSkippedReason())
                .scheduleSkippedReason(item.getScheduleSkippedReason())
                .build();
    }

    private Map<Long, OptimizationCandidateSkillFit> selectedSkillFitByWorkItemId(OptimizationRunSummary summary) {
        if (summary == null || summary.getSelectedCandidateSkillFits() == null) {
            return Map.of();
        }
        return summary.getSelectedCandidateSkillFits().stream()
                .collect(Collectors.toMap(OptimizationCandidateSkillFit::workItemId, Function.identity(), (left, right) -> left));
    }

    private OptimizationCandidateSkillFitView toSkillFitView(OptimizationCandidateSkillFit skillFit) {
        if (skillFit == null) {
            return null;
        }
        return OptimizationCandidateSkillFitView.builder()
                .suggestedAssigneeId(skillFit.candidateId())
                .requiredCoveragePercent(skillFit.requiredCoveragePercent())
                .preferredCoveragePercent(skillFit.preferredCoveragePercent())
                .matchedRequiredSkills(skillFit.matchedRequiredSkillIds())
                .missingRequiredSkills(skillFit.missingRequiredSkillIds())
                .matchedPreferredSkills(skillFit.matchedPreferredSkillIds())
                .missingPreferredSkills(skillFit.missingPreferredSkillIds())
                .proficiencySummary("score=" + skillFit.proficiencyScore())
                .confidence(skillFit.confidence().name())
                .build();
    }

    private OptimizationRunWarningView toWarningView(OptimizationRunWarningEntity warning) {
        return OptimizationRunWarningView.builder()
                .id(warning.getId())
                .workItemId(warning.getWorkItemId())
                .severity(warning.getSeverity())
                .code(warning.getCode())
                .message(warning.getMessage())
                .detailsJson(warning.getDetailsJson())
                .build();
    }

    private OptimizationRunSummary parseSummary(String summaryJson) {
        if (summaryJson == null || summaryJson.isBlank()) {
            return null;
        }
        return jsonUtils.fromJson(summaryJson, OptimizationRunSummary.class);
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return jsonUtils.fromJson(json, STRING_LIST);
    }

    private List<OptimizationScheduleAllocationView> parseAllocationChunks(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return jsonUtils.fromJson(json, ALLOCATION_LIST).stream()
                .map(allocation -> OptimizationScheduleAllocationView.builder()
                        .assigneeId(allocation.assigneeId())
                        .start(allocation.start())
                        .end(allocation.end())
                        .effortMillis(allocation.effortMillis())
                        .build())
                .toList();
    }
}
