/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TaskEntity extends BaseEntity {
    private String title;
    private Integer priority;
    private String status;
    private Long startDate;
    private Long endDate;
    private String activeStatus;
    private Long originalId;
    private Long scheduleTaskId;
    private Integer taskOrder;
    private Double effort;
    private Double enjoyability;
    private Double duration;
    private Double weight;
    private Double stopTime;
    private Integer taskBatch;

    private Long parentTaskId;
}
