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
import serp.project.pmcore.infrastructure.store.model.WorkItemSkillModel;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemSkillRepository;

import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class WorkItemSkillWriteAdapter implements IWorkItemSkillWritePort {
    private final IWorkItemSkillRepository workItemSkillRepository;
    private final WorkItemSkillMapper workItemSkillMapper;

    @Override
    public void softDeleteActive(Long tenantId, Long projectId, Long workItemId, Long userId, Long now) {
        List<WorkItemSkillModel> models = workItemSkillRepository.findAllByTenantIdAndProjectIdAndWorkItemId(
                tenantId,
                projectId,
                workItemId
        );
        models.forEach(model -> {
            model.setDeletedAt(toLocalDateTime(now));
            model.setUpdatedAt(toLocalDateTime(now));
            model.setUpdatedBy(userId);
        });
        workItemSkillRepository.saveAll(models);
    }

    private LocalDateTime toLocalDateTime(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    @Override
    public List<WorkItemSkillEntity> saveAll(List<WorkItemSkillEntity> skills) {
        return workItemSkillMapper.toEntities(workItemSkillRepository.saveAll(
                skills.stream().map(workItemSkillMapper::toModel).toList()
        ));
    }
}
