/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.port.IUserSkillReadPort;
import serp.project.pmcore.infrastructure.store.mapper.UserSkillMapper;
import serp.project.pmcore.infrastructure.store.repository.IUserSkillRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserSkillReadAdapter implements IUserSkillReadPort {
    private final IUserSkillRepository userSkillRepository;
    private final UserSkillMapper userSkillMapper;

    @Override
    public List<UserSkillEntity> listActiveByUserIds(Long tenantId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userSkillMapper.toEntities(userSkillRepository.findAllByTenantIdAndUserIdIn(tenantId, userIds));
    }
}
