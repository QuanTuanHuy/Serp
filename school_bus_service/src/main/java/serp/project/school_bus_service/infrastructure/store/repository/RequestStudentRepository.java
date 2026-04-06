package serp.project.school_bus_service.infrastructure.store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.RequestStudentEntity;

import java.util.List;

public interface RequestStudentRepository extends BaseRepository<RequestStudentEntity, Long> {
    List<RequestStudentEntity> findByRequestIdAndTenantIdAndIsDeletedFalse(Long requestId, Long tenantId);

    List<RequestStudentEntity> findByStudentIdAndTenantIdAndIsDeletedFalse(Long studentId, Long tenantId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update RequestStudentEntity entity
               set entity.isDeleted = true,
                   entity.isActive = false,
                   entity.updatedBy = :updatedBy
             where entity.request.id = :requestId
               and entity.tenantId = :tenantId
               and entity.isDeleted = false
            """)
    int softDeleteByRequestId(@Param("requestId") Long requestId, @Param("tenantId") Long tenantId,
            @Param("updatedBy") String updatedBy);
}
