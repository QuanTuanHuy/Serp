/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.GroupRoleEntity;
import serp.project.account.core.port.store.IGroupRolePort;
import serp.project.account.infrastructure.store.mapper.GroupRoleMapper;
import serp.project.account.infrastructure.store.model.GroupRoleModel;
import serp.project.account.infrastructure.store.repository.IGroupRoleRepository;

@Component
@RequiredArgsConstructor
public class GroupRoleAdapter implements IGroupRolePort {
    private final IGroupRoleRepository groupRoleRepository;
    private final GroupRoleMapper groupRoleMapper;
    
    @Override
    public void saveAll(List<GroupRoleEntity> groupRoles) {
        List<GroupRoleModel> models = groupRoleMapper.toModelList(groupRoles);
        groupRoleRepository.saveAll(models);
    }

    @Override
    public List<GroupRoleEntity> getByGroupId(Long groupId) {
        return groupRoleMapper.toEntityList(groupRoleRepository.findByGroupId(groupId));
    }
}
