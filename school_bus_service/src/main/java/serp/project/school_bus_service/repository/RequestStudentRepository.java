package serp.project.school_bus_service.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.RequestStudentEntity;
import serp.project.school_bus_service.enums.RequestStatus;

import java.time.LocalDate;
import java.util.List;

public interface RequestStudentRepository extends BaseRepository<RequestStudentEntity, Long> {
    List<RequestStudentEntity> findByRequestIdAndTenantIdAndIsDeletedFalse(Long requestId, Long tenantId);

    List<RequestStudentEntity> findByStudentIdAndTenantIdAndIsDeletedFalse(Long studentId, Long tenantId);

    @Query("""
            select entity
              from RequestStudentEntity entity
              join fetch entity.request request
              join fetch entity.student student
              left join fetch entity.pickupPoint pickupPoint
             where entity.tenantId = :tenantId
               and entity.isDeleted = false
               and request.tenantId = :tenantId
               and request.isDeleted = false
               and request.school.id = :schoolId
               and request.status = :status
               and :serviceDate >= request.effectiveFrom
               and (request.effectiveTo is null or :serviceDate <= request.effectiveTo)
             order by entity.createdAt desc, entity.id desc
            """)
    List<RequestStudentEntity> findApprovedManifestBySchoolAndServiceDate(
            @Param("schoolId") Long schoolId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("tenantId") Long tenantId,
            @Param("status") RequestStatus status);

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
