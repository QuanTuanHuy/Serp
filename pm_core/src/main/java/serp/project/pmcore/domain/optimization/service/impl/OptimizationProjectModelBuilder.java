/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.domain.issuelink.enums.IssueLinkDependencyBehavior;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkPort;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkTypePort;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateAssignee;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateSkill;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateSkillFit;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyEdge;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyGraph;
import serp.project.pmcore.domain.optimization.model.OptimizationDuration;
import serp.project.pmcore.domain.optimization.model.OptimizationPriorityScore;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationSkillRequirement;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.model.WorkItemComponentLink;
import serp.project.pmcore.domain.optimization.port.IResourceCapacityPort;
import serp.project.pmcore.domain.optimization.port.IWorkItemComponentReadPort;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.optimization.service.IOptimizationProjectModelBuilder;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.IProjectComponentPort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.service.IProjectMemberService;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;
import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillRequirementType;
import serp.project.pmcore.domain.skill.port.read.IUserSkillReadPort;
import serp.project.pmcore.domain.skill.port.read.IWorkItemSkillReadPort;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimizationProjectModelBuilder implements IOptimizationProjectModelBuilder {
    private final IProjectReadPort projectReadPort;
    private final IWorkItemReadPort workItemReadPort;
    private final IWorkItemPlanPort workItemPlanPort;
    private final IIssueLinkPort issueLinkPort;
    private final IIssueLinkTypePort issueLinkTypePort;
    private final IProjectComponentPort projectComponentPort;
    private final IWorkItemComponentReadPort workItemComponentReadPort;
    private final IProjectMemberService projectMemberService;
    private final IResourceCapacityPort resourceCapacityPort;
    private final IWorkItemSkillReadPort workItemSkillReadPort;
    private final IUserSkillReadPort userSkillReadPort;

    @Override
    public OptimizationProjectModel build(OptimizationBuilderInput input) {
        log.info("Building optimization project model: tenantId={}, projectId={}, selectedCount={}, objective={}, changeScope={}",
                input.tenantId(), input.projectId(), input.selectedWorkItemIds().size(),
                input.intent().objective(), input.intent().changeScope());
        // Load project and validate existence
        ProjectEntity project = projectReadPort.getProjectById(input.projectId(), input.tenantId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + input.projectId()));
        // Load selected work items and their active plans
        List<WorkItemEntity> selected = loadSelectedWorkItems(input);
        Map<Long, WorkItemPlanEntity> activePlans = workItemPlanPort
                .listActivePlansByWorkItemIds(input.tenantId(), idsOf(selected))
                .stream()
                .collect(Collectors.toMap(WorkItemPlanEntity::getWorkItemId, plan -> plan, (left, right) -> left));
        // Build dependency graph and detect cycles
        List<OptimizationConstraintViolation> warnings = new ArrayList<>();
        OptimizationDependencyGraph graph = buildDependencyGraph(input.tenantId(), selected, warnings);
        // Resolve durations, critical path, priority scores, and assignee candidates
        Map<Long, OptimizationDuration> durations = resolveDurations(selected, warnings);
        Set<Long> criticalPathIds = resolveCriticalPath(graph, durations);
        Map<Long, OptimizationPriorityScore> scores = resolvePriorityScores(selected, graph, durations, warnings);
        Map<Long, List<OptimizationCandidateAssignee>> candidates = attachSkillFit(input.tenantId(), selected,
                resolveCandidates(input.tenantId(), selected, project, warnings), warnings);
        // Assemble work items sorted by priority
        List<OptimizationWorkItem> workItems = selected.stream()
                .map(item -> new OptimizationWorkItem(
                        item,
                        activePlans.get(item.getId()),
                        durations.get(item.getId()),
                        scores.get(item.getId()),
                        candidates.getOrDefault(item.getId(), List.of()),
                        isDone(item),
                        criticalPathIds.contains(item.getId())))
                .sorted(workItemComparator())
                .toList();
        // Build daily capacity slots for all unique candidate assignees
        List<Long> candidateIds = candidates.values().stream()
                .flatMap(Collection::stream)
                .map(OptimizationCandidateAssignee::candidateId)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
        var capacityResolution = resourceCapacityPort.resolveCapacity(input.tenantId(), input.projectId(), candidateIds,
                input.planningStart(), input.planningEnd(), input.selectedWorkItemIds());
        List<ResourceCapacitySlot> capacitySlots = capacityResolution.slots();
        warnings.addAll(capacityResolution.warnings());
        if (!candidateIds.isEmpty()) {
            warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.LOW_CONFIDENCE_CAPACITY,
                    null, "Fallback capacity used", OptimizationConstants.FALLBACK_CAPACITY_DETAILS));
            warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.MISSING_CALENDAR,
                    null, "Calendar data is unavailable", null));
        }
        // Resolve earliest start times based on external dependency planned ends
        Map<Long, Long> earliestStarts = resolveExternalEarliestStarts(input.tenantId(), graph.externalEdges(), warnings);
        log.info("Built optimization project model: tenantId={}, projectId={}, items={}, internalDependencies={}, externalDependencies={}, warnings={}",
                input.tenantId(), input.projectId(), workItems.size(), graph.internalEdges().size(), graph.externalEdges().size(), warnings.size());
        return new OptimizationProjectModel(input.tenantId(), input.projectId(), project, input.planningStart(), input.planningEnd(),
                graph, workItems, capacitySlots, capacityResolution, warnings, earliestStarts);
    }

    private List<WorkItemEntity> loadSelectedWorkItems(OptimizationBuilderInput input) {
        List<WorkItemEntity> items = workItemReadPort.listActiveByWorkItemIds(input.tenantId(), input.selectedWorkItemIds())
                .stream()
                .filter(item -> Objects.equals(item.getProjectId(), input.projectId()))
                .toList();
        log.debug("Loaded selected work items: tenantId={}, projectId={}, requested={}, loaded={}",
                input.tenantId(), input.projectId(), input.selectedWorkItemIds().size(), items.size());
        return items;
    }

    private OptimizationDependencyGraph buildDependencyGraph(Long tenantId,
                                                            List<WorkItemEntity> selected,
                                                            List<OptimizationConstraintViolation> warnings) {
        Set<Long> selectedIds = new LinkedHashSet<>(idsOf(selected));
        // Load all link types to determine dependency behavior (blocks, blocked-by, etc.)
        Map<Long, IssueLinkTypeEntity> types = issueLinkTypePort.listByTenantIncludingSystem(tenantId).stream()
                .collect(Collectors.toMap(IssueLinkTypeEntity::getId, type -> type, (left, right) -> left));
        log.info("Loaded issue link types: tenantId={}, count={}", tenantId, types.size());
        // Collect all issue links for selected work items, deduplicated by link ID
        Map<Long, IssueLinkDetailEntity> linksById = new LinkedHashMap<>();
        for (Long workItemId : selectedIds) {
            for (IssueLinkDetailEntity link : issueLinkPort.listByWorkItemId(tenantId, workItemId)) {
                linksById.putIfAbsent(link.getLinkId(), link);
            }
        }
        // Classify edges as internal (both endpoints selected) or external (one endpoint outside selection)
        List<OptimizationDependencyEdge> internalEdges = new ArrayList<>();
        List<OptimizationDependencyEdge> externalEdges = new ArrayList<>();
        for (IssueLinkDetailEntity link : linksById.values()) {
            IssueLinkDependencyBehavior behavior = Optional.ofNullable(types.get(link.getLinkTypeId()))
                    .map(IssueLinkTypeEntity::getDependencyBehavior)
                    .orElse(IssueLinkDependencyBehavior.NONE);
            if (behavior == IssueLinkDependencyBehavior.NONE) {
                continue;
            }
            // Determine predecessor and successor based on dependency direction
            Long predecessor = behavior == IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET ? link.getSourceId() : link.getTargetId();
            Long successor = behavior == IssueLinkDependencyBehavior.SOURCE_BLOCKS_TARGET ? link.getTargetId() : link.getSourceId();
            boolean external = !selectedIds.contains(predecessor) || !selectedIds.contains(successor);
            OptimizationDependencyEdge edge = new OptimizationDependencyEdge(predecessor, successor, link.getLinkId(), link.getLinkTypeId(), external);
            if (external) {
                externalEdges.add(edge);
                // Warn about external dependencies that may affect scheduling
                warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.EXTERNAL_DEPENDENCY,
                        selectedIds.contains(successor) ? successor : predecessor,
                        "External dependency exists", predecessor + " -> " + successor));
            } else {
                internalEdges.add(edge);
            }
        }
        // Build graph adjacency maps and detect cycles using DFS
        GraphState state = buildGraphState(selectedIds, internalEdges);
        List<List<Long>> cycles = findCycles(selectedIds, state.successorsById);
        for (List<Long> cycle : cycles) {
            warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.DEPENDENCY_CYCLE,
                    cycle.isEmpty() ? null : cycle.get(0), "Dependency cycle detected", cycle.toString()));
        }
        log.debug("Built dependency graph: selected={}, internalEdges={}, externalEdges={}, cycles={}",
                selectedIds.size(), internalEdges.size(), externalEdges.size(), cycles.size());
        return new OptimizationDependencyGraph(internalEdges, externalEdges, cycles,
                state.predecessorsById, state.successorsById, topologicalOrder(selectedIds, state.predecessorsById, state.successorsById));
    }

    private Map<Long, OptimizationDuration> resolveDurations(List<WorkItemEntity> items,
                                                             List<OptimizationConstraintViolation> warnings) {
        Map<Long, OptimizationDuration> result = new HashMap<>();
        for (WorkItemEntity item : items) {
            // Prefer remaining estimate (highest confidence), fall back to original estimate, then default
            if (positive(item.getTimeRemainingEstimate())) {
                result.put(item.getId(), new OptimizationDuration(item.getId(), estimateMinutesToMillis(item.getTimeRemainingEstimate()),
                        OptimizationConfidence.HIGH, OptimizationConstants.TIME_REMAINING_ESTIMATE));
            } else if (positive(item.getTimeOriginalEstimate())) {
                result.put(item.getId(), new OptimizationDuration(item.getId(), estimateMinutesToMillis(item.getTimeOriginalEstimate()),
                        OptimizationConfidence.MEDIUM, OptimizationConstants.TIME_ORIGINAL_ESTIMATE));
            } else {
                // Use hierarchy-level-based default and warn about low confidence
                long fallback = defaultDuration(item.getIssueTypeHierarchyLevel());
                result.put(item.getId(), new OptimizationDuration(
                        item.getId(),
                        fallback,
                        OptimizationConfidence.LOW,
                        OptimizationConstants.DURATION_SOURCE_DEFAULT
                ));
                warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.MISSING_ESTIMATE, item.getId(), "Work item missing estimate", null));
                warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.DEFAULT_DURATION_USED, item.getId(), "Default duration used", String.valueOf(fallback)));
                warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.LOW_CONFIDENCE_DURATION, item.getId(), "Duration confidence is low", null));
            }
        }
        return result;
    }

    private long estimateMinutesToMillis(Long estimateMinutes) {
        return Math.multiplyExact(estimateMinutes, OptimizationConstants.MINUTE_MILLIS);
    }

    private Set<Long> resolveCriticalPath(OptimizationDependencyGraph graph, Map<Long, OptimizationDuration> durations) {
        // Cannot compute critical path when cycles exist - return empty set
        if (graph.hasCycles()) {
            return Set.of();
        }
        // Use longest path algorithm on DAG (topological order) to find critical path
        Map<Long, Long> longest = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();
        for (Long id : graph.topologicalOrder()) {
            longest.putIfAbsent(id, durations.get(id).durationMillis());
            for (Long successor : graph.successorsByWorkItemId().getOrDefault(id, Set.of())) {
                long candidate = longest.get(id) + durations.get(successor).durationMillis();
                if (candidate > longest.getOrDefault(successor, 0L)) {
                    longest.put(successor, candidate);
                    previous.put(successor, id);
                }
            }
        }
        // Trace back from the node with the longest cumulative duration
        Long end = longest.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        Set<Long> critical = new HashSet<>();
        while (end != null) {
            critical.add(end);
            end = previous.get(end);
        }
        return critical;
    }

    private Map<Long, OptimizationPriorityScore> resolvePriorityScores(List<WorkItemEntity> items,
                                                                       OptimizationDependencyGraph graph,
                                                                       Map<Long, OptimizationDuration> durations,
                                                                       List<OptimizationConstraintViolation> warnings) {
        // Count outgoing edges to identify blockers (items that block many others get higher priority)
        Map<Long, Integer> outgoing = graph.internalEdges().stream()
                .collect(Collectors.groupingBy(OptimizationDependencyEdge::predecessorId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        Map<Long, OptimizationPriorityScore> result = new HashMap<>();
        for (WorkItemEntity item : items) {
            // Priority factor: inverse of sequence (lower sequence = higher priority), default 0.5 if missing
            boolean neutralPriority = item.getPrioritySequence() == null;
            double priorityFactor = neutralPriority
                    ? OptimizationConstants.PRIORITY_NEUTRAL_FACTOR
                    : 1D / Math.max(1, item.getPrioritySequence());
            // Due date factor: closer deadlines increase score, normalized against 14-day window
            double dueFactor = item.getDueDate() == null
                    ? 0D
                    : Math.max(0D, 1D - ((double) (item.getDueDate() - System.currentTimeMillis())
                    / (OptimizationConstants.DUE_DATE_WINDOW_DAYS * OptimizationConstants.DAY_MILLIS)));
            // Blocker factor: each blocked successor adds 0.25 to the score
            double blockerFactor = outgoing.getOrDefault(item.getId(), 0)
                    * OptimizationConstants.BLOCKER_FACTOR_PER_SUCCESSOR;
            // Penalty for items using default duration estimates (low confidence)
            double estimatePenalty = OptimizationConstants.DURATION_SOURCE_DEFAULT.equals(durations.get(item.getId()).source())
                    ? OptimizationConstants.DEFAULT_ESTIMATE_PENALTY
                    : 0D;
            double score = priorityFactor + dueFactor + blockerFactor + estimatePenalty;
            if (neutralPriority) {
                warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.NEUTRAL_PRIORITY_USED, item.getId(), "Priority sequence missing", null));
            }
            result.put(item.getId(), new OptimizationPriorityScore(item.getId(), score, neutralPriority));
        }
        return result;
    }

    private Map<Long, List<OptimizationCandidateAssignee>> resolveCandidates(Long tenantId,
                                                                             List<WorkItemEntity> items,
                                                                             ProjectEntity project,
                                                                             List<OptimizationConstraintViolation> warnings) {
        Map<Long, List<OptimizationCandidateAssignee>> result = new HashMap<>();
        Map<Long, List<Long>> componentLeadsByWorkItemId = resolveComponentLeads(tenantId, project.getId(), items);
        List<Long> assignableProjectMembers = projectMemberService.listAssignableMembers(project);
        if (assignableProjectMembers.isEmpty()) {
            warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.NO_PROJECT_MEMBER_POOL,
                    null, "No assignable project member pool", null));
        }
        for (WorkItemEntity item : items) {
            // Collect potential assignees: high-signal fields, component leads, assignable project members.
            Map<Long, CandidateFlags> flagsById = new LinkedHashMap<>();
            addCandidate(flagsById, item.getAssigneeId(), flags -> flags.currentAssignee = true);
            for (Long componentLeadId : componentLeadsByWorkItemId.getOrDefault(item.getId(), List.of())) {
                addCandidate(flagsById, componentLeadId, flags -> flags.componentLead = true);
            }
            addCandidate(flagsById, project.getLeadUserId(), flags -> flags.projectLead = true);
            addCandidate(flagsById, item.getReporterId(), flags -> flags.reporter = true);
            for (Long memberId : assignableProjectMembers) {
                addCandidate(flagsById, memberId, flags -> flags.projectMember = true);
            }
            // Sort by base cost (lower is preferred) then by candidate ID for determinism
            List<OptimizationCandidateAssignee> candidates = flagsById.entrySet().stream()
                    .map(entry -> new OptimizationCandidateAssignee(item.getId(), entry.getKey(), baseCost(entry.getValue()),
                            entry.getValue().currentAssignee, entry.getValue().componentLead, entry.getValue().projectLead,
                            entry.getValue().reporter, entry.getValue().projectMember, null))
                    .sorted(Comparator.comparingDouble(OptimizationCandidateAssignee::baseCost)
                            .thenComparing(OptimizationCandidateAssignee::candidateId))
                    .toList();
            if (candidates.isEmpty()) {
                warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.NO_ELIGIBLE_ASSIGNEE, item.getId(), "No eligible assignee", null));
            }
            result.put(item.getId(), candidates);
        }
        return result;
    }

    private Map<Long, List<OptimizationCandidateAssignee>> attachSkillFit(Long tenantId,
                                                                          List<WorkItemEntity> items,
                                                                          Map<Long, List<OptimizationCandidateAssignee>> candidates,
                                                                          List<OptimizationConstraintViolation> warnings) {
        List<Long> workItemIds = idsOf(items);
        Map<Long, List<OptimizationSkillRequirement>> requirementsByWorkItemId = workItemSkillReadPort
                .listActiveByWorkItemIds(tenantId, workItemIds)
                .stream()
                .map(this::toSkillRequirement)
                .collect(Collectors.groupingBy(OptimizationSkillRequirement::workItemId));
        Set<Long> candidateIds = candidates.values().stream()
                .flatMap(Collection::stream)
                .map(OptimizationCandidateAssignee::candidateId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, Map<Long, OptimizationCandidateSkill>> skillsByCandidateId = userSkillReadPort
                .listActiveByUserIds(tenantId, candidateIds.stream().toList())
                .stream()
                .map(this::toCandidateSkill)
                .collect(Collectors.groupingBy(OptimizationCandidateSkill::candidateId,
                        Collectors.toMap(OptimizationCandidateSkill::skillId, skill -> skill, (left, right) -> left)));

        Map<Long, List<OptimizationCandidateAssignee>> result = new HashMap<>();
        for (WorkItemEntity item : items) {
            List<OptimizationSkillRequirement> requirements = requirementsByWorkItemId.getOrDefault(item.getId(), List.of())
                    .stream()
                    .sorted(Comparator.comparing(OptimizationSkillRequirement::skillId))
                    .toList();
            if (requirements.isEmpty()) {
                warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.WORK_ITEM_SKILL_DATA_MISSING,
                        item.getId(), "Work item skill requirements are missing", null));
            }
            List<OptimizationCandidateAssignee> enriched = candidates.getOrDefault(item.getId(), List.of()).stream()
                    .map(candidate -> withSkillFit(candidate, requirements, skillsByCandidateId, warnings))
                    .toList();
            result.put(item.getId(), enriched);
        }
        return result;
    }

    private OptimizationCandidateAssignee withSkillFit(OptimizationCandidateAssignee candidate,
                                                       List<OptimizationSkillRequirement> requirements,
                                                       Map<Long, Map<Long, OptimizationCandidateSkill>> skillsByCandidateId,
                                                       List<OptimizationConstraintViolation> warnings) {
        Map<Long, OptimizationCandidateSkill> candidateSkills = skillsByCandidateId.getOrDefault(candidate.candidateId(), Map.of());
        if (!requirements.isEmpty() && candidateSkills.isEmpty()) {
            warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.CANDIDATE_SKILL_DATA_MISSING,
                    candidate.workItemId(), "Candidate skill profile is missing", String.valueOf(candidate.candidateId())));
        }
        OptimizationCandidateSkillFit fit = buildSkillFit(candidate.workItemId(), candidate.candidateId(), requirements, candidateSkills);
        if (!fit.missingRequiredSkillIds().isEmpty()) {
            warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.REQUIRED_SKILL_MISSING,
                    candidate.workItemId(), "Candidate missing required skill", fit.missingRequiredSkillIds().toString()));
        }
        if ((!fit.missingRequiredSkillIds().isEmpty() && fit.matchedRequiredSkillCount() > 0)
                || (!fit.missingPreferredSkillIds().isEmpty() && fit.matchedPreferredSkillCount() > 0)) {
            warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.PARTIAL_SKILL_MATCH,
                    candidate.workItemId(), "Candidate has partial skill match", String.valueOf(candidate.candidateId())));
        }
        if (fit.confidence() == OptimizationConfidence.LOW) {
            warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.LOW_CONFIDENCE_SKILL_DATA,
                    candidate.workItemId(), "Skill fit confidence is low", String.valueOf(candidate.candidateId())));
        }
        return new OptimizationCandidateAssignee(candidate.workItemId(), candidate.candidateId(), candidate.baseCost(),
                candidate.currentAssignee(), candidate.componentLead(), candidate.projectLead(), candidate.reporter(),
                candidate.projectMember(), fit);
    }

    private OptimizationCandidateSkillFit buildSkillFit(Long workItemId,
                                                        Long candidateId,
                                                        List<OptimizationSkillRequirement> requirements,
                                                        Map<Long, OptimizationCandidateSkill> candidateSkills) {
        if (requirements.isEmpty()) {
            return OptimizationCandidateSkillFit.neutral(workItemId, candidateId);
        }
        List<Long> matchedSkillIds = new ArrayList<>();
        List<Long> matchedRequiredSkillIds = new ArrayList<>();
        List<Long> matchedPreferredSkillIds = new ArrayList<>();
        List<Long> missingRequiredSkillIds = new ArrayList<>();
        List<Long> missingPreferredSkillIds = new ArrayList<>();
        int totalRequired = 0;
        int totalPreferred = 0;
        int matchedRequired = 0;
        int matchedPreferred = 0;
        double proficiencyScore = 0D;
        for (OptimizationSkillRequirement requirement : requirements) {
            boolean required = requirement.requirementType() == SkillRequirementType.REQUIRED;
            if (required) {
                totalRequired++;
            } else {
                totalPreferred++;
            }
            OptimizationCandidateSkill skill = candidateSkills.get(requirement.skillId());
            boolean matched = skill != null && meetsProficiency(skill.proficiency(), requirement.minProficiency());
            if (matched) {
                matchedSkillIds.add(requirement.skillId());
                proficiencyScore += proficiencyScore(skill.proficiency()) * Math.max(1, Optional.ofNullable(requirement.weight()).orElse(1));
                if (required) {
                    matchedRequired++;
                    matchedRequiredSkillIds.add(requirement.skillId());
                } else {
                    matchedPreferred++;
                    matchedPreferredSkillIds.add(requirement.skillId());
                }
            } else if (required) {
                missingRequiredSkillIds.add(requirement.skillId());
            } else {
                missingPreferredSkillIds.add(requirement.skillId());
            }
        }
        OptimizationConfidence confidence = candidateSkills.isEmpty() ? OptimizationConfidence.LOW
                : (missingRequiredSkillIds.isEmpty() && missingPreferredSkillIds.isEmpty() ? OptimizationConfidence.HIGH : OptimizationConfidence.MEDIUM);
        return new OptimizationCandidateSkillFit(workItemId, candidateId, matchedRequired, totalRequired,
                matchedPreferred, totalPreferred, coverage(matchedRequired, totalRequired), coverage(matchedPreferred, totalPreferred),
                proficiencyScore, missingRequiredSkillIds.stream().sorted().toList(), missingPreferredSkillIds.stream().sorted().toList(),
                matchedSkillIds.stream().sorted().toList(), matchedRequiredSkillIds.stream().sorted().toList(),
                matchedPreferredSkillIds.stream().sorted().toList(), confidence);
    }

    private OptimizationSkillRequirement toSkillRequirement(WorkItemSkillEntity entity) {
        return new OptimizationSkillRequirement(entity.getWorkItemId(), entity.getSkillId(), entity.getRequirementType(),
                entity.getMinProficiency(), entity.getWeight());
    }

    private OptimizationCandidateSkill toCandidateSkill(UserSkillEntity entity) {
        return new OptimizationCandidateSkill(entity.getUserId(), entity.getSkillId(), entity.getProficiency(), entity.getConfidence());
    }

    private boolean meetsProficiency(SkillProficiency actual, SkillProficiency required) {
        return actual != null && required != null && actual.ordinal() >= required.ordinal();
    }

    private double proficiencyScore(SkillProficiency proficiency) {
        return proficiency == null ? 0D : proficiency.ordinal() + 1D;
    }

    private double coverage(int matched, int total) {
        return total == 0 ? 100D : (matched * 100D) / total;
    }

    private Map<Long, List<Long>> resolveComponentLeads(Long tenantId, Long projectId, List<WorkItemEntity> items) {
        List<WorkItemComponentLink> links = workItemComponentReadPort.listActiveByWorkItemIds(tenantId, idsOf(items));
        if (links.isEmpty()) {
            return Map.of();
        }
        Map<Long, ProjectComponentEntity> componentsById = projectComponentPort
                .getComponentsByIds(links.stream().map(WorkItemComponentLink::componentId).distinct().toList(), projectId, tenantId)
                .stream()
                .collect(Collectors.toMap(ProjectComponentEntity::getId, component -> component, (left, right) -> left));
        Map<Long, List<Long>> result = new HashMap<>();
        for (WorkItemComponentLink link : links) {
            ProjectComponentEntity component = componentsById.get(link.componentId());
            if (component != null && component.getLeadUserId() != null) {
                result.computeIfAbsent(link.workItemId(), id -> new ArrayList<>()).add(component.getLeadUserId());
            }
        }
        result.replaceAll((id, leads) -> leads.stream().distinct().sorted().toList());
        return result;
    }

    private Map<Long, Long> resolveExternalEarliestStarts(Long tenantId,
                                                          List<OptimizationDependencyEdge> externalEdges,
                                                          List<OptimizationConstraintViolation> warnings) {
        // Collect unique external predecessor IDs to look up their planned end dates
        List<Long> externalPredecessors = externalEdges.stream()
                .filter(edge -> !edge.external() || !Objects.equals(edge.predecessorId(), edge.successorId()))
                .map(OptimizationDependencyEdge::predecessorId)
                .distinct()
                .toList();
        Map<Long, WorkItemPlanEntity> plans = workItemPlanPort.listActivePlansByWorkItemIds(tenantId, externalPredecessors).stream()
                .collect(Collectors.toMap(WorkItemPlanEntity::getWorkItemId, plan -> plan, (left, right) -> left));
        Map<Long, Long> starts = new HashMap<>();
        for (OptimizationDependencyEdge edge : externalEdges) {
            WorkItemPlanEntity plan = plans.get(edge.predecessorId());
            if (plan != null && plan.getPlannedEnd() != null) {
                // Successor cannot start before external predecessor's planned end (use max if multiple)
                starts.merge(edge.successorId(), plan.getPlannedEnd(), Math::max);
            } else {
                warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.EXTERNAL_DEPENDENCY,
                        edge.successorId(), "External predecessor state unknown", edge.predecessorId() + " -> " + edge.successorId()));
            }
        }
        return starts;
    }

    private GraphState buildGraphState(Set<Long> ids, List<OptimizationDependencyEdge> edges) {
        // Build predecessor and successor adjacency maps for all selected work items
        Map<Long, Set<Long>> predecessors = new HashMap<>();
        Map<Long, Set<Long>> successors = new HashMap<>();
        ids.forEach(id -> {
            predecessors.put(id, new LinkedHashSet<>());
            successors.put(id, new LinkedHashSet<>());
        });
        for (OptimizationDependencyEdge edge : edges) {
            predecessors.get(edge.successorId()).add(edge.predecessorId());
            successors.get(edge.predecessorId()).add(edge.successorId());
        }
        return new GraphState(predecessors, successors);
    }

    private List<Long> topologicalOrder(Set<Long> ids, Map<Long, Set<Long>> predecessors, Map<Long, Set<Long>> successors) {
        // Kahn's algorithm: process nodes with zero in-degree first
        Map<Long, Integer> indegree = ids.stream().collect(Collectors.toMap(id -> id, id -> predecessors.getOrDefault(id, Set.of()).size()));
        Queue<Long> ready = indegree.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toCollection(ArrayDeque::new));
        List<Long> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            Long current = ready.poll();
            order.add(current);
            // Reduce in-degree of successors; add to queue when they become ready
            successors.getOrDefault(current, Set.of()).stream().sorted().forEach(successor -> {
                indegree.put(successor, indegree.get(successor) - 1);
                if (indegree.get(successor) == 0) {
                    ready.add(successor);
                }
            });
        }
        return order;
    }

    private List<List<Long>> findCycles(Set<Long> ids, Map<Long, Set<Long>> successors) {
        // DFS-based cycle detection tracking the current recursion path
        List<List<Long>> cycles = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Set<Long> visiting = new HashSet<>();
        ArrayDeque<Long> path = new ArrayDeque<>();
        for (Long id : ids) {
            detectCycle(id, successors, visited, visiting, path, cycles);
        }
        return cycles;
    }

    private void detectCycle(Long id, Map<Long, Set<Long>> successors, Set<Long> visited, Set<Long> visiting,
                             ArrayDeque<Long> path, List<List<Long>> cycles) {
        if (visited.contains(id)) {
            return;
        }
        // If node is already in current path, we found a cycle - extract it
        if (visiting.contains(id)) {
            List<Long> cycle = new ArrayList<>(path);
            int index = cycle.indexOf(id);
            cycles.add(index >= 0 ? cycle.subList(index, cycle.size()) : cycle);
            return;
        }
        visiting.add(id);
        path.addLast(id);
        for (Long successor : successors.getOrDefault(id, Set.of())) {
            detectCycle(successor, successors, visited, visiting, path, cycles);
        }
        path.removeLast();
        visiting.remove(id);
        visited.add(id);
    }

    private Comparator<OptimizationWorkItem> workItemComparator() {
        // Sort by: priority score (desc), due date (asc), rank (asc), ID (asc)
        return Comparator.comparing((OptimizationWorkItem item) -> item.priorityScore().score()).reversed()
                .thenComparing(item -> item.workItem().getDueDate(), Comparator.nullsLast(Long::compareTo))
                .thenComparing(item -> item.workItem().getRank(), Comparator.nullsLast(String::compareTo))
                .thenComparing(item -> item.workItem().getId());
    }

    private List<Long> idsOf(List<WorkItemEntity> items) {
        return items.stream().map(WorkItemEntity::getId).toList();
    }

    private boolean positive(Long value) {
        return value != null && value > 0;
    }

    private long defaultDuration(Integer hierarchyLevel) {
        // Sub-tasks get 2h, epics get 3d, stories/tasks get 1d as default estimates
        if (hierarchyLevel != null && hierarchyLevel < 0) {
            return OptimizationConstants.DEFAULT_DURATION_SUBTASK_HOURS * OptimizationConstants.HOUR_MILLIS;
        }
        if (hierarchyLevel != null && hierarchyLevel > 0) {
            return OptimizationConstants.DEFAULT_DURATION_EPIC_DAYS * OptimizationConstants.DAY_MILLIS;
        }
        return OptimizationConstants.DAY_MILLIS;
    }

    private boolean isDone(WorkItemEntity item) {
        return item.getResolutionId() != null || OptimizationConstants.STATUS_CATEGORY_DONE.equalsIgnoreCase(item.getStatusCategoryKey());
    }

    private void addCandidate(Map<Long, CandidateFlags> flagsById, Long candidateId, java.util.function.Consumer<CandidateFlags> marker) {
        // Register a candidate assignee and apply role-specific flags
        if (candidateId == null) {
            return;
        }
        CandidateFlags flags = flagsById.computeIfAbsent(candidateId, id -> new CandidateFlags());
        marker.accept(flags);
    }

    private double baseCost(CandidateFlags flags) {
        // Lower cost = more preferred assignee; discounts applied for existing roles
        double cost = OptimizationConstants.BASE_ASSIGNMENT_COST;
        if (flags.currentAssignee) {
            cost -= OptimizationConstants.CURRENT_ASSIGNEE_DISCOUNT;
        }
        if (flags.componentLead) {
            cost -= OptimizationConstants.COMPONENT_LEAD_DISCOUNT;
        }
        if (flags.projectLead) {
            cost -= OptimizationConstants.PROJECT_LEAD_DISCOUNT;
        }
        if (flags.reporter) {
            cost -= OptimizationConstants.REPORTER_DISCOUNT;
        }
        if (flags.projectMember) {
            cost -= OptimizationConstants.PROJECT_MEMBER_DISCOUNT;
        }
        return cost;
    }

    private record GraphState(Map<Long, Set<Long>> predecessorsById, Map<Long, Set<Long>> successorsById) {
    }

    private static class CandidateFlags {
        private boolean currentAssignee;
        private boolean componentLead;
        private boolean projectLead;
        private boolean reporter;
        private boolean projectMember;
    }
}
