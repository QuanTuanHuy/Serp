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

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ParentTaskEntity extends BaseEntity {
    private Long groupTaskId;
    private String groupTaskName;
    private Long projectId;
    private String projectName;
    private Long schedulePlanId;
    private String schedulePlanName;
    private Long userId;

    private List<TaskEntity> tasks;
}
