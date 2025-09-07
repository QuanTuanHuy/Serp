package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.GroupEntity;

public interface IGroupPort {
    GroupEntity save(GroupEntity group);
    GroupEntity getGroupById(Long id);
    GroupEntity getGroupByName(String groupName);
    boolean existsByName(String groupName);
}
