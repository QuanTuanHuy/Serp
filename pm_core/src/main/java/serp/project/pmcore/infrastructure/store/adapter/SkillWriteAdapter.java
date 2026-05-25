/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.SkillEntity;
import serp.project.pmcore.domain.skill.port.write.ISkillWritePort;
import serp.project.pmcore.infrastructure.store.mapper.SkillMapper;
import serp.project.pmcore.infrastructure.store.repository.ISkillRepository;

@Component
@RequiredArgsConstructor
public class SkillWriteAdapter implements ISkillWritePort {
    private final ISkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public SkillEntity save(SkillEntity skill) {
        return skillMapper.toEntity(skillRepository.save(skillMapper.toModel(skill)));
    }
}
