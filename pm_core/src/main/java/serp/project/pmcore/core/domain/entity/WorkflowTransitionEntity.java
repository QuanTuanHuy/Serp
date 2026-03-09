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

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowTransitionEntity extends BaseEntity {
    private Long tenantId;
    private Long workflowId;
    private String name;
    private Long fromStatusId;
    private Long toStatusId;
    private Integer sequence;

    private List<WorkflowTransitionRuleEntity> rules;
}
