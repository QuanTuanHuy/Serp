/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.ActivityOutcome;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ActivityEntity extends BaseEntity {
    private Long leadId;
    private Long contactId;
    private Long accountId;
    private Long opportunityId;

    private ActivityType activityType;
    private String subject;
    private String description;
    private ActivityStatus status;
    private String location;
    private Long assignedTo;

    private Long activityDate; // Unix timestamp
    private Long dueDate;
    private Long reminderDate;

    private Integer durationMinutes;
    private TaskPriority priority;
    private Integer progressPercent;
    private ActivityOutcome outcome;
    private String notes;

    private List<String> attachments;

    public boolean hasAnyLink() {
        return leadId != null || contactId != null || accountId != null || opportunityId != null;
    }

    public boolean isTask() {
        return ActivityType.TASK.equals(this.activityType);
    }

    public boolean isMeeting() {
        return ActivityType.MEETING.equals(this.activityType);
    }

    public boolean isCall() {
        return ActivityType.CALL.equals(this.activityType);
    }

    public boolean isProgressValid() {
        return progressPercent == null || (progressPercent >= 0 && progressPercent <= 100);
    }

    public boolean isDurationValid() {
        return durationMinutes == null || durationMinutes > 0;
    }

    // State management
    public boolean isCompleted() {
        return ActivityStatus.COMPLETED.equals(this.status);
    }

    public boolean isCancelled() {
        return ActivityStatus.CANCELLED.equals(this.status);
    }

    public boolean isOverdue() {
        if (isCompleted() || dueDate == null) {
            return false;
        }
        return System.currentTimeMillis() > dueDate;
    }

    public void markAsCompleted(ActivityOutcome outcome, String notes, Long completedBy) {
        if (isCompleted()) {
            throw new IllegalStateException("Activity is already completed");
        }
        if (isCancelled()) {
            throw new IllegalStateException("Cannot complete a cancelled activity");
        }
        this.status = ActivityStatus.COMPLETED;
        this.setUpdatedBy(completedBy);
        this.progressPercent = 100;
        if (outcome != null) {
            this.outcome = outcome;
        }
        if (notes != null) {
            this.notes = notes;
        }
    }

    public void markAsCancelled(Long cancelledBy) {
        if (isCompleted()) {
            throw new IllegalStateException("Cannot cancel a completed activity.");
        }

        this.status = ActivityStatus.CANCELLED;
        this.setUpdatedBy(cancelledBy);
    }

    // Scheduling
    public void reschedule(Long newDueDate, Long rescheduledBy) {
        if (isCompleted()) {
            throw new IllegalStateException("Cannot reschedule a completed activity.");
        }
        this.dueDate = newDueDate;
        this.setUpdatedBy(rescheduledBy);
    }

    public void updateFrom(ActivityEntity updates) {
        if (this.isCompleted()) {
            throw new IllegalStateException("Cannot update completed activities");
        }

        if (updates.getSubject() != null)
            this.subject = updates.getSubject();
        if (updates.getDescription() != null)
            this.description = updates.getDescription();
        if (updates.getActivityType() != null)
            this.activityType = updates.getActivityType();
        if (updates.getStatus() != null)
            this.status = updates.getStatus();
        if (updates.getLocation() != null)
            this.location = updates.getLocation();
        if (updates.getAssignedTo() != null)
            this.assignedTo = updates.getAssignedTo();
        if (updates.getActivityDate() != null)
            this.activityDate = updates.getActivityDate();
        if (updates.getDueDate() != null)
            this.dueDate = updates.getDueDate();
        if (updates.getReminderDate() != null)
            this.reminderDate = updates.getReminderDate();
        if (updates.getDurationMinutes() != null)
            this.durationMinutes = updates.getDurationMinutes();
        if (updates.getPriority() != null)
            this.priority = updates.getPriority();
        if (updates.getProgressPercent() != null)
            this.progressPercent = updates.getProgressPercent();
        if (updates.getOutcome() != null)
            this.outcome = updates.getOutcome();
        if (updates.getNotes() != null)
            this.notes = updates.getNotes();
        if (updates.getAttachments() != null)
            this.attachments = updates.getAttachments();
    }

    public void setDefaults() {
        if (this.status == null) {
            this.status = ActivityStatus.PLANNED;
        }
        if (isTask() && this.progressPercent == null) {
            this.progressPercent = 0;
        }
        if (!isTask()) {
            this.progressPercent = null;
        }
        if (this.priority == null) {
            this.priority = TaskPriority.MEDIUM;
        }
    }
}
