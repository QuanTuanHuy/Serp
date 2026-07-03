/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.optimization.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import serp.project.pmcore.application.optimization.command.update.BatchUpdateOptimizationRunItemDecisionsCommand;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BatchUpdateOptimizationRunItemDecisionsRequest {
    @Valid
    @NotEmpty
    private List<ItemDecisionRequest> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ItemDecisionRequest {
        @NotNull
        private Long workItemId;
        private OptimizationDecision assignmentDecision;
        private OptimizationDecision scheduleDecision;
        private Long overrideAssigneeId;
        private Long overridePlannedStart;
        private Long overridePlannedEnd;
        @Valid
        private List<AllocationRequest> overrideAllocationChunks = List.of();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AllocationRequest {
        @NotNull
        private Long assigneeId;
        @NotNull
        private Long start;
        @NotNull
        private Long end;
        @NotNull
        private Long effortMillis;

        public BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride toCommand() {
            return new BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride(
                    assigneeId,
                    start,
                    end,
                    effortMillis
            );
        }
    }
}
