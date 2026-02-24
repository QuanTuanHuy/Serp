/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowStepEntity extends BaseEntity {
    private Long tenantId;
    private Long workflowId;
    private Long statusId;
    private Integer sequence;
    private Boolean isInitial;
    private Boolean isFinal;
}
