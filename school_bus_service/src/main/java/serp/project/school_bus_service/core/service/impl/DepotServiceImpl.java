package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.DepotParamsRequest;
import serp.project.school_bus_service.application.dto.request.DepotUpsertRequest;
import serp.project.school_bus_service.application.dto.response.DepotResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IDepotService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.DepotEntity;
import serp.project.school_bus_service.infrastructure.store.repository.DepotRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DepotServiceImpl extends AbstractBaseService<DepotEntity, Long> implements IDepotService {

    private final DepotRepository depotRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;

    @Override
    protected BaseRepository<DepotEntity, Long> getRepository() {
        return depotRepository;
    }

    @Override
    public PageResponse<DepotResponse> getDepots(DepotParamsRequest params, Long tenantId) {
        return PageResponse.from(depotRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "name", "address", "contactPhone", "description"),
                PageableUtils.from(params,
                        Set.of("id", "name", "createdAt", "updatedAt"), "name")),
                mapper::toDepotResponse);
    }

    @Override
    public DepotResponse getDepotResponse(Long id, Long tenantId) {
        return mapper.toDepotResponse(getDepot(id, tenantId));
    }

    @Override
    public DepotEntity getDepot(Long id, Long tenantId) {
        return findById(depotRepository, id, tenantId);
    }

    @Override
    @Transactional
    public DepotResponse createDepot(DepotUpsertRequest request, Long tenantId, Long actorId) {
        DepotEntity depot = new DepotEntity();
        depot.markCreated(tenantId, actor(actorId));
        applyDepot(depot, request);
        DepotEntity saved = depotRepository.save(depot);
        auditLogService.log(tenantId, actorId, "Depot", saved.getId(), "CREATE", "Created depot");
        return mapper.toDepotResponse(saved);
    }

    @Override
    @Transactional
    public DepotResponse updateDepot(Long id, DepotUpsertRequest request, Long tenantId, Long actorId) {
        DepotEntity depot = getDepot(id, tenantId);
        depot.markUpdated(actor(actorId));
        applyDepot(depot, request);
        DepotEntity saved = depotRepository.save(depot);
        auditLogService.log(tenantId, actorId, "Depot", saved.getId(), "UPDATE", "Updated depot");
        return mapper.toDepotResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDepot(Long id, Long tenantId, Long actorId) {
        softDeleteById(depotRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "Depot", id, "SOFT_DELETE", "Soft deleted depot");
    }

    private void applyDepot(DepotEntity depot, DepotUpsertRequest request) {
        validateCoordinatePair(request.getLatitude(), request.getLongitude(), "depot");
        depot.setName(request.getName());
        depot.setAddress(request.getAddress());
        depot.setLatitude(request.getLatitude());
        depot.setLongitude(request.getLongitude());
        depot.setContactPhone(request.getContactPhone());
        depot.setDescription(request.getDescription());
        depot.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void validateCoordinatePair(Double latitude, Double longitude, String target) {
        if ((latitude == null) != (longitude == null)) {
            throw new AppException(
                    AppErrorCode.INVALID_REQUEST,
                    String.format("Both latitude and longitude are required when pinning a %s", target));
        }
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new AppException(
                    AppErrorCode.INVALID_REQUEST,
                    String.format("Latitude for %s must be between -90 and 90", target));
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new AppException(
                    AppErrorCode.INVALID_REQUEST,
                    String.format("Longitude for %s must be between -180 and 180", target));
        }
    }
}
