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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                userModuleAccessRepository.findByUserIdAndOrganizationIdAndIsActive(userId, organizationId, true));
    }

    @Override
    public List<UserModuleAccessEntity> getActiveUsersByModuleAndOrg(Long moduleId, Long organizationId) {
        return userModuleAccessMapper.toEntityList(
                userModuleAccessRepository.findByModuleIdAndOrganizationIdAndIsActive(moduleId, organizationId, true));
    }

    @Override
    public boolean hasAccess(Long userId, Long moduleId, Long organizationId) {
        return userModuleAccessRepository
                .existsByUserIdAndModuleIdAndOrganizationIdAndIsActive(
                        userId, moduleId, organizationId, true);
    }

    @Override
    public int countActiveUsers(Long moduleId, Long organizationId) {
        return userModuleAccessRepository.countActiveUsersByModuleAndOrganization(moduleId, organizationId);
    }

    @Override
    public Map<Long, Integer> countActiveModulesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        var results = userModuleAccessRepository.countActiveModulesByUserIds(userIds);
        var map = new HashMap<Long, Integer>();
        for (var row : results) {
            try {
                Long userId = Long.parseLong(row[0].toString());
                Integer count = Integer.parseInt(row[1].toString());
                map.put(userId, count);
            } catch (NumberFormatException e) {
            }
        }
        return map;
    }

    @Override
    public void deleteUserModuleAccess(Long id) {
        userModuleAccessRepository.deleteById(id);
    }
}
