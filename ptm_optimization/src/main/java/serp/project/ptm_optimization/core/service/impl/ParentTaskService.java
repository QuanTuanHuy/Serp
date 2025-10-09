/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.ptm_optimization.core.domain.entity.ParentTaskEntity;
import serp.project.ptm_optimization.core.port.store.IParentTaskPort;
import serp.project.ptm_optimization.core.service.IParentTaskService;

@Service
@RequiredArgsConstructor
public class ParentTaskService implements IParentTaskService {
    private final IParentTaskPort parentTaskPort;

    @Override
    public ParentTaskEntity save(ParentTaskEntity parentTask) {
        return parentTaskPort.save(parentTask);
    }

    @Override
    public ParentTaskEntity getParentTaskByGroupTaskId(Long groupTaskId) {
        return parentTaskPort.getByGroupTaskId(groupTaskId);
    }

}
