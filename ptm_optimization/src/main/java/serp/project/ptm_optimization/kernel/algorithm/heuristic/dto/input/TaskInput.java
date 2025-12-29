/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input;

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
    private Long earliestStartMs; // Cannot start before this time
    private Double effort;
    private Double enjoyability;
    private List<Long> dependentTaskIds;
}
