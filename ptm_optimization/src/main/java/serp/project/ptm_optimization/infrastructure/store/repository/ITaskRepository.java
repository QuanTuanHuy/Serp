/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.repository;

import org.springframework.stereotype.Repository;
import serp.project.ptm_optimization.infrastructure.store.model.TaskModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITaskRepository extends IBaseRepository<TaskModel> {
    Optional<TaskModel> findByOriginalId(Long originalId);

    List<TaskModel> findByParentTaskId(Long parentTaskId);

    List<TaskModel> findByParentTaskIdIn(List<Long> parentTaskIds);
}
