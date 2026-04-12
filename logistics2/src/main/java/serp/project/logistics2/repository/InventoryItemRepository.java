package serp.project.logistics2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import serp.project.logistics2.entity.InventoryItemEntity;

public interface InventoryItemRepository
                extends JpaRepository<InventoryItemEntity, String>, JpaSpecificationExecutor<InventoryItemEntity> {
        List<InventoryItemEntity> findByIdInAndTenantId(List<String> ids, Long tenantId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT i FROM InventoryItemEntity i WHERE i.id IN :ids AND i.tenantId = :tenantId")
        List<InventoryItemEntity> findByIdInAndTenantIdWithLock(@Param("ids") List<String> ids,
                        @Param("tenantId") Long tenantId);
}
