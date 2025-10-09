/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.service;

import serp.project.ptm_optimization.core.domain.entity.ParentTaskEntity;

public interface IParentTaskService {
    ParentTaskEntity save(ParentTaskEntity parentTask);

    ParentTaskEntity getParentTaskByGroupTaskId(Long groupTaskId);
}
