/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.MeetingRequestType;
import serp.project.crm.core.domain.enums.PreferredTimeSlot;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateMeetingRequest {
    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    private Long opportunityId;
    private Long contactId;
    private Long preferredUserId;

    @Size(max = 255, message = "Subject must not exceed 255 characters")
    private String subject;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @NotNull(message = "Meeting type is required")
    private MeetingRequestType meetingType;

    private PreferredTimeSlot preferredTimeSlot;

    @NotNull(message = "Earliest start is required")
    private Long earliestStart;

    @NotNull(message = "Latest start is required")
    private Long latestStart;

    @NotNull(message = "Requested deadline is required")
    private Long requestedDeadline;

    @Min(value = 15, message = "Duration must be at least 15 minutes")
    private Integer durationMinutes;
}
