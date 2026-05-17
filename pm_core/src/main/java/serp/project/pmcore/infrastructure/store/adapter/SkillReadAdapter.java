/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.SkillEntity;
import serp.project.pmcore.domain.skill.port.ISkillReadPort;
import serp.project.pmcore.infrastructure.store.mapper.SkillMapper;
import serp.project.pmcore.infrastructure.store.repository.ISkillRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SkillReadAdapter implements ISkillReadPort {
    private final ISkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public List<SkillEntity> listActiveByIds(Long tenantId, List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return List.of();
        }
        return skillMapper.toEntities(skillRepository.findAllByTenantIdAndIdInAndActiveTrue(tenantId, skillIds));
    }
}
