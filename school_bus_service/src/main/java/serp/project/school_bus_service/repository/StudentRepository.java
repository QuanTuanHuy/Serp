package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.StudentEntity;

import java.util.List;

public interface StudentRepository extends BaseRepository<StudentEntity, Long> {
    List<StudentEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    List<StudentEntity> findByTenantIdAndParentProfileIdAndIsDeletedFalse(Long tenantId, Long parentProfileId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    @Query("""
            SELECT COUNT(s) FROM StudentEntity s
            WHERE s.tenantId = :tenantId
              AND s.isDeleted = false
              AND s.isActive = true
              AND (:schoolId IS NULL OR s.school.id = :schoolId)
              AND (:parentProfileId IS NULL OR s.parentProfile.id = :parentProfileId)
            """)
    long countDashboardStudents(
            @Param("tenantId") Long tenantId,
            @Param("schoolId") Long schoolId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
            SELECT COUNT(DISTINCT s.parentProfile.id) FROM StudentEntity s
            WHERE s.tenantId = :tenantId
              AND s.isDeleted = false
              AND s.isActive = true
              AND (:schoolId IS NULL OR s.school.id = :schoolId)
              AND (:parentProfileId IS NULL OR s.parentProfile.id = :parentProfileId)
            """)
    long countDashboardParents(
            @Param("tenantId") Long tenantId,
            @Param("schoolId") Long schoolId,
            @Param("parentProfileId") Long parentProfileId);
}
