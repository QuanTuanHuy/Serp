/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "parent_tasks", indexes = {
        @Index(name = "idx_schedule_plan_id", columnList = "schedule_plan_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ParentTaskModel extends BaseModel {
    @Column(name = "group_task_id")
    private Long groupTaskId;

    @Column(name = "group_task_name")
    private String groupTaskName;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "schedule_plan_id")
    private Long schedulePlanId;

    @Column(name = "schedule_plan_name")
    private String schedulePlanName;

    @Column(name = "user_id")
    private Long userId;
}
