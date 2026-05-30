/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateAssignee;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateSkillFit;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GreedyAssignmentPolicy implements OptimizationAssignmentPolicy {
    @Override
    public Map<Long, OptimizationAssignmentSuggestion> generateAssignments(
            OptimizationProjectModel projectModel,
            OptimizationAlgorithmOptions options,
            List<OptimizationConstraintViolation> warnings) {
        Map<Long, OptimizationAssignmentSuggestion> suggestions = new LinkedHashMap<>();
        Map<Long, Long> assignedLoadByAssignee = new HashMap<>();
        Map<Long, Long> capacityByAssignee = totalCapacityByAssignee(projectModel.capacitySlots());

        boolean assignmentEnabled = options.intent().changeScope().includesAssignment();

        for (OptimizationWorkItem item : projectModel.workItems()) {
            WorkItemEntity workItem = item.workItem();
            if (item.done()) {
                suggestions.put(workItem.getId(), keepCurrentAssignment(item, "Done item excluded from assignment optimization"));
                continue;
            }
            if (!assignmentEnabled) {
                Long assigneeId = workItem.getAssigneeId();
                if (assigneeId != null) {
                    assignedLoadByAssignee.merge(assigneeId, item.duration().durationMillis(), Long::sum);
                }
                suggestions.put(workItem.getId(), keepCurrentAssignment(item, "Reassignment is disabled"));
                continue;
            }

            OptimizationAssignmentSuggestion suggestion = chooseCandidate(item, options, assignedLoadByAssignee,
                    capacityByAssignee, warnings);
            if (suggestion.suggestedAssigneeId() != null) {
                assignedLoadByAssignee.merge(suggestion.suggestedAssigneeId(), item.duration().durationMillis(), Long::sum);
            }
            suggestions.put(workItem.getId(), suggestion);
        }
        return suggestions;
    }

    private OptimizationAssignmentSuggestion chooseCandidate(OptimizationWorkItem item,
                                                             OptimizationAlgorithmOptions options,
                                                             Map<Long, Long> assignedLoadByAssignee,
                                                             Map<Long, Long> capacityByAssignee,
                                                             List<OptimizationConstraintViolation> warnings) {
        WorkItemEntity workItem = item.workItem();
        if (item.candidateAssignees().isEmpty()) {
            OptimizationConstraintViolation violation = new OptimizationConstraintViolation(
                    OptimizationWarningCode.NO_ELIGIBLE_ASSIGNEE,
                    workItem.getId(),
                    "No eligible assignee for assignment optimization",
                    null
            );
            warnings.add(violation);
            return new OptimizationAssignmentSuggestion(workItem.getId(), workItem.getAssigneeId(), 0D,
                    List.of("No eligible assignee; current assignment kept"), List.of(violation));
        }

        List<CandidateCost> costs = item.candidateAssignees().stream()
                .map(candidate -> candidateCost(candidate, item, options, assignedLoadByAssignee, capacityByAssignee))
                .sorted(Comparator.comparingDouble(CandidateCost::effectiveCost)
                        .thenComparing(cost -> cost.candidate().candidateId())
                        .thenComparing(cost -> item.workItem().getId()))
                .toList();
        CandidateCost chosen = costs.get(0);
        boolean everyCandidateOverloaded = costs.stream().allMatch(CandidateCost::overloaded);
        List<OptimizationConstraintViolation> violations = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        reasons.add("Selected lowest effective assignment cost");
        if (chosen.candidate().currentAssignee()) {
            reasons.add("Current assignee retained");
        }
        if (chosen.candidate().projectLead()) {
            reasons.add("Project lead is an eligible candidate");
        }
        if (chosen.candidate().reporter()) {
            reasons.add("Reporter is an eligible candidate");
        }
        addSkillReasons(workItem, chosen.candidate().skillFit(), reasons, warnings, violations);
        if (everyCandidateOverloaded) {
            OptimizationConstraintViolation violation = new OptimizationConstraintViolation(
                    OptimizationWarningCode.OVER_CAPACITY,
                    workItem.getId(),
                    "All eligible assignees exceed available planning capacity",
                    "assigneeId=" + chosen.candidate().candidateId()
            );
            warnings.add(violation);
            violations.add(violation);
            reasons.add("Overload unavoidable; selected least-bad candidate");
        }
        return new OptimizationAssignmentSuggestion(workItem.getId(), chosen.candidate().candidateId(),
                chosen.effectiveCost(), reasons, violations);
    }

    private CandidateCost candidateCost(OptimizationCandidateAssignee candidate,
                                        OptimizationWorkItem item,
                                        OptimizationAlgorithmOptions options,
                                        Map<Long, Long> assignedLoadByAssignee,
                                        Map<Long, Long> capacityByAssignee) {
        long projectedLoad = assignedLoadByAssignee.getOrDefault(candidate.candidateId(), 0L) + item.duration().durationMillis();
        long capacity = capacityByAssignee.getOrDefault(candidate.candidateId(), 0L);
        long overload = Math.max(0L, projectedLoad - capacity);
        double cost = candidate.baseCost();
        if (!Objects.equals(candidate.candidateId(), item.workItem().getAssigneeId())) {
            cost += options.intent().objective() == OptimizationObjective.MINIMAL_REASSIGNMENT
                    ? OptimizationConstants.MINIMAL_REASSIGNMENT_PENALTY
                    : OptimizationConstants.STANDARD_REASSIGNMENT_PENALTY;
        }
        if (candidate.currentAssignee() && options.intent().objective() == OptimizationObjective.MINIMAL_REASSIGNMENT) {
            cost -= OptimizationConstants.MINIMAL_REASSIGNMENT_CURRENT_ASSIGNEE_BONUS;
        }
        if (overload > 0) {
            cost += OptimizationConstants.OVERLOAD_BASE_PENALTY
                    + ((double) overload / OptimizationConstants.HOUR_MILLIS);
        }
        cost += skillCost(candidate.skillFit(), options);
        return new CandidateCost(candidate, cost, overload > 0);
    }

    private double skillCost(OptimizationCandidateSkillFit skillFit, OptimizationAlgorithmOptions options) {
        if (skillFit == null) {
            return 0D;
        }
        boolean skillFirst = options.intent().objective() == OptimizationObjective.SKILL_FIRST
                || OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST.equals(options.intent().algorithmKey());
        double requiredMultiplier = skillFirst ? 1.5D : 1D;
        double preferredMultiplier = skillFirst ? 1.25D : 1D;
        double cost = 0D;
        cost -= skillFit.matchedRequiredSkillCount() * OptimizationConstants.REQUIRED_SKILL_MATCH_BONUS * requiredMultiplier;
        cost -= skillFit.matchedPreferredSkillCount() * OptimizationConstants.PREFERRED_SKILL_MATCH_BONUS * preferredMultiplier;
        cost -= skillFit.proficiencyScore() * OptimizationConstants.PROFICIENCY_SCORE_BONUS_MULTIPLIER;
        cost += skillFit.missingRequiredSkillIds().size() * OptimizationConstants.MISSING_REQUIRED_SKILL_PENALTY;
        cost += skillFit.missingPreferredSkillIds().size() * OptimizationConstants.MISSING_PREFERRED_SKILL_PENALTY;
        if (skillFit.confidence() == OptimizationConfidence.LOW) {
            cost += OptimizationConstants.LOW_CONFIDENCE_SKILL_PENALTY;
        }
        return cost;
    }

    private void addSkillReasons(WorkItemEntity workItem,
                                 OptimizationCandidateSkillFit skillFit,
                                 List<String> reasons,
                                 List<OptimizationConstraintViolation> warnings,
                                 List<OptimizationConstraintViolation> violations) {
        if (skillFit == null || (skillFit.totalRequiredSkillCount() == 0 && skillFit.totalPreferredSkillCount() == 0)) {
            return;
        }
        if (skillFit.totalRequiredSkillCount() > 0) {
            reasons.add("Candidate matches " + skillFit.matchedRequiredSkillCount() + "/"
                    + skillFit.totalRequiredSkillCount() + " required skills");
        }
        if (skillFit.totalPreferredSkillCount() > 0) {
            reasons.add("Candidate matches " + skillFit.matchedPreferredSkillCount() + "/"
                    + skillFit.totalPreferredSkillCount() + " preferred skills");
        }
        if (!skillFit.missingRequiredSkillIds().isEmpty()) {
            OptimizationConstraintViolation violation = new OptimizationConstraintViolation(
                    OptimizationWarningCode.REQUIRED_SKILL_MISSING,
                    workItem.getId(),
                    "Selected assignee is missing required skills",
                    "skillIds=" + skillFit.missingRequiredSkillIds()
            );
            warnings.add(violation);
            violations.add(violation);
            reasons.add("Candidate missing required skills " + skillFit.missingRequiredSkillIds());
        }
        if (!skillFit.missingPreferredSkillIds().isEmpty()) {
            reasons.add("Candidate missing preferred skills " + skillFit.missingPreferredSkillIds());
        }
        if (skillFit.proficiencyScore() > 0D) {
            reasons.add("Candidate skill proficiency score " + skillFit.proficiencyScore());
        }
        if (skillFit.confidence() == OptimizationConfidence.LOW) {
            reasons.add("Skill data confidence is low");
        }
    }

    private OptimizationAssignmentSuggestion keepCurrentAssignment(OptimizationWorkItem item, String reason) {
        Long assigneeId = item.workItem().getAssigneeId();
        return new OptimizationAssignmentSuggestion(item.workItem().getId(), assigneeId, 0D, List.of(reason), List.of());
    }

    private Map<Long, Long> totalCapacityByAssignee(List<ResourceCapacitySlot> slots) {
        return slots.stream().collect(Collectors.groupingBy(
                ResourceCapacitySlot::assigneeId,
                Collectors.summingLong(ResourceCapacitySlot::capacityMillis)
        ));
    }

    private record CandidateCost(OptimizationCandidateAssignee candidate, double effectiveCost, boolean overloaded) {
    }
}
