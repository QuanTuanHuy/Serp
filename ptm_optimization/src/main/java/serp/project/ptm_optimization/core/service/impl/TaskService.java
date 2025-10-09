/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.ptm_optimization.core.domain.entity.TaskEntity;
import serp.project.ptm_optimization.core.port.store.ITaskPort;
import serp.project.ptm_optimization.core.service.ITaskService;

@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService {
    private final ITaskPort taskPort;

    @Override
    public TaskEntity save(TaskEntity task) {
        return taskPort.save(task);
    }
}
