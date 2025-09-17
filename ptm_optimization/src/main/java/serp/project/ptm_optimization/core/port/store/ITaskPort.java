/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.port.store;

import java.util.List;

import serp.project.ptm_optimization.core.domain.entity.TaskEntity;

public interface ITaskPort {
    TaskEntity save(TaskEntity task);

    TaskEntity getTaskById(Long id);

    TaskEntity getTaskByOriginalId(Long originalId);

    List<TaskEntity> getTasksBySchedulePlanId(Long schedulePlanId);

    void deleteTask(Long id);
}
