/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.domain.enums.MeetingRequestType;
import serp.project.crm.core.domain.enums.PreferredTimeSlot;
import serp.project.crm.infrastructure.store.model.MeetingRequestModel;

import java.util.List;

@Component
public class MeetingRequestMapper extends BaseMapper {

    public MeetingRequestEntity toEntity(MeetingRequestModel model) {
        if (model == null) {
            return null;
        }

        return MeetingRequestEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .teamId(model.getTeamId())
                .preferredUserId(model.getPreferredUserId())
                .assignedTeamMemberId(model.getAssignedTeamMemberId())
                .assignedUserId(model.getAssignedUserId())
                .scheduledActivityId(model.getScheduledActivityId())
                .scheduledStartTime(model.getScheduledStartTime())
                .accountId(model.getAccountId())
                .opportunityId(model.getOpportunityId())
                .contactId(model.getContactId())
                .subject(model.getSubject())
                .description(model.getDescription())
                .location(model.getLocation())
                .meetingType(stringToEnum(model.getMeetingType(), MeetingRequestType.class))
                .preferredTimeSlot(stringToEnum(model.getPreferredTimeSlot(), PreferredTimeSlot.class))
                .earliestStart(model.getEarliestStart())
                .latestStart(model.getLatestStart())
                .requestedDeadline(model.getRequestedDeadline())
                .durationMinutes(model.getDurationMinutes())
                .status(stringToEnum(model.getStatus(), MeetingRequestStatus.class))
                .schedulingAttempts(model.getSchedulingAttempts())
                .priorityScore(model.getPriorityScore())
                .failureReason(model.getFailureReason())
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public MeetingRequestModel toModel(MeetingRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        return MeetingRequestModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .teamId(entity.getTeamId())
                .preferredUserId(entity.getPreferredUserId())
                .assignedTeamMemberId(entity.getAssignedTeamMemberId())
                .assignedUserId(entity.getAssignedUserId())
                .scheduledActivityId(entity.getScheduledActivityId())
                .scheduledStartTime(entity.getScheduledStartTime())
                .accountId(entity.getAccountId())
                .opportunityId(entity.getOpportunityId())
                .contactId(entity.getContactId())
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .location(entity.getLocation())
                .meetingType(enumToString(entity.getMeetingType()))
                .preferredTimeSlot(enumToString(entity.getPreferredTimeSlot()))
                .earliestStart(entity.getEarliestStart())
                .latestStart(entity.getLatestStart())
                .requestedDeadline(entity.getRequestedDeadline())
                .durationMinutes(entity.getDurationMinutes())
                .status(enumToString(entity.getStatus()))
                .schedulingAttempts(entity.getSchedulingAttempts())
                .priorityScore(entity.getPriorityScore())
                .failureReason(entity.getFailureReason())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<MeetingRequestEntity> toEntityList(List<MeetingRequestModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
