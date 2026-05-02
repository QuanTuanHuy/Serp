/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateMeetingRequest;
import serp.project.crm.core.domain.dto.response.MeetingRequestResponse;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;

@Component
public class MeetingRequestDtoMapper {

    public MeetingRequestEntity toEntity(CreateMeetingRequest request) {
        if (request == null) {
            return null;
        }

        return MeetingRequestEntity.builder()
                .teamId(request.getTeamId())
                .preferredUserId(request.getPreferredUserId())
                .accountId(request.getAccountId())
                .opportunityId(request.getOpportunityId())
                .contactId(request.getContactId())
                .subject(request.getSubject())
                .description(request.getDescription())
                .location(request.getLocation())
                .meetingType(request.getMeetingType())
                .preferredTimeSlot(request.getPreferredTimeSlot())
                .earliestStart(request.getEarliestStart())
                .latestStart(request.getLatestStart())
                .requestedDeadline(request.getRequestedDeadline())
                .durationMinutes(request.getDurationMinutes())
                .build();
    }

    public MeetingRequestResponse toResponse(MeetingRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        return MeetingRequestResponse.builder()
                .id(entity.getId())
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
                .meetingType(entity.getMeetingType())
                .preferredTimeSlot(entity.getPreferredTimeSlot())
                .earliestStart(entity.getEarliestStart())
                .latestStart(entity.getLatestStart())
                .requestedDeadline(entity.getRequestedDeadline())
                .durationMinutes(entity.getDurationMinutes())
                .status(entity.getStatus())
                .schedulingAttempts(entity.getSchedulingAttempts())
                .priorityScore(entity.getPriorityScore())
                .failureReason(entity.getFailureReason())
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
