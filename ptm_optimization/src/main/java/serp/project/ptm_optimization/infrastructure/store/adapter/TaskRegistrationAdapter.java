/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.adapter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.ptm_optimization.core.domain.entity.TaskRegistrationEntity;
import serp.project.ptm_optimization.core.port.store.ITaskRegistrationPort;
import serp.project.ptm_optimization.infrastructure.store.mapper.TaskRegistrationMapper;
import serp.project.ptm_optimization.infrastructure.store.model.TaskRegistrationModel;
import serp.project.ptm_optimization.infrastructure.store.repository.ITaskRegistrationRepository;

@Component
@RequiredArgsConstructor
public class TaskRegistrationAdapter implements ITaskRegistrationPort {
    private final ITaskRegistrationRepository repository;
    private final TaskRegistrationMapper mapper;

    @Override
    public TaskRegistrationEntity save(TaskRegistrationEntity taskRegistration) {
        TaskRegistrationModel model = mapper.toModel(taskRegistration);
        return mapper.toEntity(repository.save(model));
    }

    @Override
    public TaskRegistrationEntity getByUserId(Long userId) {
        return repository.findByUserId(userId)
                .map(mapper::toEntity)
                .orElse(null);
    }

}
