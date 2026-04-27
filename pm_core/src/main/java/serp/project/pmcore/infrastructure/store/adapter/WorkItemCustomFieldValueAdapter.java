/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.workitem.port.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemCustomFieldValueMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemCustomFieldValueRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkItemCustomFieldValueAdapter implements IWorkItemCustomFieldValuePort {

    private final IWorkItemCustomFieldValueRepository workItemCustomFieldValueRepository;
    private final WorkItemCustomFieldValueMapper workItemCustomFieldValueMapper;

    @Override
    public List<WorkItemCustomFieldValueEntity> getActiveValuesByWorkItemId(Long workItemId, Long tenantId) {
        return workItemCustomFieldValueMapper.toEntities(
                workItemCustomFieldValueRepository
                        .findByWorkItemIdAndTenantIdOrderByCustomFieldIdAscSortOrderAscIdAsc(workItemId, tenantId)
        );
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> saveAll(List<WorkItemCustomFieldValueEntity> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return workItemCustomFieldValueMapper.toEntities(
                workItemCustomFieldValueRepository.saveAll(workItemCustomFieldValueMapper.toModels(values))
        );
    }

    @Override
    public void softDeleteByWorkItemIdAndCustomFieldIds(Long workItemId,
                                                        Collection<Long> customFieldIds,
                                                        Long updatedBy,
                                                        Long deletedAt) {
        if (customFieldIds == null || customFieldIds.isEmpty()) {
            return;
        }
        int affectedRows = workItemCustomFieldValueRepository.softDeleteByWorkItemIdAndCustomFieldIds(
                workItemId, customFieldIds, updatedBy, deletedAt
        );
        log.debug("Soft deleted {} work item custom field values for workItemId={}, customFieldIds={}",
                affectedRows, workItemId, customFieldIds);
    }
}
