package serp.project.sales.repository;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import serp.project.sales.entity.InventoryItemEntity;

public interface InventoryItemRepository
        extends JpaRepository<InventoryItemEntity, String>, JpaSpecificationExecutor<InventoryItemEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItemEntity i " +
            "WHERE i.productId = :productId " +
            "AND (cast(:timestamp as date) IS NULL OR i.expirationDate IS NULL OR i.expirationDate > :timestamp) " +
            "AND (COALESCE(i.quantityOnHand, 0) - COALESCE(i.quantityReserved, 0) - COALESCE(i.quantityCommitted, 0)) > 0 "
            +
            "ORDER BY " +
            "  i.expirationDate ASC NULLS LAST, " +
            "  i.receivedDate ASC")
    List<InventoryItemEntity> findAvailableInventoryItemByProductIdAndExpireAfter(
            @Param("productId") String productId,
            @Param("timestamp") LocalDate timestamp);

}
