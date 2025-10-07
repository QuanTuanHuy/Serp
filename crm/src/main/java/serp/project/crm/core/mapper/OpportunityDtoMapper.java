/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateOpportunityRequest;
import serp.project.crm.core.domain.dto.request.UpdateOpportunityRequest;
import serp.project.crm.core.domain.dto.response.OpportunityResponse;
import serp.project.crm.core.domain.entity.OpportunityEntity;

@Component
public class OpportunityDtoMapper {

    public OpportunityEntity toEntity(CreateOpportunityRequest request) {
        if (request == null) {
            return null;
        }

        return OpportunityEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .leadId(request.getLeadId())
                .customerId(request.getCustomerId())
                .stage(request.getStage())
                .estimatedValue(request.getEstimatedValue())
                .expectedCloseDate(request.getExpectedCloseDate())
                .assignedTo(request.getAssignedTo())
                .notes(request.getNotes())
                .build();
    }

    public OpportunityEntity toEntity(UpdateOpportunityRequest request) {
        if (request == null) {
            return null;
        }

        return OpportunityEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .stage(request.getStage())
                .estimatedValue(request.getEstimatedValue())
                .probability(request.getProbability())
                .expectedCloseDate(request.getExpectedCloseDate())
                .assignedTo(request.getAssignedTo())
                .notes(request.getNotes())
                .build();
    }

    public OpportunityResponse toResponse(OpportunityEntity entity) {
        if (entity == null) {
            return null;
        }

        return OpportunityResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .leadId(entity.getLeadId())
                .customerId(entity.getCustomerId())
                .stage(entity.getStage())
                .estimatedValue(entity.getEstimatedValue())
                .probability(entity.getProbability())
                .expectedCloseDate(entity.getExpectedCloseDate())
                .actualCloseDate(entity.getActualCloseDate())
                .assignedTo(entity.getAssignedTo())
                .notes(entity.getNotes())
                .lossReason(entity.getLossReason())
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
