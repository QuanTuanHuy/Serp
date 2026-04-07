package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

}
