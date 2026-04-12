/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.enums.TransitionRuleStage;

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "workflow_transition_rules")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class WorkflowTransitionRuleModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "transition_id", nullable = false)
    private Long transitionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_stage", nullable = false)
    private TransitionRuleStage ruleStage;

    @Column(name = "rule_key", nullable = false)
    private String ruleKey;

    @Column(name = "config_json")
    private String configJson;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;
}
