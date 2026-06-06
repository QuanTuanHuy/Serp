package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.DepotParamsRequest;
import serp.project.school_bus_service.dto.request.DepotUpsertRequest;
import serp.project.school_bus_service.dto.response.DepotResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IDepotService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.repository.DepotRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.util.Set;

@Service
public class DepotServiceImpl extends AbstractBaseService<DepotEntity, Long> implements IDepotService {

    private final DepotRepository depotRepository;
    private final SchoolBusMapper mapper;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;
    private final MessageCommon messageCommon;


    public DepotServiceImpl(
    DepotRepository depotRepository,
                                 SchoolBusMapper mapper,
                                 IAuditLogService auditLogService,
                                 ICodeGeneratorService codeGeneratorService,
                                 MessageCommon messageCommon) {
        this.depotRepository = depotRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
        this.codeGeneratorService = codeGeneratorService;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<DepotEntity, Long> getRepository() {
        return depotRepository;
    }

    @Override
    public PageResponse<DepotResponse> getDepots(DepotParamsRequest params, Long tenantId) {
        return PageResponse.from(depotRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "name", "address", "contactPhone", "description", "code"),
                PageableUtils.from(params,
                        Set.of("id", "name", "code", "createdAt", "updatedAt"), "name")),
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
        depot.setCode(codeGeneratorService.generate(
                SchoolBusCode.DEPOT.sequenceKey(), SchoolBusCode.DEPOT.prefix(), tenantId, actorId));
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
                    AppErrorCode.Coordinate.BOTH_REQUIRED,
                    messageCommon.getMessage(AppErrorCode.Coordinate.BOTH_REQUIRED, target));
        }
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new AppException(
                    AppErrorCode.Coordinate.LATITUDE_RANGE,
                    messageCommon.getMessage(AppErrorCode.Coordinate.LATITUDE_RANGE, target));
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new AppException(
                    AppErrorCode.Coordinate.LONGITUDE_RANGE,
                    messageCommon.getMessage(AppErrorCode.Coordinate.LONGITUDE_RANGE, target));
        }
    }
}
