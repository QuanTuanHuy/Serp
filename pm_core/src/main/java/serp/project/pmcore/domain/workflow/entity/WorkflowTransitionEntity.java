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

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowTransitionEntity extends BaseEntity {
    private Long tenantId;
    private Long workflowVersionId;
    private String name;
    private Long fromStepId;
    private Long toStepId;
    private Long screenId;
    private Integer sequence;
    private Long deletedAt;

    private List<WorkflowTransitionRuleEntity> rules;
}
