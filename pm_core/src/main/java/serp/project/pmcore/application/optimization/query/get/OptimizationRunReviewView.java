/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.model.OptimizationRunSummary;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRunReviewView {
    private Long id;
    private Long tenantId;
    private Long projectId;
    private String scope;
    private String mode;
    private OptimizationRunStatus status;
    private Long planningStart;
    private Long planningEnd;
    private Boolean allowReassignment;
    private Boolean allowScheduleChanges;
    private Integer selectedWorkItemCount;
    private OptimizationRunSummary summary;
    private Long createdAt;
    private Long createdBy;
    private Long updatedAt;
    private Long updatedBy;
    private List<OptimizationRunItemView> items;
    private List<OptimizationRunWarningView> warnings;
}
