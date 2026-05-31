package serp.project.school_bus_service.shared.base;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.BaseModel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T extends BaseModel, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    @Query("select entity from #{#entityName} entity where entity.id = :id and entity.tenantId = :tenantId and entity.isDeleted = false")
    Optional<T> findByIdAndTenantIdAndIsDeletedFalse(@Param("id") ID id, @Param("tenantId") Long tenantId);

    @Query("select entity from #{#entityName} entity where entity.id in :ids and entity.tenantId = :tenantId and entity.isDeleted = false")
    List<T> findAllByIdInAndTenantIdAndIsDeletedFalse(@Param("ids") Collection<ID> ids, @Param("tenantId") Long tenantId);

    @Query("select count(entity) > 0 from #{#entityName} entity where entity.id = :id and entity.tenantId = :tenantId and entity.isDeleted = false")
    boolean existsByIdAndTenantIdAndIsDeletedFalse(@Param("id") ID id, @Param("tenantId") Long tenantId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update #{#entityName} entity
               set entity.isDeleted = true,
                   entity.isActive = false,
                   entity.updatedBy = :updatedBy
             where entity.id = :id
               and entity.tenantId = :tenantId
               and entity.isDeleted = false
            """)
    int softDeleteById(@Param("id") ID id, @Param("tenantId") Long tenantId, @Param("updatedBy") String updatedBy);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update #{#entityName} entity
               set entity.isDeleted = true,
                   entity.isActive = false,
                   entity.updatedBy = :updatedBy
             where entity.id in :ids
               and entity.tenantId = :tenantId
               and entity.isDeleted = false
            """)
    int softDeleteByIds(@Param("ids") Collection<ID> ids, @Param("tenantId") Long tenantId, @Param("updatedBy") String updatedBy);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update #{#entityName} entity
               set entity.isActive = true,
                   entity.updatedBy = :updatedBy
             where entity.id in :ids
               and entity.tenantId = :tenantId
               and entity.isDeleted = false
            """)
    int activateByIds(@Param("ids") Collection<ID> ids, @Param("tenantId") Long tenantId, @Param("updatedBy") String updatedBy);
}
