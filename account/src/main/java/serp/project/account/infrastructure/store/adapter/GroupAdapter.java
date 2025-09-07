/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.GroupEntity;
import serp.project.account.core.port.store.IGroupPort;
import serp.project.account.infrastructure.store.mapper.GroupMapper;
import serp.project.account.infrastructure.store.model.GroupModel;
import serp.project.account.infrastructure.store.repository.IGroupRepository;

@Component
@RequiredArgsConstructor
public class GroupAdapter implements IGroupPort {
    private final IGroupRepository groupRepository;
    private final GroupMapper groupMapper;
    
    @Override
    public GroupEntity save(GroupEntity group) {
        GroupModel groupModel = groupMapper.toModel(group);
        return groupMapper.toEntity(groupRepository.save(groupModel));
    }

    @Override
    public GroupEntity getGroupById(Long id) {
        return groupRepository.findById(id)
                .map(groupMapper::toEntity)
                .orElse(null);
    }

    @Override
    public GroupEntity getGroupByName(String groupName) {
        return groupRepository.findByGroupName(groupName)
                .map(groupMapper::toEntity)
                .orElse(null);
    }

    @Override
    public boolean existsByName(String groupName) {
        return groupRepository.existsByGroupName(groupName);
    }
}
