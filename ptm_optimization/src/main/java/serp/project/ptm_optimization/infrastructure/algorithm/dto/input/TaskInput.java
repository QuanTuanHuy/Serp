/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.algorithm.dto.input;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TaskInput {
    private Long taskId;
    private Integer durationMin;
    private Double priorityScore;
    private Long deadlineMs;
    private Long earliestStartMs;
    private Double effort;
    private Double enjoyability;
    private List<Long> dependentTaskIds;
}
