/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.tms_order.domain.Order;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    boolean existsByCustomerOrderCodeIgnoreCaseAndTenantId(String customerOrderCode, Long tenantId);

    @Query("""
            select lower(o.customerOrderCode)
            from Order o
            where o.tenantId = :tenantId
                and lower(o.customerOrderCode) in :normalizedCodes
            """)
    Set<String> findExistingCustomerOrderCodes(
            @Param("tenantId") Long tenantId,
            @Param("normalizedCodes") Collection<String> normalizedCodes
    );

    boolean existsByCustomerOrderCodeIgnoreCaseAndTenantIdAndIdNot(
            String customerOrderCode,
            Long tenantId,
            Long id
    );

    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Order> findByOrderCodeAndTenantId(String orderCode, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from Order o
            where o.id = :id
                and o.tenantId = :tenantId
            """)
    Optional<Order> findByIdAndTenantIdForUpdate(
            @Param("id") Long id,
            @Param("tenantId") Long tenantId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from Order o
            where o.orderCode = :orderCode
                and o.tenantId = :tenantId
            """)
    Optional<Order> findByOrderCodeAndTenantIdForUpdate(
            @Param("orderCode") String orderCode,
            @Param("tenantId") Long tenantId
    );

    @Query("""
            select max(o.orderCode)
            from Order o
            where o.orderCode like concat(:prefix, '%')
            """)
    String findMaxOrderCodeByPrefix(@Param("prefix") String prefix);
}
