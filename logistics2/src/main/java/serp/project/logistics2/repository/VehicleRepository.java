package serp.project.logistics2.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import serp.project.logistics2.entity.VehicleEntity;

public interface VehicleRepository extends JpaRepository<VehicleEntity, String> {

    @Query("""
                SELECT v FROM VehicleEntity v
                WHERE (:query IS NULL OR v.licensePlate LIKE %:query%)
                  AND (:vehicleType IS NULL OR v.vehicleType = :vehicleType)
                  AND (:vehicleStatus IS NULL OR v.status = :vehicleStatus)
                  AND v.tenantId = :tenantId
            """)
    Page<VehicleEntity> search(
            @Param("query") String query,
            @Param("vehicleType") String vehicleType,
            @Param("vehicleStatus") String vehicleStatus,
            @Param("tenantId") Long tenantId,
            Pageable pageable);

    @Query("""
                SELECT v FROM VehicleEntity v
                WHERE (:query IS NULL OR v.licensePlate LIKE %:query%)
                  AND (:vehicleType IS NULL OR v.vehicleType = :vehicleType)
                  AND v.status = "IN_USE"
                  AND v.tenantId = :tenantId
                  AND NOT EXISTS (
                      SELECT 1 FROM VehicleShipperEntity vs
                      WHERE vs.vehicleId = v.id
                        AND vs.workingDate = :workingDate
                        AND vs.status IN ("ACTIVE", "INACTIVATE_REQUESTED", "ACTION_NEEDED")
                  )
            """)
    Page<VehicleEntity> searchForUsage(
            @Param("query") String query,
            @Param("vehicleType") String vehicleType,
            @Param("workingDate") LocalDate workingDate,
            @Param("tenantId") Long tenantId,
            Pageable pageable);

    Optional<VehicleEntity> findByIdAndTenantId(String id, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VehicleEntity v WHERE v.id = :id AND v.tenantId = :tenantId")
    Optional<VehicleEntity> findByIdAndTenantIdWithLock(@Param("id") String id, @Param("tenantId") Long tenantId);

    List<VehicleEntity> findByIdInAndTenantId(List<String> ids, Long tenantId);

}
