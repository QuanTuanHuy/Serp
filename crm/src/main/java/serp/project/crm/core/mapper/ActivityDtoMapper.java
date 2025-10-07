/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateActivityRequest;
import serp.project.crm.core.domain.dto.request.UpdateActivityRequest;
import serp.project.crm.core.domain.dto.response.ActivityResponse;
import serp.project.crm.core.domain.entity.ActivityEntity;

@Component
public class ActivityDtoMapper {

    public ActivityEntity toEntity(CreateActivityRequest request) {
        if (request == null) {
            return null;
        }

        return ActivityEntity.builder()
                .subject(request.getSubject())
                .description(request.getDescription())
                .activityType(request.getActivityType())
                .status(request.getStatus())
                .location(request.getLocation())
                .leadId(request.getRelatedToLeadId())
                .customerId(request.getRelatedToCustomerId())
                .opportunityId(request.getRelatedToOpportunityId())
                .contactId(request.getRelatedToContactId())
                .assignedTo(request.getAssignedTo())
                .activityDate(request.getActivityDate())
                .dueDate(request.getDueDate())
                .reminderDate(request.getReminderDate())
                .durationMinutes(request.getDurationMinutes())
                .priority(request.getPriority())
                .progressPercent(request.getProgressPercent())
                .build();
    }

    public ActivityEntity toEntity(UpdateActivityRequest request) {
        if (request == null) {
            return null;
        }

        return ActivityEntity.builder()
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(request.getStatus())
                .location(request.getLocation())
                .assignedTo(request.getAssignedTo())
                .activityDate(request.getActivityDate())
                .dueDate(request.getDueDate())
                .reminderDate(request.getReminderDate())
                .durationMinutes(request.getDurationMinutes())
                .priority(request.getPriority())
                .progressPercent(request.getProgressPercent())
                .build();
    }

    public ActivityResponse toResponse(ActivityEntity entity) {
        if (entity == null) {
            return null;
        }

        return ActivityResponse.builder()
                .id(entity.getId())
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .activityType(entity.getActivityType())
                .status(entity.getStatus())
                .location(entity.getLocation())
                .relatedToLeadId(entity.getLeadId())
                .relatedToCustomerId(entity.getCustomerId())
                .relatedToOpportunityId(entity.getOpportunityId())
                .relatedToContactId(entity.getContactId())
                .assignedTo(entity.getAssignedTo())
                .activityDate(entity.getActivityDate())
                .dueDate(entity.getDueDate())
                .reminderDate(entity.getReminderDate())
                .durationMinutes(entity.getDurationMinutes())
                .priority(entity.getPriority())
                .progressPercent(entity.getProgressPercent())
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
