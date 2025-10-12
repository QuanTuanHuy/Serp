/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.UserModuleAccessEntity;
import serp.project.account.core.port.store.IUserModuleAccessPort;
import serp.project.account.infrastructure.store.mapper.UserModuleAccessMapper;
import serp.project.account.infrastructure.store.repository.IUserModuleAccessRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserModuleAccessAdapter implements IUserModuleAccessPort {
    private final IUserModuleAccessRepository userModuleAccessRepository;
    private final UserModuleAccessMapper userModuleAccessMapper;

    @Override
    public UserModuleAccessEntity save(UserModuleAccessEntity userModuleAccess) {
        var model = userModuleAccessMapper.toModel(userModuleAccess);
        return userModuleAccessMapper.toEntity(userModuleAccessRepository.save(model));
    }

    @Override
    public UserModuleAccessEntity getUserModuleAccess(Long userId, Long moduleId, Long organizationId) {
        return userModuleAccessRepository
                .findByUserIdAndModuleIdAndOrganizationId(userId, moduleId, organizationId)
                .map(userModuleAccessMapper::toEntity)
                .orElse(null);
    }

    @Override
    public List<UserModuleAccessEntity> getUserModuleAccessesByUserId(Long userId) {
        return userModuleAccessMapper.toEntityList(
                userModuleAccessRepository.findByUserId(userId));
    }

    @Override
    public List<UserModuleAccessEntity> getUserModuleAccessesByUserIdAndOrgId(Long userId, Long organizationId) {
        return userModuleAccessMapper.toEntityList(
                userModuleAccessRepository.findByUserIdAndOrganizationId(userId, organizationId));
    }

    @Override
    public boolean hasAccess(Long userId, Long moduleId, Long organizationId) {
        return userModuleAccessRepository
                .existsByUserIdAndModuleIdAndOrganizationIdAndIsActive(
                        userId, moduleId, organizationId, true);
    }

    @Override
    public void deleteUserModuleAccess(Long id) {
        userModuleAccessRepository.deleteById(id);
    }
}
