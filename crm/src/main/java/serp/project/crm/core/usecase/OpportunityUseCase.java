/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.AssignOpportunityRequest;
import serp.project.crm.core.domain.dto.request.ChangeOpportunityStageRequest;
import serp.project.crm.core.domain.dto.request.CreateOpportunityRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.dto.request.PipelineFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateOpportunityRequest;
import serp.project.crm.core.domain.dto.response.OpportunityResponse;
import serp.project.crm.core.domain.dto.response.PipelineResponse;
import serp.project.crm.core.domain.dto.response.PipelineStageResponse;
import serp.project.crm.core.domain.dto.response.PipelineSummaryResponse;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.OpportunityDtoMapper;
import serp.project.crm.core.port.client.IUserProfileClient;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.service.IAccountService;
import serp.project.crm.core.service.IOpportunityService;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityUseCase {

    private final IOpportunityService opportunityService;
    private final IAccountService accountService;
    private final ITeamMemberService teamMemberService;
    private final IActivityPort activityPort;
    private final ILeadPort leadPort;
    private final IUserProfileClient userProfileClient;

    private final OpportunityDtoMapper opportunityDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createOpportunity(CreateOpportunityRequest request, Long userId, Long tenantId) {
        try {
            AccountEntity account = accountService.getAccountById(request.getAccountId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
            if (!account.isActive()) {
                throw new AppException(ErrorMessage.ACCOUNT_INACTIVE);
            }
            // teamMemberService.getTeamMemberByUserId(userId, tenantId)
            //         .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
            if (opportunityService.existByAccountIdAndName(account.getId(), request.getName(), tenantId)) {
                throw new AppException(ErrorMessage.OPPORTUNITY_ALREADY_EXISTS);
            }
            if (request.getAssignedTo() != null && !userId.equals(request.getAssignedTo())) {
                teamMemberService.getTeamMemberByUserId(request.getAssignedTo(), tenantId)
                        .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
            } else {
                request.setAssignedTo(userId);
            }

            OpportunityEntity opportunity = opportunityDtoMapper.toEntity(request);
            OpportunityEntity createdOpportunity = opportunityService.createOpportunity(opportunity, tenantId);
            OpportunityResponse response = opportunityDtoMapper.toResponse(createdOpportunity);

            log.info("Opportunity created successfully with ID: {}", createdOpportunity.getId());
            return responseUtils.success(response, "Opportunity created successfully");
        } catch (AppException e) {
            log.error("Error creating opportunity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating opportunity: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateOpportunity(Long id, UpdateOpportunityRequest request, Long userId, Long tenantId) {
        try {
            OpportunityEntity updates = opportunityDtoMapper.toEntity(request);
            OpportunityEntity updatedOpportunity = opportunityService.updateOpportunity(id, updates, tenantId);
            OpportunityResponse response = opportunityDtoMapper.toResponse(updatedOpportunity);

            log.info("Opportunity updated successfully: {}", id);
            return responseUtils.success(response, "Opportunity updated successfully");

        } catch (AppException e) {
            log.error("Error updating opportunity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating opportunity: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> changeOpportunityStage(Long id, ChangeOpportunityStageRequest request, Long userId,
            Long tenantId) {
        try {
            OpportunityEntity currentOpportunity = opportunityService.getOpportunityById(id, tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));

            OpportunityEntity opportunity;
            String successMessage;
            OpportunityStage targetStage = request.getStage();
            OpportunityStage currentStage = currentOpportunity.getStage();

            if (OpportunityStage.CLOSED_LOST.equals(currentStage) && targetStage != null && targetStage.isActive()) {
                String reopenReason = Optional.ofNullable(request.getReopenReason())
                        .map(String::trim)
                        .orElse(null);
                if (reopenReason == null || reopenReason.isEmpty()) {
                    throw new AppException("Reopen reason is required when reopening a closed lost opportunity");
                }

                opportunity = opportunityService.reopenOpportunity(id, targetStage, reopenReason, userId, tenantId);
                successMessage = "Opportunity reopened successfully";
            } else if (OpportunityStage.CLOSED_WON.equals(targetStage)) {
                opportunity = opportunityService.closeAsWon(id, request.getActualValue(), request.getNotes(), userId,
                        tenantId);
                if (opportunity.getAccountId() != null) {
                    accountService.updateAccountRevenue(opportunity.getAccountId(), tenantId,
                            opportunity.getActualValue(), true, userId);
                }
                successMessage = "Opportunity closed as won";
            } else if (OpportunityStage.CLOSED_LOST.equals(targetStage)) {
                String lossReason = Optional.ofNullable(request.getLossReason())
                        .map(String::trim)
                        .orElse(null);
                if (lossReason == null || lossReason.isEmpty()) {
                    throw new AppException("Loss reason is required");
                }

                opportunity = opportunityService.closeAsLost(id, lossReason, userId, tenantId);
                if (opportunity.getAccountId() != null) {
                    accountService.updateAccountRevenue(opportunity.getAccountId(), tenantId, BigDecimal.ZERO, false,
                            userId);
                }
                successMessage = "Opportunity closed as lost";
            } else {
                opportunity = opportunityService.changeStage(id, targetStage, userId, tenantId);
                successMessage = "Stage updated successfully";
            }

            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);

            log.info("Opportunity stage changed successfully: {}", id);
            return responseUtils.success(response, successMessage);

        } catch (AppException e) {
            log.error("Error changing opportunity stage: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error changing opportunity stage: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignOpportunity(Long id, AssignOpportunityRequest request, Long userId, Long tenantId) {
        OpportunityEntity opportunity = opportunityService.assignOpportunity(id, request.getAssignedTo(), userId,
                tenantId);
        OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);
        return responseUtils.success(response, "Opportunity assigned successfully");
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getOpportunityById(Long id, Long tenantId) {
        try {
            OpportunityEntity opportunity = opportunityService.getOpportunityById(id, tenantId)
                    .orElse(null);

            if (opportunity == null) {
                return responseUtils.notFound(ErrorMessage.OPPORTUNITY_NOT_FOUND);
            }

            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);
            enrichOpportunityResponses(List.of(response), tenantId);
            return responseUtils.success(response);
        } catch (AppException e) {
            log.error("Error fetching opportunity: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching opportunity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch opportunity");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllOpportunities(Long tenantId, PageRequest pageRequest) {
        try {
            var result = opportunityService.getAllOpportunities(tenantId, pageRequest);

            List<OpportunityResponse> opportunityResponses = result.getFirst().stream()
                    .map(opportunityDtoMapper::toResponse)
                    .toList();
            enrichOpportunityResponses(opportunityResponses, tenantId);

            PageResponse<OpportunityResponse> pageResponse = PageResponse.of(
                    opportunityResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("Error fetching opportunities: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch opportunities");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deleteOpportunity(Long id, Long tenantId) {
        try {
            opportunityService.deleteOpportunity(id, tenantId);

            log.info("Opportunity deleted successfully: {}", id);
            return responseUtils.status("Opportunity deleted successfully");

        } catch (AppException e) {
            log.error("Validation error deleting opportunity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting opportunity: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> filterOpportunities(OpportunityFilterRequest filter,
            Long tenantId) {
        try {
            var result = opportunityService.filterOpportunities(filter, tenantId, filter.toPageRequest());

            List<OpportunityResponse> opportunityResponses = result.getFirst().stream()
                    .map(opportunityDtoMapper::toResponse)
                    .toList();
            enrichOpportunityResponses(opportunityResponses, tenantId);

            PageResponse<OpportunityResponse> pageResponse = PageResponse.of(
                    opportunityResponses, filter.toPageRequest(), result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("Error filtering opportunities: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to filter opportunities");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getPipeline(PipelineFilterRequest request, Long tenantId) {
        PipelineFilterRequest safeRequest = request != null ? request : PipelineFilterRequest.builder().build();
        OpportunityFilterRequest filter = OpportunityFilterRequest.builder()
                .accountId(safeRequest.getAccountId())
                .assignedTo(safeRequest.getAssignedTo())
                .expectedCloseDateFrom(safeRequest.getFromDate())
                .expectedCloseDateTo(safeRequest.getToDate())
                .build();
        List<OpportunityEntity> opportunities = opportunityService.filterAllOpportunities(filter, tenantId).stream()
                .filter(opportunity -> opportunity.getStage() != null)
                .sorted(Comparator.comparing(OpportunityEntity::getExpectedCloseDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<PipelineStageResponse> stages = new ArrayList<>();
        BigDecimal totalPipelineValue = BigDecimal.ZERO;
        BigDecimal weightedPipelineValue = BigDecimal.ZERO;
        int totalOpportunities = 0;
        List<OpportunityResponse> pipelineOpportunityResponses = opportunities.stream()
                .map(opportunityDtoMapper::toResponse)
                .toList();
        enrichOpportunityResponses(pipelineOpportunityResponses, tenantId);
        Map<Long, OpportunityResponse> enrichedById = new HashMap<>();
        pipelineOpportunityResponses.forEach(response -> enrichedById.put(response.getId(), response));

        for (OpportunityStage stage : OpportunityStage.values()) {
            List<OpportunityEntity> stageOpportunities = opportunities.stream()
                    .filter(opportunity -> stage.equals(opportunity.getStage()))
                    .toList();
            BigDecimal totalValue = stageOpportunities.stream()
                    .map(OpportunityUseCase::estimatedValueOrZero)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal weightedValue = stageOpportunities.stream()
                    .map(OpportunityUseCase::weightedValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalPipelineValue = totalPipelineValue.add(totalValue);
            weightedPipelineValue = weightedPipelineValue.add(weightedValue);
            totalOpportunities += stageOpportunities.size();

            stages.add(PipelineStageResponse.builder()
                    .stage(stage)
                    .count(stageOpportunities.size())
                    .totalValue(totalValue)
                    .weightedValue(weightedValue)
                    .opportunities(stageOpportunities.stream()
                            .map(opportunity -> enrichedById.get(opportunity.getId()))
                            .filter(Objects::nonNull)
                            .toList())
                    .build());
        }

        BigDecimal averageDealSize = totalOpportunities == 0 ? BigDecimal.ZERO
                : totalPipelineValue.divide(BigDecimal.valueOf(totalOpportunities), 2, RoundingMode.HALF_UP);
        PipelineSummaryResponse summary = PipelineSummaryResponse.builder()
                .totalOpportunities(totalOpportunities)
                .totalPipelineValue(totalPipelineValue)
                .weightedPipelineValue(weightedPipelineValue)
                .averageDealSize(averageDealSize)
                .build();
        PipelineResponse response = PipelineResponse.builder()
                .stages(stages)
                .summary(summary)
                .build();
        return responseUtils.success(response);
    }

    private void enrichOpportunityResponses(List<OpportunityResponse> responses, Long tenantId) {
        if (responses == null || responses.isEmpty() || tenantId == null) {
            return;
        }

        List<Long> accountIds = responses.stream()
                .map(OpportunityResponse::getAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> accountNames = loadAccountNames(accountIds, tenantId);

        List<Long> leadIds = responses.stream()
                .map(OpportunityResponse::getLeadId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> leadNames = loadLeadNames(leadIds, tenantId);

        List<Long> userIds = responses.stream()
                .map(OpportunityResponse::getAssignedTo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> userNames = loadUserNames(userIds);

        List<Long> opportunityIds = responses.stream()
                .map(OpportunityResponse::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, List<ActivityEntity>> activitiesByOpportunityId = loadActivitiesByOpportunityId(
                opportunityIds, tenantId);

        for (OpportunityResponse response : responses) {
            response.setAccountName(accountNames.get(response.getAccountId()));
            response.setLeadName(leadNames.get(response.getLeadId()));
            response.setAssignedToName(userNames.get(response.getAssignedTo()));
            applyActivitySummary(response, activitiesByOpportunityId.getOrDefault(response.getId(), List.of()));
        }
    }

    private Map<Long, String> loadAccountNames(List<Long> accountIds, Long tenantId) {
        if (accountIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<Long, String> accountNames = new HashMap<>();
            accountService.getAccountsByIds(accountIds, tenantId)
                    .forEach(account -> putName(accountNames, account.getId(), account.getName()));
            return accountNames;
        } catch (Exception e) {
            log.warn("Unable to enrich opportunity account names: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, String> loadLeadNames(List<Long> leadIds, Long tenantId) {
        if (leadIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<Long, String> leadNames = new HashMap<>();
            leadPort.findByIds(leadIds, tenantId)
                    .forEach(lead -> putName(leadNames, lead.getId(), lead.getName()));
            return leadNames;
        } catch (Exception e) {
            log.warn("Unable to enrich opportunity lead names: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, String> loadUserNames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<Long, String> userNames = new HashMap<>();
            userProfileClient.getUserProfilesByIds(userIds)
                    .forEach(profile -> putName(userNames, profile.getId(), profile.getFullName()));
            return userNames;
        } catch (Exception e) {
            log.warn("Unable to enrich opportunity owner names: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, List<ActivityEntity>> loadActivitiesByOpportunityId(List<Long> opportunityIds, Long tenantId) {
        if (opportunityIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return activityPort.findByOpportunityIds(opportunityIds, tenantId).stream()
                    .filter(activity -> activity.getOpportunityId() != null)
                    .collect(Collectors.groupingBy(ActivityEntity::getOpportunityId));
        } catch (Exception e) {
            log.warn("Unable to enrich opportunity activity summaries: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private static void putName(Map<Long, String> names, Long id, String name) {
        if (id == null || name == null || name.trim().isEmpty()) {
            return;
        }
        names.put(id, name.trim());
    }

    private void applyActivitySummary(OpportunityResponse response, List<ActivityEntity> activities) {
        long now = System.currentTimeMillis();
        int openCount = 0;
        int overdueCount = 0;
        Long lastActivityAt = null;
        Long nextActivityAt = null;

        for (ActivityEntity activity : activities) {
            Long activityTimestamp = preferredActivityTimestamp(activity);
            if (activityTimestamp != null) {
                lastActivityAt = maxTimestamp(lastActivityAt, activityTimestamp);
            }

            boolean open = !ActivityStatus.COMPLETED.equals(activity.getStatus())
                    && !ActivityStatus.CANCELLED.equals(activity.getStatus());
            if (!open) {
                continue;
            }

            openCount++;
            Long dueOrActivityAt = preferredDueTimestamp(activity);
            if (dueOrActivityAt != null && dueOrActivityAt < now) {
                overdueCount++;
            }
            if (dueOrActivityAt != null && dueOrActivityAt >= now) {
                nextActivityAt = minTimestamp(nextActivityAt, dueOrActivityAt);
            }
        }

        response.setLastActivityAt(lastActivityAt);
        response.setNextActivityAt(nextActivityAt);
        response.setOpenActivityCount(openCount);
        response.setOverdueActivityCount(overdueCount);
    }

    private static Long preferredActivityTimestamp(ActivityEntity activity) {
        if (activity.getActivityDate() != null) {
            return activity.getActivityDate();
        }
        if (activity.getDueDate() != null) {
            return activity.getDueDate();
        }
        return activity.getUpdatedAt();
    }

    private static Long preferredDueTimestamp(ActivityEntity activity) {
        if (activity.getActivityDate() != null) {
            return activity.getActivityDate();
        }
        return activity.getDueDate();
    }

    private static Long maxTimestamp(Long current, Long candidate) {
        return current == null || candidate > current ? candidate : current;
    }

    private static Long minTimestamp(Long current, Long candidate) {
        return current == null || candidate < current ? candidate : current;
    }

    private static BigDecimal estimatedValueOrZero(OpportunityEntity opportunity) {
        return opportunity.getEstimatedValue() != null ? opportunity.getEstimatedValue() : BigDecimal.ZERO;
    }

    private static BigDecimal weightedValue(OpportunityEntity opportunity) {
        BigDecimal estimatedValue = estimatedValueOrZero(opportunity);
        BigDecimal probability = BigDecimal.valueOf(opportunity.getProbability() != null ? opportunity.getProbability() : 0);
        return estimatedValue.multiply(probability).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
