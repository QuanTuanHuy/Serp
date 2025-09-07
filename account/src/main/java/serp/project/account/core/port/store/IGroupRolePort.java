package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.GroupRoleEntity;

import java.util.List;

public interface IGroupRolePort {
    void saveAll(List<GroupRoleEntity> groupRoles);
    List<GroupRoleEntity> getByGroupId(Long groupId);
}
