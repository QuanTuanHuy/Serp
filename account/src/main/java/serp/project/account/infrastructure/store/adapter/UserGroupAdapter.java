/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.UserGroupEntity;
import serp.project.account.core.port.store.IUserGroupPort;
import serp.project.account.infrastructure.store.mapper.UserGroupMapper;
import serp.project.account.infrastructure.store.model.UserGroupModel;
import serp.project.account.infrastructure.store.repository.IUserGroupRepository;

@Component
@RequiredArgsConstructor
public class UserGroupAdapter implements IUserGroupPort {
    private final IUserGroupRepository userGroupRepository;
    private final UserGroupMapper userGroupMapper;
    
    @Override
    public void save(UserGroupEntity userGroup) {
        UserGroupModel userGroupModel = userGroupMapper.toModel(userGroup);
        userGroupRepository.save(userGroupModel);
    }

    @Override
    public List<UserGroupEntity> getByUserId(Long userId) {
        return userGroupMapper.toEntityList(userGroupRepository.findByUserId(userId));
    }
}
