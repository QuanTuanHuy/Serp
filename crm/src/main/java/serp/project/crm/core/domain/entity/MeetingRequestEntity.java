/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.domain.enums.MeetingRequestType;
import serp.project.crm.core.domain.enums.PreferredTimeSlot;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MeetingRequestEntity extends BaseEntity {
    private Long teamId;
    private Long preferredUserId;
    private Long assignedTeamMemberId;
    private Long assignedUserId;
    private Long scheduledActivityId;
    private Long scheduledStartTime;

    private Long accountId;
    private Long opportunityId;
    private Long contactId;

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

    public int getEffectiveDurationMinutes() {
        if (durationMinutes != null && durationMinutes > 0) {
            return durationMinutes;
        }
        return meetingType != null ? meetingType.getDefaultDurationMinutes() : 60;
    }

    public boolean isExpired() {
        return latestStart != null && System.currentTimeMillis() > latestStart;
    }

    public boolean isFinalStatus() {
        return MeetingRequestStatus.SCHEDULED.equals(status)
                || MeetingRequestStatus.FAILED.equals(status)
                || MeetingRequestStatus.CANCELLED.equals(status);
    }

    public void incrementAttempts() {
        if (schedulingAttempts == null) {
            schedulingAttempts = 0;
        }
        schedulingAttempts++;
    }

    public void setDefaults() {
        if (status == null) {
            status = MeetingRequestStatus.PENDING;
        }
        if (schedulingAttempts == null) {
            schedulingAttempts = 0;
        }
        if (priorityScore == null) {
            priorityScore = 0;
        }
    }
}
