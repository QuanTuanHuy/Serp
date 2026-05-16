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
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkItemPlanEntity extends BaseEntity {
    private Long tenantId;
    private Long projectId;
    private Long workItemId;
    private Long plannedStart;
    private Long plannedEnd;
    private WorkItemPlanSource source;
    private Long sourceRunId;
    private Boolean locked;
    private Long deletedAt;
}
