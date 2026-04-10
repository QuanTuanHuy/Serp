package serp.project.first_mile.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

	@Query("""
			select o
			from Order o
			where o.tenantId = :tenantId
				and o.status in :statuses
				and o.senderLocation is not null
				and (
					:postOfficeCode is null
					or o.originPostOfficeCode = :postOfficeCode
				)
				and (
					:horizonStart is null
					or o.pickupTimeEnd is null
					or o.pickupTimeEnd >= :horizonStart
				)
				and (
					:horizonEnd is null
					or o.pickupTimeStart is null
					or o.pickupTimeStart <= :horizonEnd
				)
			order by o.pickupTimeEnd asc, o.id asc
			""")
	List<Order> findPickupCandidateOrders(
			@Param("tenantId") Long tenantId,
			@Param("statuses") Collection<OrderStatus> statuses,
			@Param("postOfficeCode") String postOfficeCode,
			@Param("horizonStart") LocalDateTime horizonStart,
			@Param("horizonEnd") LocalDateTime horizonEnd,
			Pageable pageable
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select o
			from Order o
			where o.tenantId = :tenantId
				and o.id in :orderIds
			""")
	List<Order> findByTenantIdAndIdInWithLock(
			@Param("tenantId") Long tenantId,
			@Param("orderIds") Collection<Long> orderIds
	);

		Optional<Order> findByIdAndTenantId(Long id, Long tenantId);

		List<Order> findByIdInAndTenantId(Collection<Long> ids, Long tenantId);

}
