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
@Table(name = "workflow_steps")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class WorkflowStepModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "workflow_version_id", nullable = false)
    private Long workflowVersionId;

    @Column(name = "step_key", nullable = false)
    private String stepKey;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @Column(name = "step_order")
    private Integer stepOrder;

    @Column(name = "is_initial", nullable = false)
    private Boolean isInitial;

    @Column(name = "is_terminal", nullable = false)
    private Boolean isTerminal;
}
