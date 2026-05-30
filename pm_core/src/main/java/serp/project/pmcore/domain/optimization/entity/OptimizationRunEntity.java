/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OptimizationRunEntity extends BaseEntity {
    private Long tenantId;
    private Long projectId;
    private String scope;
    private String objective;
    private String changeScope;
    private OptimizationRunStatus status;
    private Long planningStart;
    private Long planningEnd;
    private Integer selectedWorkItemCount;
    private String summaryJson;
    private String algorithmKey;
    private String algorithmVersion;
    private String solverStatus;
    private BigDecimal objectiveScore;
    private Long appliedAt;
    private Long appliedBy;
    private Long discardedAt;
    private Long deletedAt;
}
