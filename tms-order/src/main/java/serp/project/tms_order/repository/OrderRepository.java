/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.OrderType;
import serp.project.tms_order.repository.projection.OrderDashboardSliceProjection;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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

    List<Order> findByIdInAndTenantId(Collection<Long> ids, Long tenantId);

    @Query("""
            select o
            from Order o
            where o.tenantId = :tenantId
                and upper(o.orderCode) in :orderCodes
            """)
    List<Order> findByTenantIdAndUpperOrderCodeIn(
            @Param("tenantId") Long tenantId,
            @Param("orderCodes") Collection<String> orderCodes
    );

    @Query("""
        select o
        from Order o
        where o.tenantId = :tenantId
            and o.status in :statuses
            and o.senderLocation is not null
            and (
                :postOfficeCode is null
                or upper(o.originPostOfficeCode) = upper(:postOfficeCode)
            )
            and (
                o.pickupTimeStart is null
                or cast(:horizonEnd as java.time.LocalDateTime) is null
                or o.pickupTimeStart <= :horizonEnd
            )
        order by
            case
                when cast(:horizonStart as java.time.LocalDateTime) is not null 
                    and o.pickupTimeEnd is not null
                    and o.pickupTimeEnd < :horizonStart
                then 0
                else 1
            end asc,
            o.pickupTimeEnd asc,
            o.id asc
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

    @Query("""
            select o
            from Order o
            where o.tenantId = :tenantId
                and upper(o.destinationPostOfficeCode) = upper(:destinationPostOfficeCode)
                and o.status in :statuses
            order by o.id asc
            """)
    List<Order> findByDestinationPostOfficeAndStatuses(
            @Param("tenantId") Long tenantId,
            @Param("destinationPostOfficeCode") String destinationPostOfficeCode,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("""
            select
                o.id as id,
                o.orderCode as orderCode,
                o.customerOrderCode as customerOrderCode,
                o.status as status,
                o.originPostOfficeCode as originPostOfficeCode,
                o.destinationPostOfficeCode as destinationPostOfficeCode,
                o.createdAt as createdAt,
                o.updatedAt as updatedAt,
                o.pickupTimeEnd as pickupTimeEnd,
                o.totalShippingFee as totalShippingFee,
                o.baseShippingFee as baseShippingFee,
                o.codFee as codFee,
                o.extraFee as extraFee,
                o.codAmount as codAmount,
                o.paymentStatus as paymentStatus
            from Order o
            where o.tenantId = :tenantId
                and o.createdAt >= :fromDateTime
                and o.createdAt < :toDateTime
                and (:serviceType is null or o.orderType = :serviceType)
                and (
                    :postOfficeCodesEmpty = true
                    or upper(o.originPostOfficeCode) in :postOfficeCodes
                    or upper(o.destinationPostOfficeCode) in :postOfficeCodes
                )
            """)
    List<OrderDashboardSliceProjection> findDashboardSlices(
            @Param("tenantId") Long tenantId,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            @Param("postOfficeCodes") Collection<String> postOfficeCodes,
            @Param("postOfficeCodesEmpty") boolean postOfficeCodesEmpty,
            @Param("serviceType") OrderType serviceType
    );

    @Query("""
            select count(o)
            from Order o
            where o.tenantId = :tenantId
                and o.createdAt >= :fromDateTime
                and o.createdAt < :toDateTime
                and (:serviceType is null or o.orderType = :serviceType)
                and (
                    :postOfficeCodesEmpty = true
                    or upper(o.originPostOfficeCode) in :postOfficeCodes
                    or upper(o.destinationPostOfficeCode) in :postOfficeCodes
                )
            """)
    long countDashboardOrders(
            @Param("tenantId") Long tenantId,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            @Param("postOfficeCodes") Collection<String> postOfficeCodes,
            @Param("postOfficeCodesEmpty") boolean postOfficeCodesEmpty,
            @Param("serviceType") OrderType serviceType
    );
}
