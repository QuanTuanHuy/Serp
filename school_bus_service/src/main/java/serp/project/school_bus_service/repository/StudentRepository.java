package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.dto.response.StudentResponse;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.StudentEntity;
import serp.project.school_bus_service.repository.projection.StudentSummaryProjection;

import java.util.List;

public interface StudentRepository extends BaseRepository<StudentEntity, Long> {
    List<StudentEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    List<StudentEntity> findByTenantIdAndParentProfileIdAndIsDeletedFalse(Long tenantId, Long parentProfileId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    @Query(value = """
            SELECT
                COUNT(s.id) AS totalStudents,
                COUNT(DISTINCT s.school_id) AS linkedSchools,
                COUNT(DISTINCT s.parent_profile_id) AS linkedParents,
                COALESCE(SUM(CASE WHEN s.is_active = true THEN 1 ELSE 0 END), 0) AS activeStudents
              FROM public.school_bus_student s
             WHERE s.tenant_id = :tenantId
               AND s.is_deleted = false
               AND (CAST(:parentProfileId AS bigint) IS NULL OR s.parent_profile_id = :parentProfileId)
            """, nativeQuery = true)
    StudentSummaryProjection getStudentSummary(
            @Param("tenantId") Long tenantId,
            @Param("parentProfileId") Long parentProfileId);

    @Query(value = """
            SELECT new serp.project.school_bus_service.dto.response.StudentResponse(
                s.id, s.tenantId, s.isActive, s.isDeleted, s.createdAt, s.createdBy, s.updatedAt, s.updatedBy,
                school.id, school.name,
                parent.id, parent.fullName,
                pickup.id, pickup.name,
                dropoff.id, dropoff.name,
                s.fullName, s.studentCode, s.grade, s.className, s.homeAddress, s.dateOfBirth, s.gender, s.specialNote
            )
            FROM StudentEntity s
            JOIN s.school school
            JOIN s.parentProfile parent
            LEFT JOIN s.pickupPoint pickup
            LEFT JOIN s.defaultDropoffPoint dropoff
            WHERE s.tenantId = :tenantId
              AND s.isDeleted = false
              AND (:parentProfileId IS NULL OR parent.id = :parentProfileId)
              AND (:schoolId IS NULL OR school.id = :schoolId)
              AND (
                  :keywordPattern IS NULL
                  OR LOWER(s.fullName) LIKE :keywordPattern
                  OR LOWER(s.studentCode) LIKE :keywordPattern
                  OR LOWER(s.grade) LIKE :keywordPattern
                  OR LOWER(s.className) LIKE :keywordPattern
                  OR LOWER(s.homeAddress) LIKE :keywordPattern
                  OR LOWER(school.name) LIKE :keywordPattern
                  OR LOWER(parent.fullName) LIKE :keywordPattern
                  OR LOWER(pickup.name) LIKE :keywordPattern
              )
            """,
            countQuery = """
            SELECT COUNT(s)
            FROM StudentEntity s
            JOIN s.school school
            JOIN s.parentProfile parent
            LEFT JOIN s.pickupPoint pickup
            WHERE s.tenantId = :tenantId
              AND s.isDeleted = false
              AND (:parentProfileId IS NULL OR parent.id = :parentProfileId)
              AND (:schoolId IS NULL OR school.id = :schoolId)
              AND (
                  :keywordPattern IS NULL
                  OR LOWER(s.fullName) LIKE :keywordPattern
                  OR LOWER(s.studentCode) LIKE :keywordPattern
                  OR LOWER(s.grade) LIKE :keywordPattern
                  OR LOWER(s.className) LIKE :keywordPattern
                  OR LOWER(s.homeAddress) LIKE :keywordPattern
                  OR LOWER(school.name) LIKE :keywordPattern
                  OR LOWER(parent.fullName) LIKE :keywordPattern
                  OR LOWER(pickup.name) LIKE :keywordPattern
              )
            """)
    Page<StudentResponse> findStudentListItems(
            @Param("tenantId") Long tenantId,
            @Param("parentProfileId") Long parentProfileId,
            @Param("schoolId") Long schoolId,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable);

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
