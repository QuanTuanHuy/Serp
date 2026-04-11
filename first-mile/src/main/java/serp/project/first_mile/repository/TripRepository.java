package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.enums.PickupShift;
import serp.project.first_mile.enums.TripStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    Optional<Trip> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Trip> findFirstByTenantIdAndPostOfficeIdAndCourierStaffIdAndTripDateAndShiftAndStatusIn(
            Long tenantId,
            Long postOfficeId,
            Long courierStaffId,
            LocalDate tripDate,
            PickupShift shift,
            Collection<TripStatus> statuses
    );

    List<Trip> findByTenantIdAndPostOfficeIdAndTripDateAndShiftAndStatusIn(
            Long tenantId,
            Long postOfficeId,
            LocalDate tripDate,
            PickupShift shift,
            Collection<TripStatus> statuses
    );

    @Query("""
            select (count(t) > 0)
            from Trip t
            where t.tenantId = :tenantId
                and t.tripDate = :tripDate
                and t.shift = :shift
                and t.courierStaffId = :courierStaffId
                and t.status in :statuses
                and (:excludeTripId is null or t.id <> :excludeTripId)
            """)
    boolean existsActiveTripByCourierAndShift(
            @Param("tenantId") Long tenantId,
            @Param("tripDate") LocalDate tripDate,
            @Param("shift") PickupShift shift,
            @Param("courierStaffId") Long courierStaffId,
            @Param("statuses") Collection<TripStatus> statuses,
            @Param("excludeTripId") Long excludeTripId
    );

    @Query("""
            select (count(t) > 0)
            from Trip t
            where t.tenantId = :tenantId
                and t.tripDate = :tripDate
                and t.shift = :shift
                and t.vehicleId = :vehicleId
                and t.status in :statuses
                and (:excludeTripId is null or t.id <> :excludeTripId)
            """)
    boolean existsActiveTripByVehicleAndShift(
            @Param("tenantId") Long tenantId,
            @Param("tripDate") LocalDate tripDate,
            @Param("shift") PickupShift shift,
            @Param("vehicleId") Long vehicleId,
            @Param("statuses") Collection<TripStatus> statuses,
            @Param("excludeTripId") Long excludeTripId
    );
}
