/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.domain.enums.MeetingRequestType;
import serp.project.crm.core.domain.enums.PreferredTimeSlot;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingRequestResponse {
    private Long id;
    private Long teamId;
    private Long preferredUserId;
    private Long assignedTeamMemberId;
    private Long assignedUserId;
    private Long scheduledActivityId;
    private Long scheduledStartTime;

    private Long accountId;
    private String accountName;
    private Long opportunityId;
    private String opportunityName;
    private Long contactId;
    private String contactName;

    private String subject;
    private String description;
    private String location;

    private MeetingRequestType meetingType;
    private PreferredTimeSlot preferredTimeSlot;

    private Long earliestStart;
    private Long latestStart;
    private Long requestedDeadline;
    private Integer durationMinutes;

    private MeetingRequestStatus status;
    private Integer schedulingAttempts;
    private Integer priorityScore;
    private String failureReason;

    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
