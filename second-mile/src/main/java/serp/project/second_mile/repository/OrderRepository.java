/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/
package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderCodeAndTenantId(String orderCode, Long tenantId);

    Optional<Order> findByOrderCodeIgnoreCaseAndTenantId(String orderCode, Long tenantId);

    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);

    List<Order> findByTenantIdAndOrderCodeIn(Long tenantId, List<String> orderCodes);

    @Query("""
            select o
            from Order o
            where o.tenantId = :tenantId
                and upper(o.orderCode) in :orderCodes
            """)
    List<Order> findByTenantIdAndUpperOrderCodeIn(
            @Param("tenantId") Long tenantId,
            @Param("orderCodes") List<String> orderCodes
    );
}
