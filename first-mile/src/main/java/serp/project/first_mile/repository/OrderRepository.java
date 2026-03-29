package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.Order;

import java.util.Collection;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
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

	@Query("""
			select max(o.orderCode)
			from Order o
			where o.orderCode like concat(:prefix, '%')
			""")
	String findMaxOrderCodeByPrefix(@Param("prefix") String prefix);

}
