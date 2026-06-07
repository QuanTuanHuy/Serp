/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.summary;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateAssignee;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateSkillFit;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationRunSummary;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OptimizationSummaryBuilder {
    public OptimizationRunSummary buildSummary(OptimizationProjectModel projectModel,
                                               Map<Long, OptimizationAssignmentSuggestion> assignments,
                                               Map<Long, OptimizationScheduleSuggestion> schedules,
                                               List<OptimizationConstraintViolation> warnings) {
        Set<Long> assignees = new HashSet<>();
        int assignmentSuggestionCount = 0;
        for (OptimizationWorkItem item : projectModel.workItems()) {
            Long currentAssignee = item.workItem().getAssigneeId();
            OptimizationAssignmentSuggestion suggestion = assignments.get(item.workItem().getId());
            Long suggestedAssignee = suggestion == null ? currentAssignee : suggestion.suggestedAssigneeId();
            if (suggestedAssignee != null) {
                assignees.add(suggestedAssignee);
            }
            if (!Objects.equals(currentAssignee, suggestedAssignee)) {
                assignmentSuggestionCount++;
            }
        }
        return OptimizationRunSummary.builder()
                .scopeSize(projectModel.workItems().size())
                .assigneeCount(assignees.size())
                .dependencyCount(projectModel.dependencyGraph().internalEdges().size())
                .planningStart(projectModel.planningStart())
                .planningEnd(projectModel.planningEnd())
                .assignmentSuggestionCount(assignmentSuggestionCount)
                .scheduledItemCount(schedules.size())
                .lateItemsBefore(lateItemsBefore(projectModel))
                .lateItemsAfter(lateItemsAfter(projectModel, schedules))
                .overloadedAssigneeCountBefore(0)
                .overloadedAssigneeCountAfter(overloadedAssigneeCount(projectModel, schedules))
                .warningsCount(warnings.size())
                .confidenceLevel(confidenceLevel(warnings))
                .capacitySourceMode(projectModel.capacityResolution().sourceMode().name())
                .calendarCoverageStatus(projectModel.capacityResolution().calendarCoverageStatus().name())
                .workloadCoverageStatus(projectModel.capacityResolution().workloadCoverageStatus().name())
                .fallbackUserIds(projectModel.capacityResolution().fallbackUserIds())
                .calendarFetchedAt(projectModel.capacityResolution().calendarFetchedAt())
                .workloadFetchedAt(projectModel.capacityResolution().workloadFetchedAt())
                .deductedWorkloadMillis(projectModel.capacityResolution().deductedWorkloadMillis())
                .sameProjectOutsideScopeDeductedMillis(projectModel.capacityResolution().sameProjectOutsideScopeDeductedMillis())
                .crossProjectDeductedMillis(projectModel.capacityResolution().crossProjectDeductedMillis())
                .workloadBuckets(projectModel.capacityResolution().workloadBuckets())
                .itemsWithSkillRequirements(itemsWithSkillRequirements(projectModel))
                .itemsMissingSkillRequirements(itemsMissingSkillRequirements(projectModel))
                .candidatesWithSkillProfiles(candidatesWithSkillProfiles(projectModel))
                .candidatesMissingSkillProfiles(candidatesMissingSkillProfiles(projectModel))
                .requiredSkillMismatchCount(requiredSkillMismatchCount(projectModel, assignments))
                .skillRankingConfidence(skillRankingConfidence(projectModel))
                .selectedCandidateSkillFits(selectedCandidateSkillFits(projectModel, assignments))
                .build();
    }

    private int itemsWithSkillRequirements(OptimizationProjectModel projectModel) {
        return (int) projectModel.workItems().stream()
                .filter(item -> item.candidateAssignees().stream()
                        .map(OptimizationCandidateAssignee::skillFit)
                        .filter(Objects::nonNull)
                        .anyMatch(fit -> fit.totalRequiredSkillCount() > 0 || fit.totalPreferredSkillCount() > 0))
                .count();
    }

    private int itemsMissingSkillRequirements(OptimizationProjectModel projectModel) {
        return projectModel.workItems().size() - itemsWithSkillRequirements(projectModel);
    }

    private int candidatesWithSkillProfiles(OptimizationProjectModel projectModel) {
        return (int) projectModel.workItems().stream()
                .flatMap(item -> item.candidateAssignees().stream())
                .map(OptimizationCandidateAssignee::skillFit)
                .filter(Objects::nonNull)
                .filter(fit -> fit.confidence() != OptimizationConfidence.LOW || !fit.matchedSkillIds().isEmpty())
                .count();
    }

    private int candidatesMissingSkillProfiles(OptimizationProjectModel projectModel) {
        return (int) projectModel.workItems().stream()
                .flatMap(item -> item.candidateAssignees().stream())
                .map(OptimizationCandidateAssignee::skillFit)
                .filter(Objects::nonNull)
                .filter(fit -> (fit.totalRequiredSkillCount() > 0 || fit.totalPreferredSkillCount() > 0)
                        && fit.confidence() == OptimizationConfidence.LOW
                        && fit.matchedSkillIds().isEmpty())
                .count();
    }

    private int requiredSkillMismatchCount(OptimizationProjectModel projectModel,
                                           Map<Long, OptimizationAssignmentSuggestion> assignments) {
        return (int) selectedCandidateSkillFits(projectModel, assignments).stream()
                .filter(fit -> !fit.missingRequiredSkillIds().isEmpty())
                .count();
    }

    private String skillRankingConfidence(OptimizationProjectModel projectModel) {
        if (candidatesMissingSkillProfiles(projectModel) > 0) {
            return OptimizationConfidence.LOW.name();
        }
        return itemsWithSkillRequirements(projectModel) == 0
                ? OptimizationConfidence.LOW.name()
                : OptimizationConfidence.HIGH.name();
    }

    private List<OptimizationCandidateSkillFit> selectedCandidateSkillFits(OptimizationProjectModel projectModel,
                                                                           Map<Long, OptimizationAssignmentSuggestion> assignments) {
        List<OptimizationCandidateSkillFit> fits = new ArrayList<>();
        for (OptimizationWorkItem item : projectModel.workItems()) {
            OptimizationAssignmentSuggestion assignment = assignments.get(item.workItem().getId());
            if (assignment == null || assignment.suggestedAssigneeId() == null) {
                continue;
            }
            item.candidateAssignees().stream()
                    .filter(candidate -> Objects.equals(candidate.candidateId(), assignment.suggestedAssigneeId()))
                    .map(OptimizationCandidateAssignee::skillFit)
                    .filter(Objects::nonNull)
                    .filter(fit -> fit.totalRequiredSkillCount() > 0 || fit.totalPreferredSkillCount() > 0)
                    .findFirst()
                    .ifPresent(fits::add);
        }
        return fits;
    }

    private int lateItemsBefore(OptimizationProjectModel projectModel) {
        int count = 0;
        for (OptimizationWorkItem item : projectModel.workItems()) {
            WorkItemPlanEntity plan = item.activePlan();
            Long dueDate = item.workItem().getDueDate();
            if (plan != null && dueDate != null && plan.getPlannedEnd() != null && plan.getPlannedEnd() > dueDate) {
                count++;
            }
        }
        return count;
    }

    private int lateItemsAfter(OptimizationProjectModel projectModel, Map<Long, OptimizationScheduleSuggestion> schedules) {
        int count = 0;
        for (OptimizationWorkItem item : projectModel.workItems()) {
            OptimizationScheduleSuggestion schedule = schedules.get(item.workItem().getId());
            Long dueDate = item.workItem().getDueDate();
            if (schedule != null && dueDate != null && schedule.plannedEnd() > dueDate) {
                count++;
            }
        }
        return count;
    }

    private int overloadedAssigneeCount(OptimizationProjectModel projectModel,
                                        Map<Long, OptimizationScheduleSuggestion> schedules) {
        Map<Long, Long> scheduledByAssignee = new HashMap<>();
        schedules.values().forEach(schedule -> scheduledByAssignee.merge(
                schedule.assigneeId(),
                schedule.allocatedEffortMillis() == null ? 0L : schedule.allocatedEffortMillis(),
                Long::sum
        ));
        Map<Long, Long> capacityByAssignee = totalCapacityByAssignee(projectModel.capacitySlots());
        return (int) scheduledByAssignee.entrySet().stream()
                .filter(entry -> entry.getValue() > capacityByAssignee.getOrDefault(entry.getKey(), 0L))
                .count();
    }

    private String confidenceLevel(List<OptimizationConstraintViolation> warnings) {
        boolean low = warnings.stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.DEPENDENCY_CYCLE
                || warning.code() == OptimizationWarningCode.OVER_CAPACITY
                || warning.code() == OptimizationWarningCode.NO_ELIGIBLE_ASSIGNEE);
        if (low) {
            return OptimizationConfidence.LOW.name();
        }
        return warnings.isEmpty() ? OptimizationConfidence.HIGH.name() : OptimizationConfidence.MEDIUM.name();
    }

    private Map<Long, Long> totalCapacityByAssignee(List<ResourceCapacitySlot> slots) {
        return slots.stream().collect(Collectors.groupingBy(
                ResourceCapacitySlot::assigneeId,
                Collectors.summingLong(ResourceCapacitySlot::capacityMillis)
        ));
    }
}
