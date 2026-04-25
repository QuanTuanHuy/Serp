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
import serp.project.crm.core.domain.dto.request.CloseOpportunityLostRequest;
import serp.project.crm.core.domain.dto.request.CloseOpportunityWonRequest;
import serp.project.crm.core.domain.dto.request.CreateOpportunityRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.dto.request.PipelineFilterRequest;
import serp.project.crm.core.domain.dto.request.ReopenOpportunityRequest;
import serp.project.crm.core.domain.dto.request.UpdateOpportunityRequest;
import serp.project.crm.core.domain.dto.response.OpportunityResponse;
import serp.project.crm.core.domain.dto.response.PipelineResponse;
import serp.project.crm.core.domain.dto.response.PipelineStageResponse;
import serp.project.crm.core.domain.dto.response.PipelineSummaryResponse;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.OpportunityDtoMapper;
import serp.project.crm.core.service.IAccountService;
import serp.project.crm.core.service.IOpportunityService;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityUseCase {

    private final IOpportunityService opportunityService;
    private final IAccountService accountService;
    private final ITeamMemberService teamMemberService;

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
            teamMemberService.getTeamMemberByUserId(userId, tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
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
            OpportunityEntity opportunity;
            if (OpportunityStage.CLOSED_WON.equals(request.getStage())) {
                opportunity = opportunityService.closeAsWon(id, null, request.getNotes(), userId, tenantId);
                if (opportunity.getAccountId() != null) {
                    accountService.updateAccountRevenue(opportunity.getAccountId(), tenantId,
                            opportunity.getActualValue(), true, userId);
                }
            } else if (OpportunityStage.CLOSED_LOST.equals(request.getStage())) {
                opportunity = opportunityService.closeAsLost(id, request.getLossReason(), userId, tenantId);
                accountService.updateAccountRevenue(opportunity.getAccountId(), tenantId, BigDecimal.ZERO, false,
                        userId);
            } else {
                opportunity = opportunityService.changeStage(id, request.getStage(), userId, tenantId);
            }
            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);

            log.info("Opportunity stage changed successfully: {}", id);
            return responseUtils.success(response, "Stage updated successfully");

        } catch (AppException e) {
            log.error("Error changing opportunity stage: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error changing opportunity stage: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> closeOpportunityAsWon(Long id, CloseOpportunityWonRequest request, Long userId,
            Long tenantId) {
        try {
            CloseOpportunityWonRequest safeRequest = request != null ? request : CloseOpportunityWonRequest.builder().build();
            OpportunityEntity opportunity = opportunityService.closeAsWon(id, safeRequest.getActualValue(),
                    safeRequest.getNotes(), userId, tenantId);

            if (opportunity.getAccountId() != null && opportunity.getEstimatedValue() != null) {
                accountService.updateAccountRevenue(
                    opportunity.getAccountId(),
                    tenantId,
                    opportunity.getActualValue(),
                    true,
                    userId);
            }

            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);
            log.info("Opportunity closed as won: {}", id);
            return responseUtils.success(response, "Opportunity closed as won");

        } catch (AppException e) {
            log.error("Error closing opportunity as won: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error closing opportunity as won: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> closeOpportunityAsLost(Long id, CloseOpportunityLostRequest request, Long userId,
            Long tenantId) {
        try {
            OpportunityEntity opportunity = opportunityService.closeAsLost(id, request.getLossReason(), userId,
                    tenantId);

            if (opportunity.getAccountId() != null) {
                accountService.updateAccountRevenue(
                    opportunity.getAccountId(),
                    tenantId,
                    BigDecimal.ZERO,
                    false,
                    userId);
            }


            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);
            log.info("Opportunity closed as lost: {}", id);
            return responseUtils.success(response, "Opportunity closed as lost");

        } catch (AppException e) {
            log.error("Error closing opportunity as lost: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error closing opportunity as lost: {}", e.getMessage(), e);
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

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> reopenOpportunity(Long id, ReopenOpportunityRequest request, Long userId, Long tenantId) {
        OpportunityEntity opportunity = opportunityService.reopenOpportunity(id, request.getStage(),
                request.getReopenReason(), userId, tenantId);
        OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);
        return responseUtils.success(response, "Opportunity reopened successfully");
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
                .filter(opportunity -> opportunity.getStage() != null && opportunity.getStage().isActive())
                .sorted(Comparator.comparing(OpportunityEntity::getExpectedCloseDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<PipelineStageResponse> stages = new ArrayList<>();
        BigDecimal totalPipelineValue = BigDecimal.ZERO;
        BigDecimal weightedPipelineValue = BigDecimal.ZERO;
        int totalOpportunities = 0;

        for (OpportunityStage stage : OpportunityStage.values()) {
            if (!stage.isActive()) {
                continue;
            }
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
                    .opportunities(stageOpportunities.stream().map(opportunityDtoMapper::toResponse).toList())
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

    private static BigDecimal estimatedValueOrZero(OpportunityEntity opportunity) {
        return opportunity.getEstimatedValue() != null ? opportunity.getEstimatedValue() : BigDecimal.ZERO;
    }

    private static BigDecimal weightedValue(OpportunityEntity opportunity) {
        BigDecimal estimatedValue = estimatedValueOrZero(opportunity);
        BigDecimal probability = BigDecimal.valueOf(opportunity.getProbability() != null ? opportunity.getProbability() : 0);
        return estimatedValue.multiply(probability).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
