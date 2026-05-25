package serp.project.school_bus_service.shared.base;

import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.entity.BaseModel;

import java.util.Collection;
import java.util.List;

public abstract class AbstractBaseService<T extends BaseModel, ID> implements IBaseService<T, ID> {

    protected abstract BaseRepository<T, ID> getRepository();

    protected String actor(Long actorId) {
        return actorId == null ? "SYSTEM" : String.valueOf(actorId);
    }

    @Override
    public T findById(ID id, Long tenantId) {
        return findById(getRepository(), id, tenantId);
    }

    @Override
    public List<T> findByIds(Collection<ID> ids, Long tenantId) {
        return findByIds(getRepository(), ids, tenantId);
    }

    @Override
    public void softDeleteById(ID id, Long tenantId, Long actorId) {
        softDeleteById(getRepository(), id, tenantId, actorId);
    }

    @Override
    public void softDeleteByIds(Collection<ID> ids, Long tenantId, Long actorId) {
        softDeleteByIds(getRepository(), ids, tenantId, actorId);
    }

    @Override
    public void activateByIds(Collection<ID> ids, Long tenantId, Long actorId) {
        activateByIds(getRepository(), ids, tenantId, actorId);
    }

    protected <M extends BaseModel> M findById(BaseRepository<M, ID> repository, ID id, Long tenantId) {
        return repository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
    }

    protected <M extends BaseModel> List<M> findByIds(BaseRepository<M, ID> repository, Collection<ID> ids, Long tenantId) {
        List<M> items = repository.findAllByIdInAndTenantIdAndIsDeletedFalse(ids, tenantId);
        if (items.size() != ids.size()) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        return items;
    }

    protected <M extends BaseModel> void softDeleteById(BaseRepository<M, ID> repository, ID id, Long tenantId, Long actorId) {
        if (repository.softDeleteById(id, tenantId, actor(actorId)) == 0) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
    }

    protected <M extends BaseModel> void softDeleteByIds(BaseRepository<M, ID> repository, Collection<ID> ids, Long tenantId,
            Long actorId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        repository.softDeleteByIds(ids, tenantId, actor(actorId));
    }

    protected <M extends BaseModel> void activateByIds(BaseRepository<M, ID> repository, Collection<ID> ids, Long tenantId,
            Long actorId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        repository.activateByIds(ids, tenantId, actor(actorId));
    }

    @Override
    public void validateTenantAccess(T entity, Long tenantId) {
        if (entity == null || !tenantId.equals(entity.getTenantId()) || Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
    }
}
