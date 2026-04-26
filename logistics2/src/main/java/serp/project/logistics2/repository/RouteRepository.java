package serp.project.logistics2.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import serp.project.logistics2.entity.RouteEntity;

public interface RouteRepository extends JpaRepository<RouteEntity, String> {

    boolean existsByStatusAndVehicleShipperId(String status, String vehicleShipperId);

    List<String> findIdsByVehicleShipperIdAndStatus(String vehicleShipperId, String status);

    @Query("SELECT r FROM RouteEntity r " +
            "WHERE (:deliveryPlanId IS NULL OR r.deliveryPlanId = :deliveryPlanId) " +
            "AND (:vehicleShipperId IS NULL OR r.vehicleShipperId = :vehicleShipperId) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:deliveryDate IS NULL OR r.deliveryDate = :deliveryDate) " +
            "AND r.tenantId = :tenantId")
    Page<RouteEntity> search(
            @Param("deliveryPlanId") String deliveryPlanId,
            @Param("vehicleShipperId") String vehicleShipperId,
            @Param("status") String status,
            @Param("deliveryDate") LocalDate deliveryDate,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT r FROM RouteEntity r " +
            "WHERE r.tenantId = :tenantId " +
            "AND (:deliverySlipId IS NULL OR EXISTS (" +
            "       SELECT 1 FROM RouteStopEntity rs " +
            "       WHERE rs.routeId = r.id AND rs.deliverySlipId = :deliverySlipId" +
            "))")
    Page<RouteEntity> search(
            @Param("deliverySlipId") String deliverySlipId,
            @Param("tenantId") String tenantId,
            Pageable pageable);
}
