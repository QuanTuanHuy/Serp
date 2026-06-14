package serp.project.school_bus_service.service;

import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import serp.project.school_bus_service.entity.SchoolBusUserRoleEntity;
import serp.project.school_bus_service.shared.base.IBaseService;

import java.util.Collection;
import java.util.List;

public interface ISchoolBusUserRoleService extends IBaseService<SchoolBusUserRoleEntity, Long> {

    void replaceRoles(SchoolBusUserEntity user, Collection<String> roleNames);

    List<Long> findActiveUserIdsByTenantAndRoleNames(Long tenantId, Collection<String> roleNames);
}
