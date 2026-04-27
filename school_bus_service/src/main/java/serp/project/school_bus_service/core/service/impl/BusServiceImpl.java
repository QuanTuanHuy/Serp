package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.BusParamsRequest;
import serp.project.school_bus_service.application.dto.request.BusUpsertRequest;
import serp.project.school_bus_service.application.dto.response.BusResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IBusService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.BusEntity;
import serp.project.school_bus_service.infrastructure.store.repository.BusRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class BusServiceImpl extends AbstractBaseService<BusEntity, Long> implements IBusService {

    private final BusRepository busRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;

    @Override
    protected BaseRepository<BusEntity, Long> getRepository() {
        return busRepository;
    }

    @Override
    public PageResponse<BusResponse> getBuses(BusParamsRequest params, Long tenantId) {
        return PageResponse.from(busRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "plateNumber", "busType", "status"),
                PageableUtils.from(params,
                        Set.of("id", "plateNumber", "busType", "capacity", "status", "createdAt", "updatedAt"),
                        "plateNumber")),
                mapper::toBusResponse);
    }

    @Override
    public BusResponse getBusResponse(Long id, Long tenantId) {
        return mapper.toBusResponse(getBus(id, tenantId));
    }

    @Override
    public BusEntity getBus(Long id, Long tenantId) {
        return findById(busRepository, id, tenantId);
    }

    @Override
    @Transactional
    public BusResponse createBus(BusUpsertRequest request, Long tenantId, Long actorId) {
        BusEntity bus = new BusEntity();
        bus.markCreated(tenantId, actor(actorId));
        applyBus(bus, request);
        BusEntity saved = busRepository.save(bus);
        auditLogService.log(tenantId, actorId, "Bus", saved.getId(), "CREATE", "Created bus profile");
        return mapper.toBusResponse(saved);
    }

    @Override
    @Transactional
    public BusResponse updateBus(Long id, BusUpsertRequest request, Long tenantId, Long actorId) {
        BusEntity bus = getBus(id, tenantId);
        bus.markUpdated(actor(actorId));
        applyBus(bus, request);
        BusEntity saved = busRepository.save(bus);
        auditLogService.log(tenantId, actorId, "Bus", saved.getId(), "UPDATE", "Updated bus profile");
        return mapper.toBusResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBus(Long id, Long tenantId, Long actorId) {
        softDeleteById(busRepository, id, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "Bus", id, "SOFT_DELETE", "Soft deleted bus profile");
    }

    private void applyBus(BusEntity bus, BusUpsertRequest request) {
        bus.setPlateNumber(request.getPlateNumber());
        bus.setBusType(request.getBusType());
        bus.setCapacity(request.getCapacity());
        bus.setStatus(request.getStatus());
        bus.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }
}
