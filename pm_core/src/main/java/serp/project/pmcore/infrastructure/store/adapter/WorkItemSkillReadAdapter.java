/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;
import serp.project.pmcore.domain.skill.port.read.IWorkItemSkillReadPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemSkillMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemSkillRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkItemSkillReadAdapter implements IWorkItemSkillReadPort {
    private final IWorkItemSkillRepository workItemSkillRepository;
    private final WorkItemSkillMapper workItemSkillMapper;

    @Override
    public List<WorkItemSkillEntity> listActive(Long tenantId, Long projectId, Long workItemId) {
        return workItemSkillMapper.toEntities(workItemSkillRepository.findAllByTenantIdAndProjectIdAndWorkItemId(
                tenantId,
                projectId,
                workItemId
        ));
    }

    @Override
    public List<WorkItemSkillEntity> listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds) {
        if (workItemIds == null || workItemIds.isEmpty()) {
            return List.of();
        }
        return workItemSkillMapper.toEntities(workItemSkillRepository.findAllByTenantIdAndWorkItemIdIn(tenantId, workItemIds));
    }
}
