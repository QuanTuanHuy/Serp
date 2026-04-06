package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.application.dto.response.TripHistoryResponse;
import serp.project.school_bus_service.core.service.ITripHistoryService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.repository.TripHistoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripHistoryServiceImpl implements ITripHistoryService {

    private final TripHistoryRepository tripHistoryRepository;
    private final SchoolBusMapper mapper;

    @Override
    public List<TripHistoryResponse> getTripHistory(Long tenantId) {
        return tripHistoryRepository.findByTenantIdAndIsDeletedFalseOrderByServiceDateDescCreatedAtDesc(tenantId).stream()
                .map(mapper::toTripHistoryResponse)
                .toList();
    }
}
