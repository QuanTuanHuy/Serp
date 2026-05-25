/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.entity.WorkItemHistoryEntity;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemHistoryWritePort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemHistoryMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemHistoryRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkItemHistoryPersistenceAdapter implements IWorkItemHistoryWritePort {

    private final IWorkItemHistoryRepository repository;
    private final WorkItemHistoryMapper mapper;

    @Override
    public List<WorkItemHistoryEntity> saveAll(List<WorkItemHistoryEntity> histories) {
        if (histories == null || histories.isEmpty()) {
            return List.of();
        }
        return repository.saveAll(histories.stream().map(mapper::toModel).toList())
                .stream()
                .map(mapper::toEntity)
                .toList();
    }
}
