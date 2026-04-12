/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "workflow_transitions")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class WorkflowTransitionModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "workflow_version_id", nullable = false)
    private Long workflowVersionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "from_step_id")
    private Long fromStepId;

    @Column(name = "to_step_id", nullable = false)
    private Long toStepId;

    @Column(name = "screen_id")
    private Long screenId;

    @Column(name = "sequence")
    private Integer sequence;
}
