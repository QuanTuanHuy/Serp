/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.repository;

import org.springframework.stereotype.Repository;
import serp.project.ptm_optimization.infrastructure.store.model.ParentTaskModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IParentTaskRepository extends IBaseRepository<ParentTaskModel> {
    Optional<ParentTaskModel> findByGroupTaskId(Long groupTaskId);

    List<ParentTaskModel> findByProjectId(Long projectId);

    List<ParentTaskModel> findBySchedulePlanId(Long schedulePlanId);

    List<ParentTaskModel> findByUserId(Long userId);
}
