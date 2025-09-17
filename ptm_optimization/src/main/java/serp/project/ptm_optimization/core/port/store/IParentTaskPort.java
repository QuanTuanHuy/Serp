/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.port.store;

import java.util.List;

import serp.project.ptm_optimization.core.domain.entity.ParentTaskEntity;

public interface IParentTaskPort {
    ParentTaskEntity save(ParentTaskEntity parentTask);

    ParentTaskEntity getByGroupTaskId(Long groupTaskId);

    ParentTaskEntity getById(Long id);

    List<ParentTaskEntity> getByProjectId(Long projectId);

    List<ParentTaskEntity> getByUserId(Long userId);
}
