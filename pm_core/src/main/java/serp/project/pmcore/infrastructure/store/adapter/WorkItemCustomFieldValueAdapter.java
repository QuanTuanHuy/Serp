/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.workitem.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.port.store.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemCustomFieldValueMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemCustomFieldValueRepository;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkItemCustomFieldValueAdapter implements IWorkItemCustomFieldValuePort {

    private final IWorkItemCustomFieldValueRepository workItemCustomFieldValueRepository;
    private final WorkItemCustomFieldValueMapper workItemCustomFieldValueMapper;

    @Override
    public List<WorkItemCustomFieldValueEntity> saveAll(List<WorkItemCustomFieldValueEntity> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return workItemCustomFieldValueMapper.toEntities(
                workItemCustomFieldValueRepository.saveAll(workItemCustomFieldValueMapper.toModels(values))
        );
    }
}
