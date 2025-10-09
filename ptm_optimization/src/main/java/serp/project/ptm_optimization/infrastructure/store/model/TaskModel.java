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
@Table(name = "tasks", indexes = {
        @Index(name = "idx_original_id", columnList = "original_id"),
        @Index(name = "idx_schedule_task_id", columnList = "schedule_task_id"),
        @Index(name = "idx_parent_task_id", columnList = "parent_task_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TaskModel extends BaseModel {
    @Column(name = "title")
    private String title;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "status")
    private String status;

    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "end_date")
    private Long endDate;

    @Column(name = "active_status")
    private String activeStatus;

    @Column(name = "original_id")
    private Long originalId;

    @Column(name = "schedule_task_id")
    private Long scheduleTaskId;

    @Column(name = "task_order")
    private Integer taskOrder;

    @Column(name = "effort")
    private Double effort;

    @Column(name = "enjoyability")
    private Double enjoyability;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "stop_time")
    private Double stopTime;

    @Column(name = "task_batch")
    private Integer taskBatch;

    @Column(name = "parent_task_id")
    private Long parentTaskId;
}
