/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.SkillEntity;
import serp.project.pmcore.domain.skill.port.read.ISkillReadPort;
import serp.project.pmcore.infrastructure.store.mapper.SkillMapper;
import serp.project.pmcore.infrastructure.store.repository.ISkillRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SkillReadAdapter implements ISkillReadPort {
    private final ISkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public Optional<SkillEntity> findActiveById(Long tenantId, Long skillId) {
        return skillRepository.findByTenantIdAndIdAndActiveTrue(tenantId, skillId).map(skillMapper::toEntity);
    }

    @Override
    public Optional<SkillEntity> findActiveByCode(Long tenantId, String code) {
        return skillRepository.findByTenantIdAndCodeIgnoreCaseAndActiveTrue(tenantId, code).map(skillMapper::toEntity);
    }

    @Override
    public List<SkillEntity> listActive(Long tenantId) {
        return skillMapper.toEntities(skillRepository.findAllByTenantIdAndActiveTrueOrderByCodeAsc(tenantId));
    }

    @Override
    public List<SkillEntity> listActiveByIds(Long tenantId, List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return List.of();
        }
        return skillMapper.toEntities(skillRepository.findAllByTenantIdAndIdInAndActiveTrue(tenantId, skillIds));
    }
}
