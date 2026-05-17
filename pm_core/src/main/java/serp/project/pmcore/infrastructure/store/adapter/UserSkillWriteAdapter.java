/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.port.write.IUserSkillWritePort;
import serp.project.pmcore.infrastructure.store.mapper.UserSkillMapper;
import serp.project.pmcore.infrastructure.store.model.UserSkillModel;
import serp.project.pmcore.infrastructure.store.repository.IUserSkillRepository;

import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class UserSkillWriteAdapter implements IUserSkillWritePort {
    private final IUserSkillRepository userSkillRepository;
    private final UserSkillMapper userSkillMapper;

    @Override
    public void softDeleteActive(Long tenantId, Long userId, Long updatedBy, Long now) {
        List<UserSkillModel> models = userSkillRepository.findAllByTenantIdAndUserId(tenantId, userId);
        models.forEach(model -> {
            model.setDeletedAt(toLocalDateTime(now));
            model.setUpdatedAt(toLocalDateTime(now));
            model.setUpdatedBy(updatedBy);
        });
        userSkillRepository.saveAll(models);
    }

    private LocalDateTime toLocalDateTime(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    @Override
    public List<UserSkillEntity> saveAll(List<UserSkillEntity> skills) {
        return userSkillMapper.toEntities(userSkillRepository.saveAll(
                skills.stream().map(userSkillMapper::toModel).toList()
        ));
    }
}
