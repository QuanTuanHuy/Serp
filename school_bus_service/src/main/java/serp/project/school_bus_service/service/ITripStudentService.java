package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.entity.TripStudentEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ITripStudentService extends IBaseService<TripStudentEntity, Long> {

    List<TripStudentEntity> findByTrip(Long tripId, Long tenantId);

    List<TripStudentEntity> findByTrips(Collection<Long> tripIds, Long tenantId);

    Optional<TripStudentEntity> findByTripAndStudent(Long tripId, Long studentId, Long tenantId);

    TripStudentEntity save(TripStudentEntity entity);
}
