/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;
import serp.project.crm.infrastructure.store.model.ActivityModel;

@Component
@RequiredArgsConstructor
public class ActivityMapper extends BaseMapper {

    public ActivityEntity toEntity(ActivityModel model) {
        if (model == null) {
            return null;
        }

        return ActivityEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .leadId(model.getLeadId())
                .contactId(model.getContactId())
                .customerId(model.getCustomerId())
                .opportunityId(model.getOpportunityId())
                .activityType(stringToEnum(model.getActivityType(), ActivityType.class))
                .subject(model.getSubject())
                .description(model.getDescription())
                .status(stringToEnum(model.getStatus(), ActivityStatus.class))
                .location(model.getLocation())
                .assignedTo(model.getAssignedTo())
                .activityDate(model.getActivityDate())
                .dueDate(model.getDueDate())
                .reminderDate(model.getReminderDate())
                .durationMinutes(model.getDurationMinutes())
                .priority(stringToEnum(model.getPriority(), TaskPriority.class))
                .progressPercent(model.getProgressPercent())
                .attachments(parseJsonToList(model.getAttachments()))
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ActivityModel toModel(ActivityEntity entity) {
        if (entity == null) {
            return null;
        }

        return ActivityModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .leadId(entity.getLeadId())
                .contactId(entity.getContactId())
                .customerId(entity.getCustomerId())
                .opportunityId(entity.getOpportunityId())
                .activityType(enumToString(entity.getActivityType()))
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .status(enumToString(entity.getStatus()))
                .location(entity.getLocation())
                .assignedTo(entity.getAssignedTo())
                .activityDate(entity.getActivityDate())
                .dueDate(entity.getDueDate())
                .reminderDate(entity.getReminderDate())
                .durationMinutes(entity.getDurationMinutes())
                .priority(enumToString(entity.getPriority()))
                .progressPercent(entity.getProgressPercent())
                .attachments(serializeListToJson(entity.getAttachments()))
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ActivityEntity> toEntityList(List<ActivityModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
