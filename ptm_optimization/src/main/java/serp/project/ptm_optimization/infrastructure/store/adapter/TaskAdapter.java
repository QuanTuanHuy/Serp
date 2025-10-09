/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.adapter;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import serp.project.ptm_optimization.core.domain.entity.TaskEntity;
import serp.project.ptm_optimization.core.port.store.ITaskPort;
import serp.project.ptm_optimization.infrastructure.store.mapper.TaskMapper;
import serp.project.ptm_optimization.infrastructure.store.model.ParentTaskModel;
import serp.project.ptm_optimization.infrastructure.store.model.TaskModel;
import serp.project.ptm_optimization.infrastructure.store.repository.IParentTaskRepository;
import serp.project.ptm_optimization.infrastructure.store.repository.ITaskRepository;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskAdapter implements ITaskPort {
    private final ITaskRepository repository;
    private final IParentTaskRepository parentTaskRepository;
    private final TaskMapper mapper;

    @Override
    public TaskEntity save(TaskEntity task) {
        TaskModel model = mapper.toModel(task);
        return mapper.toEntity(repository.save(model));
    }

    @Override
    public TaskEntity getTaskById(Long id) {
        return repository.findById(id)
                .map(mapper::toEntity)
                .orElse(null);
    }

    @Override
    public TaskEntity getTaskByOriginalId(Long originalId) {
        return repository.findByOriginalId(originalId)
                .map(mapper::toEntity)
                .orElse(null);
    }

    @Override
    public List<TaskEntity> getTasksBySchedulePlanId(Long schedulePlanId) {
        var parentTasks = parentTaskRepository.findBySchedulePlanId(schedulePlanId);
        var parentTaskIds = parentTasks.stream().map(ParentTaskModel::getId).toList();
        if (parentTaskIds != null && !parentTaskIds.isEmpty()) {
            return mapper.toEntityList(repository.findByParentTaskIdIn(parentTaskIds));
        }
        return Collections.emptyList();
    }

    @Override
    public void deleteTask(Long id) {
        repository.deleteById(id);
    }
}
