package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.PickupPointParamsRequest;
import serp.project.school_bus_service.dto.request.PickupPointUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.PickupPointResponse;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.service.IPickupPointService;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.repository.PickupPointRepository;
import serp.project.school_bus_service.repository.SchoolPickupPointRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.shared.code.SchoolBusCode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PickupPointServiceImpl extends AbstractBaseService<PickupPointEntity, Long> implements IPickupPointService {

    private final PickupPointRepository pickupPointRepository;
    private final SchoolPickupPointRepository schoolPickupPointRepository;
    private final SchoolBusMapper mapper;
    private final ICodeGeneratorService codeGeneratorService;
    private final MessageCommon messageCommon;


    public PickupPointServiceImpl(
            PickupPointRepository pickupPointRepository,
            SchoolPickupPointRepository schoolPickupPointRepository,
            SchoolBusMapper mapper,
            ICodeGeneratorService codeGeneratorService,
            MessageCommon messageCommon) {
        this.pickupPointRepository = pickupPointRepository;
        this.schoolPickupPointRepository = schoolPickupPointRepository;
        this.mapper = mapper;
        this.codeGeneratorService = codeGeneratorService;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<PickupPointEntity, Long> getRepository() {
        return pickupPointRepository;
    }

    @Override
    public PageResponse<PickupPointResponse> getPickupPoints(PickupPointParamsRequest params, Long tenantId) {
        PageResponse<PickupPointResponse> page = PageResponse.from(pickupPointRepository.findAll(
                BaseSpecification.tenantActiveWithKeyword(tenantId,
                        params == null ? null : params.getKeyword(),
                        "name", "address", "code"),
                PageableUtils.from(params,
                        Set.of("id", "name", "code", "createdAt", "updatedAt"), "name")),
                mapper::toPickupPointResponse);

        // Enrich with linked schools
        List<Long> pickupPointIds = page.getItems().stream()
                .map(PickupPointResponse::getId)
                .collect(Collectors.toList());
        if (!pickupPointIds.isEmpty()) {
            Map<Long, List<PickupPointResponse.LinkedSchoolDto>> schoolsByPp =
                    schoolPickupPointRepository.findByPickupPointIdInAndTenantIdAndIsDeletedFalse(pickupPointIds, tenantId)
                            .stream()
                            .collect(Collectors.groupingBy(
                                    spp -> spp.getPickupPoint().getId(),
                                    Collectors.mapping(spp -> new PickupPointResponse.LinkedSchoolDto(
                                            spp.getSchool().getId(),
                                            spp.getSchool().getCode(),
                                            spp.getSchool().getName()
                                    ), Collectors.toList())
                            ));
            page.getItems().forEach(pp -> pp.setSchools(schoolsByPp.getOrDefault(pp.getId(), List.of())));
        }
        return page;
    }

    @Override
    public PickupPointResponse getPickupPointResponse(Long id, Long tenantId) {
        PickupPointResponse response = mapper.toPickupPointResponse(getPickupPoint(id, tenantId));
        List<SchoolPickupPointEntity> links = schoolPickupPointRepository
                .findByPickupPointIdInAndTenantIdAndIsDeletedFalse(List.of(id), tenantId);
        response.setSchools(links.stream()
                .map(spp -> new PickupPointResponse.LinkedSchoolDto(
                        spp.getSchool().getId(), spp.getSchool().getCode(), spp.getSchool().getName()))
                .collect(Collectors.toList()));
        return response;
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
        applyPickupPoint(pickupPoint, request);
        String code = request.getCode();
        if (code == null || code.isBlank()) {
            code = codeGeneratorService.generate(
                    SchoolBusCode.PICKUP_POINT.sequenceKey(), SchoolBusCode.PICKUP_POINT.prefix(), tenantId, actorId);
        }
        pickupPoint.setCode(code);
        PickupPointEntity saved = pickupPointRepository.save(pickupPoint);
        return mapper.toPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public PickupPointResponse updatePickupPoint(Long id, PickupPointUpsertRequest request, Long tenantId, Long actorId) {
        PickupPointEntity pickupPoint = getPickupPoint(id, tenantId);
        pickupPoint.markUpdated(actor(actorId));
        applyPickupPoint(pickupPoint, request);
        PickupPointEntity saved = pickupPointRepository.save(pickupPoint);
        return mapper.toPickupPointResponse(saved);
    }

    @Override
    @Transactional
    public void deletePickupPoint(Long id, Long tenantId, Long actorId) {
        softDeleteById(pickupPointRepository, id, tenantId, actorId);
    }

    private static final Set<String> VALID_USAGE_TYPES = Set.of(
            "PICKUP_ONLY", "DROPOFF_ONLY", "PICKUP_DROPOFF");

    private void applyPickupPoint(PickupPointEntity pickupPoint, PickupPointUpsertRequest request) {
        validateCoordinatePair(request.getLatitude(), request.getLongitude(), "điểm đón/trả");
        if (request.getUsageType() == null || request.getUsageType().isBlank()) {
            throw new AppException(AppErrorCode.PickupPoint.USAGE_TYPE_REQUIRED, messageCommon.getMessage(AppErrorCode.PickupPoint.USAGE_TYPE_REQUIRED));
        }
        if (!VALID_USAGE_TYPES.contains(request.getUsageType())) {
            throw new AppException(AppErrorCode.PickupPoint.USAGE_TYPE_INVALID,
                    messageCommon.getMessage(AppErrorCode.PickupPoint.USAGE_TYPE_INVALID, request.getUsageType(), VALID_USAGE_TYPES));
        }
        pickupPoint.setName(request.getName());
        pickupPoint.setAddress(request.getAddress());
        pickupPoint.setLatitude(request.getLatitude());
        pickupPoint.setLongitude(request.getLongitude());
        pickupPoint.setUsageType(request.getUsageType());
        pickupPoint.setPickupInstruction(request.getPickupInstruction());
        pickupPoint.setIsActive(request.resolveIsActive(Boolean.TRUE));
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
