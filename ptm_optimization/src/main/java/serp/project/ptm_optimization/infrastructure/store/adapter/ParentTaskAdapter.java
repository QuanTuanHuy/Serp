/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.adapter;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import serp.project.ptm_optimization.core.domain.entity.ParentTaskEntity;
import serp.project.ptm_optimization.core.port.store.IParentTaskPort;
import serp.project.ptm_optimization.infrastructure.store.mapper.ParentTaskMapper;
import serp.project.ptm_optimization.infrastructure.store.model.ParentTaskModel;
import serp.project.ptm_optimization.infrastructure.store.repository.IParentTaskRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ParentTaskAdapter implements IParentTaskPort {
    private final IParentTaskRepository repository;
    private final ParentTaskMapper mapper;

    @Override
    public ParentTaskEntity save(ParentTaskEntity parentTask) {
        ParentTaskModel model = mapper.toModel(parentTask);
        return mapper.toEntity(repository.save(model));
    }

    @Override
    public ParentTaskEntity getByGroupTaskId(Long groupTaskId) {
        return repository.findByGroupTaskId(groupTaskId)
                .map(mapper::toEntity)
                .orElse(null);
    }

    @Override
    public ParentTaskEntity getById(Long id) {
        return repository.findById(id)
                .map(mapper::toEntity)
                .orElse(null);
    }

    @Override
    public List<ParentTaskEntity> getByProjectId(Long projectId) {
        return repository.findByProjectId(projectId).stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParentTaskEntity> getByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
    }
}
