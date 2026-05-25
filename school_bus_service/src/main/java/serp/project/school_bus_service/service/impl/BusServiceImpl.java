package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.BusParamsRequest;
import serp.project.school_bus_service.dto.request.BusUpsertRequest;
import serp.project.school_bus_service.dto.response.BusResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IBusService;
import serp.project.school_bus_service.service.IDepotService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.BusEntity;
import serp.project.school_bus_service.repository.BusRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.util.Set;

@Service
public class BusServiceImpl extends AbstractBaseService<BusEntity, Long> implements IBusService {

    private static final Set<String> VALID_BUS_STATUSES = Set.of(
            "AVAILABLE", "ASSIGNED", "MAINTENANCE", "INACTIVE");

    private final BusRepository busRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final IDepotService depotService;
    private final MessageCommon messageCommon;


    public BusServiceImpl(
    BusRepository busRepository,
                                 SchoolBusMapper mapper,
                                 IAuditLogService auditLogService,
                                 IDepotService depotService,
                                 MessageCommon messageCommon) {
        this.busRepository = busRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.depotService = depotService;
        this.messageCommon = messageCommon;
    }


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
        applyBus(bus, request, tenantId);
        BusEntity saved = busRepository.save(bus);
        auditLogService.log(tenantId, actorId, "Bus", saved.getId(), "CREATE", "Created bus profile");
        return mapper.toBusResponse(saved);
    }

    @Override
    @Transactional
    public BusResponse updateBus(Long id, BusUpsertRequest request, Long tenantId, Long actorId) {
        BusEntity bus = getBus(id, tenantId);
        bus.markUpdated(actor(actorId));
        applyBus(bus, request, tenantId);
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

    private void applyBus(BusEntity bus, BusUpsertRequest request, Long tenantId) {
        if (request.getStatus() != null && !VALID_BUS_STATUSES.contains(request.getStatus().toUpperCase())) {
            throw new AppException(AppErrorCode.Bus.INVALID_STATUS,
                    messageCommon.getMessage(AppErrorCode.Bus.INVALID_STATUS, request.getStatus(), VALID_BUS_STATUSES));
        }
        if (request.getHomeDepotId() == null) {
            throw new AppException(AppErrorCode.Bus.HOME_DEPOT_REQUIRED, messageCommon.getMessage(AppErrorCode.Bus.HOME_DEPOT_REQUIRED));
        }
        // Plate number uniqueness (exclude self on update)
        Long excludeId = bus.getId() != null ? bus.getId() : -1L;
        if (busRepository.existsByPlateNumberAndTenantIdAndIsDeletedFalseAndIdNot(
                request.getPlateNumber(), tenantId, excludeId)) {
            throw new AppException(AppErrorCode.Bus.PLATE_NUMBER_CONFLICT,
                    messageCommon.getMessage(AppErrorCode.Bus.PLATE_NUMBER_CONFLICT, request.getPlateNumber()));
        }
        bus.setPlateNumber(request.getPlateNumber());
        bus.setBusType(request.getBusType());
        bus.setCapacity(request.getCapacity());
        bus.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "AVAILABLE");
        bus.setIsActive(request.resolveIsActive(Boolean.TRUE));
        bus.setHomeDepot(depotService.getDepot(request.getHomeDepotId(), tenantId));
    }

    @Override
    public long countByTenant(Long tenantId) {
        return busRepository.countByTenantIdAndIsDeletedFalse(tenantId);
    }
}
