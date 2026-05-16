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
import org.hibernate.annotations.SQLRestriction;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_item_plans")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class WorkItemPlanModel extends BaseModel {
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "work_item_id", nullable = false)
    private Long workItemId;

    @Column(name = "planned_start", nullable = false)
    private LocalDateTime plannedStart;

    @Column(name = "planned_end", nullable = false)
    private LocalDateTime plannedEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private WorkItemPlanSource source;

    @Column(name = "source_run_id")
    private Long sourceRunId;

    @Column(name = "locked", nullable = false)
    private Boolean locked;
}
