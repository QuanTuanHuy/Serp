package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolScheduleRepository extends BaseRepository<SchoolScheduleEntity, Long> {

    // --- Plain queries (no scheduleDays fetched) ---

    Page<SchoolScheduleEntity> findBySchoolIdAndTenantIdAndIsDeletedFalse(
            Long schoolId, Long tenantId, Pageable pageable);

    List<SchoolScheduleEntity> findBySchoolIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(
            Long schoolId, Long tenantId);

    Optional<SchoolScheduleEntity> findByIdAndTenantIdAndIsDeletedFalse(Long id, Long tenantId);

    // --- With scheduleDays eagerly fetched via EntityGraph ---

    @EntityGraph(attributePaths = "scheduleDays")
    @Query("SELECT s FROM SchoolScheduleEntity s WHERE s.school.id = :schoolId AND s.tenantId = :tenantId AND s.isDeleted = false")
    Page<SchoolScheduleEntity> findBySchoolIdWithDays(
            @Param("schoolId") Long schoolId, @Param("tenantId") Long tenantId, Pageable pageable);

    @EntityGraph(attributePaths = "scheduleDays")
    @Query("SELECT s FROM SchoolScheduleEntity s WHERE s.school.id = :schoolId AND s.tenantId = :tenantId AND s.isDeleted = false AND s.isActive = true")
    List<SchoolScheduleEntity> findActiveBySchoolIdWithDays(
            @Param("schoolId") Long schoolId, @Param("tenantId") Long tenantId);

    @EntityGraph(attributePaths = "scheduleDays")
    @Query("SELECT s FROM SchoolScheduleEntity s WHERE s.id = :id AND s.tenantId = :tenantId AND s.isDeleted = false")
    Optional<SchoolScheduleEntity> findByIdWithDays(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(s) > 0 FROM SchoolScheduleEntity s WHERE s.school.id = :schoolId AND s.tenantId = :tenantId AND s.isDeleted = false AND s.isActive = true AND s.isDefaultSchedule = true AND s.id <> :excludeId")
    boolean existsDefaultActiveBySchoolExcluding(@Param("schoolId") Long schoolId, @Param("tenantId") Long tenantId, @Param("excludeId") Long excludeId);
}
