package serp.project.school_bus_service.shared.base;

import serp.project.school_bus_service.entity.BaseModel;

import java.util.Collection;
import java.util.List;

public interface IBaseService<T extends BaseModel, ID> {

    T findById(ID id, Long tenantId);

    List<T> findByIds(Collection<ID> ids, Long tenantId);

    void softDeleteById(ID id, Long tenantId, Long actorId);

    void softDeleteByIds(Collection<ID> ids, Long tenantId, Long actorId);

    void activateByIds(Collection<ID> ids, Long tenantId, Long actorId);

    void validateTenantAccess(T entity, Long tenantId);
}
