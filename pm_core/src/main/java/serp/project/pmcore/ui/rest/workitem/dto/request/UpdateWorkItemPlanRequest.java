/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import serp.project.pmcore.application.workitem.command.schedule.UpdateWorkItemPlanAllocationCommand;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateWorkItemPlanRequest {

    @NotNull
    @Positive
    private Long plannedStart;

    @NotNull
    @Positive
    private Long plannedEnd;

    private Boolean locked;

    @Valid
    private List<AllocationRequest> allocations = List.of();

    public List<UpdateWorkItemPlanAllocationCommand> toAllocationCommands() {
        return allocations == null
                ? List.of()
                : allocations.stream()
                        .map(AllocationRequest::toCommand)
                        .toList();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AllocationRequest {

        @NotNull
        @Positive
        private Long assigneeId;

        @NotNull
        @Positive
        private Long start;

        @NotNull
        @Positive
        private Long end;

        @NotNull
        @Positive
        private Long effortMillis;

        public UpdateWorkItemPlanAllocationCommand toCommand() {
            return new UpdateWorkItemPlanAllocationCommand(
                    assigneeId,
                    start,
                    end,
                    effortMillis
            );
        }
    }
}
