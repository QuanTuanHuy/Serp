package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.enums.TripStatus;

import java.util.Collection;
import java.util.List;

@Repository
public interface TripOrderRepository extends JpaRepository<TripOrder, Long> {

    List<TripOrder> findByTrip_IdOrderBySequenceNoAsc(Long tripId);

    void deleteByTrip_Id(Long tripId);

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
