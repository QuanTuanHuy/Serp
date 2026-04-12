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
import serp.project.logistics2.entity.DeliveryPlanEntity;

public interface DeliveryPlanRepository extends JpaRepository<DeliveryPlanEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM DeliveryPlanEntity p WHERE p.id = :id")
    Optional<DeliveryPlanEntity> findByIdWithLock(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM DeliveryPlanEntity p WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<DeliveryPlanEntity> findByIdAndTenantIdWithLock(@Param("id") String id,
            @Param("tenantId") Long tenantId);

    Optional<DeliveryPlanEntity> findByIdAndTenantId(String id, Long tenantId);

    @Query("SELECT p FROM DeliveryPlanEntity p " +
            "WHERE (:query IS NULL OR p.planCode LIKE %:query%) " +
            "AND (:facilityId IS NULL OR p.facilityId = :facilityId) " +
            "AND (:deliveryDate IS NULL OR p.deliveryDate = :deliveryDate) " +
            "AND (:optimizationStatus IS NULL OR p.optimizationStatus = :optimizationStatus) " +
            "AND p.tenantId = :tenantId")
    Page<DeliveryPlanEntity> search(
            @Param("query") String query,
            @Param("facilityId") String facilityId,
            @Param("deliveryDate") LocalDate deliveryDate,
            @Param("optimizationStatus") String optimizationStatus,
            @Param("tenantId") Long tenantId,
            Pageable pageable);

}
