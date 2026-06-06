/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;
import serp.project.pmcore.domain.skill.port.write.IWorkItemSkillWritePort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemSkillMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemSkillRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkItemSkillWriteAdapter implements IWorkItemSkillWritePort {
    private final IWorkItemSkillRepository workItemSkillRepository;
    private final WorkItemSkillMapper workItemSkillMapper;

    @Override
    public void deleteActive(Long tenantId, Long projectId, Long workItemId) {
        workItemSkillRepository.deleteAllByTenantIdAndProjectIdAndWorkItemId(
                tenantId,
                projectId,
                workItemId
        );
    }

    @Override
    public List<WorkItemSkillEntity> saveAll(List<WorkItemSkillEntity> skills) {
        return workItemSkillMapper.toEntities(workItemSkillRepository.saveAll(
                skills.stream().map(workItemSkillMapper::toModel).toList()
        ));
    }
}
