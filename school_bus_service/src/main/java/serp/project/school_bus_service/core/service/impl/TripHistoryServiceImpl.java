package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.application.dto.params.TripHistoryParamsRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.TripHistoryResponse;
import serp.project.school_bus_service.core.service.ITripHistoryService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.TripHistoryEntity;
import serp.project.school_bus_service.infrastructure.store.repository.TripHistoryRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TripHistoryServiceImpl implements ITripHistoryService {

    private final TripHistoryRepository tripHistoryRepository;
    private final SchoolBusMapper mapper;

    @Override
    public PageResponse<TripHistoryResponse> getTripHistory(TripHistoryParamsRequest params, Long tenantId) {
        return PageResponse.from(tripHistoryRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "routeCode", "status",
                        "bus.plateNumber", "driver.fullName", "attendant.fullName"),
                pageable(params, Set.of("id", "routeCode", "serviceDate", "status", "createdAt", "updatedAt"),
                        "serviceDate")),
                mapper::toTripHistoryResponse);
    }

    private Specification<TripHistoryEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(
            serp.project.school_bus_service.application.dto.request.BaseParamsRequest params,
            Set<String> allowedSorts,
            String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }
}
