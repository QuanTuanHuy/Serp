/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
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
import serp.project.pmcore.domain.optimization.service.assignment.scoring.OptimizationAssignmentScoringStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Greedy implementation of the {@link OptimizationAssignmentPolicy}.
 *
 * This class uses a greedy optimization approach to assign resources/assignees to work items.
 * It iterates through each work item and evaluates candidate assignees by computing
 * an effective assignment cost. The candidate with the lowest cost is chosen.
 *
 * The cost evaluation considers:
 * 1. Base costs of the resources.
 * 2. Reassignment penalties (to minimize disruption) or current assignee bonuses.
 * 3. Skill fit bonuses (for matching required or preferred skills) and proficiency levels.
 * 4. Capacity overload penalties (to balance work distribution across assignees).
 */
@Service
public class GreedyAssignmentPolicy implements OptimizationAssignmentPolicy {

    /**
     * Generates greedy assignment suggestions for all work items in the project model.
     *
     * @param projectModel {@inheritDoc}
     * @param options      {@inheritDoc}
     * @param warnings     {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Map<Long, OptimizationAssignmentSuggestion> generateAssignments(
            OptimizationProjectModel projectModel,
            OptimizationAlgorithmOptions options,
            List<OptimizationConstraintViolation> warnings) {
        // Output suggestions map (maintaining insertion order)
        Map<Long, OptimizationAssignmentSuggestion> suggestions = new LinkedHashMap<>();
        // Tracks the accumulated workload duration (in milliseconds) assigned to each resource
        Map<Long, Long> assignedLoadByAssignee = new HashMap<>();
        // Computes the total capacity limits of each candidate assignee based on slot sizes
        Map<Long, Long> capacityByAssignee = totalCapacityByAssignee(projectModel.capacitySlots());

        // Check if resource assignment optimization is enabled in options
        boolean assignmentEnabled = options.intent().changeScope().includesAssignment();

        // Process each work item in sequence
        for (OptimizationWorkItem item : projectModel.workItems()) {
            WorkItemEntity workItem = item.workItem();
            
            // Skip completed items, retaining their current database assignment state
            if (item.done()) {
                suggestions.put(workItem.getId(), keepCurrentAssignment(item, "Done item excluded from assignment optimization"));
                continue;
            }
            
            // If reassignment optimization is disabled, preserve current assignee but still track capacity load
            if (!assignmentEnabled) {
                Long assigneeId = workItem.getAssigneeId();
                if (assigneeId != null) {
                    assignedLoadByAssignee.merge(assigneeId, item.duration().durationMillis(), Long::sum);
                }
                suggestions.put(workItem.getId(), keepCurrentAssignment(item, "Reassignment is disabled"));
                continue;
            }

            // Choose the best candidate assignee greedily based on calculated costs
            OptimizationAssignmentSuggestion suggestion = chooseCandidate(item, options, assignedLoadByAssignee,
                    capacityByAssignee, warnings);
            
            // Accumulate workload for the suggested assignee to update capacity constraints for subsequent tasks
            if (suggestion.suggestedAssigneeId() != null) {
                assignedLoadByAssignee.merge(suggestion.suggestedAssigneeId(), item.duration().durationMillis(), Long::sum);
            }
            suggestions.put(workItem.getId(), suggestion);
        }
        return suggestions;
    }

    /**
     * Chooses the candidate assignee with the lowest effective assignment cost for a specific work item.
     *
     * @param item                   the work item to assign
     * @param options                algorithm options containing scoring configurations
     * @param assignedLoadByAssignee current workload tracking map
     * @param capacityByAssignee     pre-calculated total assignee capacities
     * @param warnings               list to collect constraint violations
     * @return the assignment suggestion containing the selected assignee, cost, reasons, and violations
     */
    private OptimizationAssignmentSuggestion chooseCandidate(OptimizationWorkItem item,
                                                             OptimizationAlgorithmOptions options,
                                                             Map<Long, Long> assignedLoadByAssignee,
                                                             Map<Long, Long> capacityByAssignee,
                                                             List<OptimizationConstraintViolation> warnings) {
        WorkItemEntity workItem = item.workItem();
        
        // Handle scenario where no eligible candidates exist for this work item
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

        // Calculate cost for each candidate and sort them to find the lowest cost
        // Break ties consistently using candidate ID and work item ID to ensure deterministic behavior
        List<CandidateCost> costs = item.candidateAssignees().stream()
                .map(candidate -> candidateCost(candidate, item, options, assignedLoadByAssignee, capacityByAssignee))
                .sorted(Comparator.comparingDouble(CandidateCost::effectiveCost)
                        .thenComparing(cost -> cost.candidate().candidateId())
                        .thenComparing(cost -> item.workItem().getId()))
                .toList();
        
        // Greedily choose the candidate with the lowest cost
        CandidateCost chosen = costs.get(0);
        
        // Check if capacity overload is unavoidable (every candidate is overloaded)
        boolean everyCandidateOverloaded = costs.stream().allMatch(CandidateCost::overloaded);
        List<OptimizationConstraintViolation> violations = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        reasons.add("Selected lowest effective assignment cost");
        
        // Compile selection reasons based on roles/context
        if (chosen.candidate().currentAssignee()) {
            reasons.add("Current assignee retained");
        }
        if (chosen.candidate().projectLead()) {
            reasons.add("Project lead is an eligible candidate");
        }
        if (chosen.candidate().reporter()) {
            reasons.add("Reporter is an eligible candidate");
        }
        
        // Analyze and append skill justification details
        addSkillReasons(workItem, chosen.candidate().skillFit(), reasons, warnings, violations);
        
        // If all candidate assignees are overloaded, register a capacity violation warning
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

    /**
     * Calculates the effective assignment cost for a candidate assignee.
     *
     * @param candidate              the candidate resource being evaluated
     * @param item                   the work item details
     * @param options                algorithm options containing scoring configurations
     * @param assignedLoadByAssignee current workload tracking map
     * @param capacityByAssignee     pre-calculated total assignee capacities
     * @return a CandidateCost containing the candidate details, calculated cost, and overload status
     */
    private CandidateCost candidateCost(OptimizationCandidateAssignee candidate,
                                        OptimizationWorkItem item,
                                        OptimizationAlgorithmOptions options,
                                        Map<Long, Long> assignedLoadByAssignee,
                                        Map<Long, Long> capacityByAssignee) {
        // Calculate projected load by adding this work item's duration to the assignee's currently scheduled load
        long projectedLoad = assignedLoadByAssignee.getOrDefault(candidate.candidateId(), 0L) + item.duration().durationMillis();
        long capacity = capacityByAssignee.getOrDefault(candidate.candidateId(), 0L);
        long overload = Math.max(0L, projectedLoad - capacity);
        
        // Start with candidate base cost
        double cost = candidate.baseCost();
        OptimizationAssignmentScoringStrategy scoringStrategy = options.assignmentScoringStrategy();
        
        // Add a penalty if the candidate is not the current assignee (to prevent excessive/unnecessary reshuffling)
        if (!Objects.equals(candidate.candidateId(), item.workItem().getAssigneeId())) {
            cost += scoringStrategy.reassignmentPenalty();
        }
        
        // Subtract a bonus if the candidate is already the current assignee
        if (candidate.currentAssignee()) {
            cost -= scoringStrategy.currentAssigneeBonus();
        }
        
        // Add capacity overload penalty if candidate exceeds their capacity limit
        if (overload > 0) {
            cost += OptimizationConstants.OVERLOAD_BASE_PENALTY
                    + ((double) overload / OptimizationConstants.HOUR_MILLIS);
        }
        
        // Add skill alignment cost details
        cost += skillCost(candidate.skillFit(), options);
        return new CandidateCost(candidate, cost, overload > 0);
    }

    /**
     * Evaluates skill matching and computes skill cost component for the resource.
     * Higher alignment (matched required/preferred skills, high proficiency) subtracts cost,
     * while missing skills add penalties.
     *
     * @param skillFit details of candidate skills alignment
     * @param options  algorithm options containing scoring configurations
     * @return the calculated skill cost component
     */
    private double skillCost(OptimizationCandidateSkillFit skillFit, OptimizationAlgorithmOptions options) {
        if (skillFit == null) {
            return 0D;
        }
        OptimizationAssignmentScoringStrategy scoringStrategy = options.assignmentScoringStrategy();
        double requiredMultiplier = scoringStrategy.requiredSkillMultiplier();
        double preferredMultiplier = scoringStrategy.preferredSkillMultiplier();
        double cost = 0D;
        
        // Grant bonuses for matched required/preferred skills
        cost -= skillFit.matchedRequiredSkillCount() * OptimizationConstants.REQUIRED_SKILL_MATCH_BONUS * requiredMultiplier;
        cost -= skillFit.matchedPreferredSkillCount() * OptimizationConstants.PREFERRED_SKILL_MATCH_BONUS * preferredMultiplier;
        
        // Grant bonus for proficiency score
        cost -= skillFit.proficiencyScore() * OptimizationConstants.PROFICIENCY_SCORE_BONUS_MULTIPLIER;
        
        // Add penalties for missing required/preferred skills
        cost += skillFit.missingRequiredSkillIds().size() * OptimizationConstants.MISSING_REQUIRED_SKILL_PENALTY;
        cost += skillFit.missingPreferredSkillIds().size() * OptimizationConstants.MISSING_PREFERRED_SKILL_PENALTY;
        
        // Add penalty for low confidence skill data
        if (skillFit.confidence() == OptimizationConfidence.LOW) {
            cost += OptimizationConstants.LOW_CONFIDENCE_SKILL_PENALTY;
        }
        return cost;
    }

    /**
     * Appends skill fit description reasons to the suggestion justification list,
     * and collects constraint violations if required skills are missing.
     *
     * @param workItem   the work item entity
     * @param skillFit   details of candidate skills alignment
     * @param reasons    list of justifications to append reasons to
     * @param warnings   list of warnings to collect constraint violations
     * @param violations list of violations for the current suggestion
     */
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
        
        // Generate constraint violation warning if required skills are missing
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

    /**
     * Helper method to preserve the work item's current assignee.
     *
     * @param item   the work item
     * @param reason the reason explaining why reassignment is skipped
     * @return the assignment suggestion containing the current assignee
     */
    private OptimizationAssignmentSuggestion keepCurrentAssignment(OptimizationWorkItem item, String reason) {
        Long assigneeId = item.workItem().getAssigneeId();
        return new OptimizationAssignmentSuggestion(item.workItem().getId(), assigneeId, 0D, List.of(reason), List.of());
    }

    /**
     * Aggregates total capacity limit in milliseconds for each assignee.
     *
     * @param slots list of capacity slots
     * @return a map of resource ID to total capacity milliseconds
     */
    private Map<Long, Long> totalCapacityByAssignee(List<ResourceCapacitySlot> slots) {
        return slots.stream().collect(Collectors.groupingBy(
                ResourceCapacitySlot::assigneeId,
                Collectors.summingLong(ResourceCapacitySlot::capacityMillis)
        ));
    }

    /**
     * Represents a candidate's evaluated assignment cost and whether it results in a capacity overload.
     */
    private record CandidateCost(OptimizationCandidateAssignee candidate, double effectiveCost, boolean overloaded) {
    }
}
