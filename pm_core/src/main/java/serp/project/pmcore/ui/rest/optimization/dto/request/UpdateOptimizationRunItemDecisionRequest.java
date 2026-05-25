/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.optimization.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;

@Getter
@Setter
@NoArgsConstructor
public class UpdateOptimizationRunItemDecisionRequest {
    private OptimizationDecision assignmentDecision;
    private OptimizationDecision scheduleDecision;
    private Long overrideAssigneeId;
    private Long overridePlannedStart;
    private Long overridePlannedEnd;
}
