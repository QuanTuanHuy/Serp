/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.UserRoleEntity;
import serp.project.account.core.port.store.IUserRolePort;
import serp.project.account.infrastructure.store.mapper.UserRoleMapper;
import serp.project.account.infrastructure.store.model.UserRoleModel;
import serp.project.account.infrastructure.store.repository.IUserRoleRepository;

@Component
@RequiredArgsConstructor
public class UserRoleAdapter implements IUserRolePort {
    private final IUserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;
    
    @Override
    public void saveAll(List<UserRoleEntity> userRoles) {
        List<UserRoleModel> models = userRoleMapper.toModelList(userRoles);
        userRoleMapper.toEntityList(userRoleRepository.saveAll(models));
    }

    @Override
    public List<UserRoleEntity> getUserRolesByUserId(Long userId) {
        return userRoleMapper.toEntityList(userRoleRepository.findByUserId(userId));
    }

    @Override
    public List<UserRoleEntity> getUserRolesByUserIds(List<Long> userIds) {
        return userRoleMapper.toEntityList(userRoleRepository.findByUserIdIn(userIds));
    }
}
