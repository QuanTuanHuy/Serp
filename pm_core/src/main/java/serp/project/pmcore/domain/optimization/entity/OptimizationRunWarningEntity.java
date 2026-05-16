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
import serp.project.pmcore.domain.shared.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OptimizationRunWarningEntity extends BaseEntity {
    private Long tenantId;
    private Long runId;
    private Long workItemId;
    private String severity;
    private String code;
    private String message;
    private String detailsJson;
    private Long deletedAt;
}
