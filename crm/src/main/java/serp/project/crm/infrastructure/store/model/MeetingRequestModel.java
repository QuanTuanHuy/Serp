/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "meeting_requests", indexes = {
        @Index(name = "idx_meeting_requests_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_meeting_requests_status_deadline", columnList = "status, requested_deadline"),
        @Index(name = "idx_meeting_requests_team_id", columnList = "team_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MeetingRequestModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "preferred_user_id")
    private Long preferredUserId;

    @Column(name = "assigned_team_member_id")
    private Long assignedTeamMemberId;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(name = "scheduled_activity_id")
    private Long scheduledActivityId;

    @Column(name = "scheduled_start_time")
    private Long scheduledStartTime;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "opportunity_id")
    private Long opportunityId;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "meeting_type", nullable = false, length = 50)
    private String meetingType;

    @Column(name = "preferred_time_slot", length = 50)
    private String preferredTimeSlot;

    @Column(name = "earliest_start", nullable = false)
    private Long earliestStart;

    @Column(name = "latest_start", nullable = false)
    private Long latestStart;

    @Column(name = "requested_deadline", nullable = false)
    private Long requestedDeadline;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "scheduling_attempts", nullable = false)
    private Integer schedulingAttempts;

    @Column(name = "priority_score", nullable = false)
    private Integer priorityScore;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;
}
