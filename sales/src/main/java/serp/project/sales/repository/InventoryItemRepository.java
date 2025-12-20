package serp.project.sales.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import serp.project.sales.entity.InventoryItemEntity;

public interface InventoryItemRepository
        extends JpaRepository<InventoryItemEntity, String>, JpaSpecificationExecutor<InventoryItemEntity> {

    @Query("SELECT i FROM InventoryItemEntity i " +
            "WHERE i.productId = :productId " +
            "AND (:timestamp IS NULL OR i.expiryDate IS NULL OR i.expiryDate > :timestamp) " +
            "AND (COALESCE(i.quantityOnHand, 0) - COALESCE(i.quantityReserved, 0) - COALESCE(i.quantityCommitted, 0)) > 0 "
            +
            "ORDER BY " +
            "  i.expiryDate ASC NULLS LAST, " +
            "  i.receivedDate ASC" +
            " FOR UPDATE")
    List<InventoryItemEntity> findAvailableInventoryItemByProductIdAndExpireAfter(
            @Param("productId") String productId,
            @Param("timestamp") LocalDate timestamp);

}
