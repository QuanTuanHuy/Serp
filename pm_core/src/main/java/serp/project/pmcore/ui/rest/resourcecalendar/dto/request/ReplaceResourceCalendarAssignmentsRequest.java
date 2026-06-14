/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.resourcecalendar.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplaceResourceCalendarAssignmentsRequest {
    @NotNull(message = "assignments is required")
    private List<@Valid AssignmentRequest> assignments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentRequest {
        @NotNull(message = "userId is required")
        private Long userId;

        @NotNull(message = "effectiveFrom is required")
        private LocalDate effectiveFrom;

        private LocalDate effectiveTo;
    }
}
