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
import serp.project.crm.core.domain.dto.request.CreateOpportunityRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateOpportunityRequest;
import serp.project.crm.core.domain.dto.response.OpportunityResponse;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.OpportunityDtoMapper;
import serp.project.crm.core.service.ICustomerService;
import serp.project.crm.core.service.IOpportunityService;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityUseCase {

    private final IOpportunityService opportunityService;
    private final ICustomerService customerService;
    private final ITeamMemberService teamMemberService;

    private final OpportunityDtoMapper opportunityDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createOpportunity(CreateOpportunityRequest request, Long userId, Long tenantId) {
        try {
            CustomerEntity customer = customerService.getCustomerById(request.getCustomerId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.CUSTOMER_NOT_FOUND));
            TeamMemberEntity member = teamMemberService.getTeamMemberByUserId(userId, tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
            if (opportunityService.existByCustomerIdAndName(customer.getId(), request.getName(), tenantId)) {
                throw new AppException(ErrorMessage.OPPORTUNITY_ALREADY_EXISTS);
            }
            if (request.getAssignedTo() != null && !member.getId().equals(request.getAssignedTo())) {
                teamMemberService.getTeamMemberById(request.getAssignedTo(), tenantId)
                        .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
            } else {
                request.setAssignedTo(member.getId());
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
    public GeneralResponse<?> changeOpportunityStage(Long id, OpportunityStage newStage, Long tenantId) {
        try {
            OpportunityEntity opportunity = opportunityService.changeStage(id, newStage, tenantId);
            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);

            log.info("Opportunity stage changed successfully: {}", id);
            return responseUtils.success(response, "Opportunity stage changed successfully");

        } catch (AppException e) {
            log.error("Error changing opportunity stage: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error changing opportunity stage: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> closeOpportunityAsWon(Long id, Long tenantId) {
        try {
            OpportunityEntity opportunity = opportunityService.closeAsWon(id, tenantId);

            if (opportunity.getCustomerId() != null && opportunity.getEstimatedValue() != null) {
                customerService.updateCustomerRevenue(
                    opportunity.getCustomerId(),
                    tenantId,
                    opportunity.getEstimatedValue(),
                    true);
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
    public GeneralResponse<?> closeOpportunityAsLost(Long id, String lostReason, Long tenantId) {
        try {
            OpportunityEntity opportunity = opportunityService.closeAsLost(id, lostReason, tenantId);

            if (opportunity.getCustomerId() != null) {
                customerService.updateCustomerRevenue(
                    opportunity.getCustomerId(),
                    tenantId,
                    BigDecimal.ZERO,
                    false);
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
}
