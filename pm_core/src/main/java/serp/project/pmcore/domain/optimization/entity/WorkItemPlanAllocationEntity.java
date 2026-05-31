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
public class WorkItemPlanAllocationEntity extends BaseEntity {
    private Long tenantId;
    private Long projectId;
    private Long workItemPlanId;
    private Long workItemId;
    private Long assigneeId;
    private Long startTime;
    private Long endTime;
    private Long effortMillis;
    private WorkItemPlanSource source;
    private Long sourceRunId;
    private Long sourceRunItemId;
}
