package serp.project.logistics2.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import serp.project.logistics2.entity.VehicleShipperEntity;

public interface VehicleShipperRepository extends JpaRepository<VehicleShipperEntity, String> {

    @Query("SELECT v FROM VehicleShipperEntity v " +
            "WHERE (:shipperId IS NULL OR v.shipperId = :shipperId) " +
            "AND (:vehicleId IS NULL OR v.vehicleId = :vehicleId) " +
            "AND (CAST(:workingDate AS date) IS NULL OR v.workingDate = :workingDate) " +
            "AND v.tenantId = :tenantId")
    Page<VehicleShipperEntity> search(
            @Param("shipperId") Long shipperId,
            @Param("vehicleId") String vehicleId,
            @Param("workingDate") LocalDate workingDate,
            @Param("tenantId") Long tenantId,
            Pageable pageable);

    Optional<VehicleShipperEntity> findByIdAndTenantId(String id, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VehicleShipperEntity v " +
            "WHERE v.id = :id AND v.tenantId = :tenantId")
    Optional<VehicleShipperEntity> findByIdAndTenantIdWithLock(@Param("id") String id,
            @Param("tenantId") Long tenantId);

}
