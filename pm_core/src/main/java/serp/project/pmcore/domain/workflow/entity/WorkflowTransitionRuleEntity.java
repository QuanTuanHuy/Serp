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
import serp.project.pmcore.domain.shared.enums.TransitionRuleStage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowTransitionRuleEntity extends BaseEntity {
    private Long tenantId;
    private Long transitionId;
    private TransitionRuleStage ruleStage;
    private String ruleKey;
    private String configJson;
    private Integer sequence;
    private Boolean isEnabled;
}
