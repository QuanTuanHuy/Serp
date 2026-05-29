package serp.project.first_mile.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.enums.TripStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripOrderRepository extends JpaRepository<TripOrder, Long> {

    List<TripOrder> findByTrip_IdOrderBySequenceNoAsc(Long tripId);

    List<TripOrder> findByTenantIdAndTrip_IdOrderBySequenceNoAsc(Long tenantId, Long tripId);

    long countByTenantIdAndTrip_Id(Long tenantId, Long tripId);

    @Query("""
            select to
            from TripOrder to
            where to.tenantId = :tenantId
                and to.trip.id in :tripIds
            order by to.trip.id asc, to.sequenceNo asc
            """)
    List<TripOrder> findByTenantIdAndTripIdInOrderByTripIdAscSequenceNoAsc(
            @Param("tenantId") Long tenantId,
            @Param("tripIds") Collection<Long> tripIds
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TripOrder> findFirstByTenantIdAndOrderIdAndTrip_CourierStaffIdAndTrip_StatusInOrderByTrip_IdDesc(
            Long tenantId,
            Long orderId,
            Long courierStaffId,
            Collection<TripStatus> statuses
    );

    void deleteByTrip_Id(Long tripId);

    @Modifying
    @Query("""
            delete from TripOrder to
            where to.tenantId = :tenantId
                and to.orderId in :orderIds
                and to.trip.status in :statuses
                and to.trip.id <> :targetTripId
            """)
    int deleteByTenantIdAndOrderIdInAndTripStatusInAndTripIdNot(
            @Param("tenantId") Long tenantId,
            @Param("orderIds") Collection<Long> orderIds,
            @Param("statuses") Collection<TripStatus> statuses,
            @Param("targetTripId") Long targetTripId
    );

    @Query("""
            select (count(to) > 0)
            from TripOrder to
            where to.tenantId = :tenantId
                and to.orderId = :orderId
                and to.trip.status in :statuses
                and (:excludeTripId is null or to.trip.id <> :excludeTripId)
            """)
    boolean existsByTenantIdAndOrderIdAndTripStatusIn(
            @Param("tenantId") Long tenantId,
            @Param("orderId") Long orderId,
            @Param("statuses") Collection<TripStatus> statuses,
            @Param("excludeTripId") Long excludeTripId
    );

    @Query("""
            select (count(to) > 0)
            from TripOrder to
            where to.tenantId = :tenantId
            and to.orderId = :orderId
            and to.trip.courierStaffId = :courierStaffId
            and to.trip.status in :statuses
            """)
    boolean existsByTenantIdAndOrderIdAndCourierStaffIdAndTripStatusIn(
            @Param("tenantId") Long tenantId,
            @Param("orderId") Long orderId,
            @Param("courierStaffId") Long courierStaffId,
            @Param("statuses") Collection<TripStatus> statuses
    );
}
