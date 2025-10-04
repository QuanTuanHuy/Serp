/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateOpportunityRequest;
import serp.project.crm.core.domain.dto.request.UpdateOpportunityRequest;
import serp.project.crm.core.domain.dto.response.OpportunityResponse;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.mapper.OpportunityDtoMapper;
import serp.project.crm.core.service.IOpportunityService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

/**
 * Opportunity Use Case - Orchestrates opportunity management workflows
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityUseCase {

    private final IOpportunityService opportunityService;
    private final OpportunityDtoMapper opportunityDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional
    public GeneralResponse<?> createOpportunity(CreateOpportunityRequest request, Long tenantId) {
        try {
            log.info("Creating opportunity: {}, tenant: {}", request.getName(), tenantId);

            OpportunityEntity opportunityEntity = opportunityDtoMapper.toEntity(request);
            OpportunityEntity createdOpportunity = opportunityService.createOpportunity(opportunityEntity, tenantId);
            OpportunityResponse response = opportunityDtoMapper.toResponse(createdOpportunity);

            log.info("Opportunity created successfully with ID: {}", createdOpportunity.getId());
            return responseUtils.success(response, "Opportunity created successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating opportunity: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating opportunity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to create opportunity");
        }
    }

    @Transactional
    public GeneralResponse<?> updateOpportunity(Long id, UpdateOpportunityRequest request, Long tenantId) {
        try {
            log.info("Updating opportunity ID: {}, tenant: {}", id, tenantId);

            OpportunityEntity updates = opportunityDtoMapper.toEntity(request);
            OpportunityEntity updatedOpportunity = opportunityService.updateOpportunity(id, updates, tenantId);
            OpportunityResponse response = opportunityDtoMapper.toResponse(updatedOpportunity);

            log.info("Opportunity updated successfully: {}", id);
            return responseUtils.success(response, "Opportunity updated successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating opportunity: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating opportunity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to update opportunity");
        }
    }

    @Transactional
    public GeneralResponse<?> changeOpportunityStage(Long id, OpportunityStage newStage, Long tenantId) {
        try {
            log.info("Changing opportunity ID: {} to stage: {}, tenant: {}", id, newStage, tenantId);

            OpportunityEntity opportunity = opportunityService.changeStage(id, newStage, tenantId);
            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);

            log.info("Opportunity stage changed successfully: {}", id);
            return responseUtils.success(response, "Opportunity stage changed successfully");

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error changing opportunity stage: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error changing opportunity stage: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to change opportunity stage");
        }
    }

    @Transactional
    public GeneralResponse<?> closeOpportunityAsWon(Long id, Long tenantId) {
        try {
            log.info("Closing opportunity ID: {} as won, tenant: {}", id, tenantId);

            OpportunityEntity opportunity = opportunityService.closeAsWon(id, tenantId);
            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);

            log.info("Opportunity closed as won: {}", id);
            return responseUtils.success(response, "Opportunity closed as won");

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error closing opportunity as won: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error closing opportunity as won: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to close opportunity as won");
        }
    }

    @Transactional
    public GeneralResponse<?> closeOpportunityAsLost(Long id, String lostReason, Long tenantId) {
        try {
            log.info("Closing opportunity ID: {} as lost, tenant: {}", id, tenantId);

            OpportunityEntity opportunity = opportunityService.closeAsLost(id, lostReason, tenantId);
            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);

            log.info("Opportunity closed as lost: {}", id);
            return responseUtils.success(response, "Opportunity closed as lost");

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error closing opportunity as lost: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error closing opportunity as lost: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to close opportunity as lost");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getOpportunityById(Long id, Long tenantId) {
        try {
            log.info("Fetching opportunity ID: {}, tenant: {}", id, tenantId);

            OpportunityEntity opportunity = opportunityService.getOpportunityById(id, tenantId)
                    .orElse(null);

            if (opportunity == null) {
                return responseUtils.notFound("Opportunity not found");
            }

            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("Error fetching opportunity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch opportunity");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllOpportunities(Long tenantId, PageRequest pageRequest) {
        try {
            log.info("Fetching all opportunities for tenant: {}", tenantId);

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

    @Transactional
    public GeneralResponse<?> deleteOpportunity(Long id, Long tenantId) {
        try {
            log.info("Deleting opportunity ID: {}, tenant: {}", id, tenantId);

            opportunityService.deleteOpportunity(id, tenantId);

            log.info("Opportunity deleted successfully: {}", id);
            return responseUtils.status("Opportunity deleted successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error deleting opportunity: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting opportunity: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to delete opportunity");
        }
    }
}
