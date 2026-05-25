package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.repository.TripStudentRepository;
import serp.project.school_bus_service.service.ITripStudentService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TripStudentServiceImpl extends AbstractBaseService<TripStudentEntity, Long>
        implements ITripStudentService {

    private final TripStudentRepository tripStudentRepository;

    public TripStudentServiceImpl(TripStudentRepository tripStudentRepository) {
        this.tripStudentRepository = tripStudentRepository;
    }

    @Override
    protected BaseRepository<TripStudentEntity, Long> getRepository() {
        return tripStudentRepository;
    }

    @Override
    public List<TripStudentEntity> findByTrip(Long tripId, Long tenantId) {
        return tripStudentRepository.findByTripIdAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(tripId, tenantId);
    }

    @Override
    public Optional<TripStudentEntity> findByTripAndStudent(Long tripId, Long studentId, Long tenantId) {
        return tripStudentRepository.findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalse(tripId, studentId, tenantId);
    }

    @Override
    public TripStudentEntity save(TripStudentEntity entity) {
        return tripStudentRepository.save(entity);
    }
}
