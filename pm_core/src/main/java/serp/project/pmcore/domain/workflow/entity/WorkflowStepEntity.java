/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.entity;

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
public class WorkflowStepEntity extends BaseEntity {
    private Long tenantId;
    private Long workflowVersionId;
    private String stepKey;
    private String name;
    private Long statusId;
    private Integer stepOrder;
    private Boolean isInitial;
    private Boolean isTerminal;
}
