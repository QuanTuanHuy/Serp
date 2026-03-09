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

    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "is_initial", nullable = false)
    private Boolean isInitial;

    @Column(name = "is_final", nullable = false)
    private Boolean isFinal;
}
