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
@Table(name = "optimization_run_warnings")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OptimizationRunWarningModel extends BaseModel {
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "run_id", nullable = false)
    private Long runId;
    @Column(name = "work_item_id")
    private Long workItemId;
    @Column(name = "severity", nullable = false, length = 50)
    private String severity;
    @Column(name = "code", nullable = false, length = 100)
    private String code;
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;
}
