package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.PickupPointParamsRequest;
import serp.project.school_bus_service.application.dto.request.PickupPointUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.PickupPointResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IPickupPointService;
import serp.project.school_bus_service.core.service.ISchoolService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.PickupPointEntity;
import serp.project.school_bus_service.infrastructure.store.repository.PickupPointRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PickupPointServiceImpl extends AbstractBaseService<PickupPointEntity, Long> implements IPickupPointService {

    private final PickupPointRepository pickupPointRepository;
    private final ISchoolService schoolService;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;

    @Override
    protected BaseRepository<PickupPointEntity, Long> getRepository() {
        return pickupPointRepository;
    }

    @Override
    public PageResponse<PickupPointResponse> getPickupPoints(PickupPointParamsRequest params, Long tenantId) {
        return PageResponse.from(pickupPointRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "name", "address", "school.name"),
                PageableUtils.from(params,
                        Set.of("id", "name", "createdAt", "updatedAt"), "name")),
                mapper::toPickupPointResponse);
    }

    @Override
    public PickupPointResponse getPickupPointResponse(Long id, Long tenantId) {
        return mapper.toPickupPointResponse(getPickupPoint(id, tenantId));
    }

    @Override
    public PickupPointEntity getPickupPoint(Long id, Long tenantId) {
        return findById(pickupPointRepository, id, tenantId);
    }

    @Override
    @Transactional
    public PickupPointResponse createPickupPoint(PickupPointUpsertRequest request, Long tenantId, Long actorId) {
        PickupPointEntity pickupPoint = new PickupPointEntity();
        pickupPoint.markCreated(tenantId, actor(actorId));
        applyPickupPoint(pickupPoint, request, tenantId);
        PickupPointEntity saved = pickupPointRepository.save(pickupPoint);
        auditLogService.log(tenantId, actorId, "PickupPoint", saved.getId(), "CREATE", "Created pickup point");
        return mapper.toPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public PickupPointResponse updatePickupPoint(Long id, PickupPointUpsertRequest request, Long tenantId, Long actorId) {
        PickupPointEntity pickupPoint = getPickupPoint(id, tenantId);
        pickupPoint.markUpdated(actor(actorId));
        applyPickupPoint(pickupPoint, request, tenantId);
        PickupPointEntity saved = pickupPointRepository.save(pickupPoint);
        auditLogService.log(tenantId, actorId, "PickupPoint", saved.getId(), "UPDATE", "Updated pickup point");
        return mapper.toPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public void deletePickupPoint(Long id, Long tenantId, Long actorId) {
        softDeleteById(pickupPointRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "PickupPoint", id, "SOFT_DELETE", "Soft deleted pickup point");
    }

    private void applyPickupPoint(PickupPointEntity pickupPoint, PickupPointUpsertRequest request, Long tenantId) {
        validateCoordinatePair(request.getLatitude(), request.getLongitude(), "pickup point");
        pickupPoint.setSchool(schoolService.getSchool(request.getSchoolId(), tenantId));
        pickupPoint.setName(request.getName());
        pickupPoint.setAddress(request.getAddress());
        pickupPoint.setLatitude(request.getLatitude());
        pickupPoint.setLongitude(request.getLongitude());
        pickupPoint.setPickupWindowStart(request.getPickupWindowStart());
        pickupPoint.setPickupWindowEnd(request.getPickupWindowEnd());
        pickupPoint.setIsActive(request.resolveIsActive(Boolean.TRUE));
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
