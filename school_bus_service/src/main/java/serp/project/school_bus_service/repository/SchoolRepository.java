package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.SchoolEntity;

import java.util.Collection;
import java.util.List;

public interface SchoolRepository extends BaseRepository<SchoolEntity, Long> {
    List<SchoolEntity> findByTenantIdAndIsDeletedFalseOrderByNameAsc(Long tenantId);

    List<SchoolEntity> findByTenantIdAndIdInAndIsActiveTrueAndIsDeletedFalseOrderByNameAsc(
            Long tenantId,
            Collection<Long> ids);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    @Query("""
            SELECT DISTINCT s FROM SchoolEntity s
            WHERE s.tenantId = :tenantId
              AND s.isDeleted = false
              AND s.isActive = true
              AND EXISTS (
                  SELECT st FROM StudentEntity st
                  WHERE st.school.id = s.id
                    AND st.parentProfile.id = :parentProfileId
                    AND st.tenantId = :tenantId
                    AND st.isDeleted = false
                    AND st.isActive = true
              )
            ORDER BY s.name ASC
            """)
    List<SchoolEntity> findDashboardSchoolsForParent(
            @Param("tenantId") Long tenantId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
            SELECT DISTINCT s FROM SchoolEntity s
            WHERE s.tenantId = :tenantId
              AND s.isDeleted = false
              AND s.isActive = true
              AND (
                  EXISTS (
                      SELECT t FROM TripExecutionEntity t
                      WHERE t.route.planningSession.school.id = s.id
                        AND EXISTS (
                            SELECT assignment FROM RouteAssignmentEntity assignment
                            WHERE assignment.route.id = t.route.id
                              AND assignment.driver.id = :driverProfileId
                              AND assignment.tenantId = :tenantId
                              AND assignment.isDeleted = false
                              AND assignment.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                                        serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
                        )
                        AND t.tenantId = :tenantId
                        AND t.isDeleted = false
                        AND t.route.isDeleted = false
                  )
                  OR EXISTS (
                      SELECT a FROM RouteAssignmentEntity a
                      WHERE a.route.planningSession.school.id = s.id
                        AND a.driver.id = :driverProfileId
                        AND a.tenantId = :tenantId
                        AND a.isDeleted = false
                        AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                         serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
                        AND a.route.isDeleted = false
                  )
              )
            ORDER BY s.name ASC
            """)
    List<SchoolEntity> findDashboardSchoolsForDriver(
            @Param("tenantId") Long tenantId,
            @Param("driverProfileId") Long driverProfileId);

    @Query("""
            SELECT DISTINCT s FROM SchoolEntity s
            WHERE s.tenantId = :tenantId
              AND s.isDeleted = false
              AND s.isActive = true
              AND (
                  EXISTS (
                      SELECT t FROM TripExecutionEntity t
                      WHERE t.route.planningSession.school.id = s.id
                        AND EXISTS (
                            SELECT assignment FROM RouteAssignmentEntity assignment
                            WHERE assignment.route.id = t.route.id
                              AND assignment.attendant.id = :attendantProfileId
                              AND assignment.tenantId = :tenantId
                              AND assignment.isDeleted = false
                              AND assignment.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                                        serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
                        )
                        AND t.tenantId = :tenantId
                        AND t.isDeleted = false
                        AND t.route.isDeleted = false
                  )
                  OR EXISTS (
                      SELECT a FROM RouteAssignmentEntity a
                      WHERE a.route.planningSession.school.id = s.id
                        AND a.attendant.id = :attendantProfileId
                        AND a.tenantId = :tenantId
                        AND a.isDeleted = false
                        AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                         serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
                        AND a.route.isDeleted = false
                  )
              )
            ORDER BY s.name ASC
            """)
    List<SchoolEntity> findDashboardSchoolsForAttendant(
            @Param("tenantId") Long tenantId,
            @Param("attendantProfileId") Long attendantProfileId);
}
