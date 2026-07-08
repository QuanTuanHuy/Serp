/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.second_mile.caller.TmsOrderClient;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.domain.BagDistributionManifest;
import serp.project.second_mile.domain.BagDistributionManifestBag;
import serp.project.second_mile.domain.BagOrder;
import serp.project.second_mile.domain.Checkin;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AutoPlanBagDistributionRequest;
import serp.project.second_mile.dto.request.BagDistributionManifestFilterRequest;
import serp.project.second_mile.dto.request.ConfirmBagDistributionInboundRequest;
import serp.project.second_mile.dto.request.CreateBagDistributionManifestRequest;
import serp.project.second_mile.dto.request.DriverHandoverCheckinRequest;
import serp.project.second_mile.dto.response.BagDistributionManifestBagResponse;
import serp.project.second_mile.dto.response.BagDistributionManifestResponse;
import serp.project.second_mile.dto.response.BagDistributionPlanItemResponse;
import serp.project.second_mile.dto.response.BagDistributionPlanResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagDistributionManifestStatus;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.CheckinType;
import serp.project.second_mile.enums.HandoverManifestStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.BagDistributionManifestBagRepository;
import serp.project.second_mile.repository.BagDistributionManifestRepository;
import serp.project.second_mile.repository.BagOrderRepository;
import serp.project.second_mile.repository.BagRepository;
import serp.project.second_mile.repository.CheckinRepository;
import serp.project.second_mile.repository.HandoverManifestRepository;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.repository.specification.BagDistributionManifestSpecification;
import serp.project.second_mile.service.BagDistributionManifestService;
import serp.project.second_mile.service.FileStorageService;
import serp.project.second_mile.service.TmsOrderTransitionPublisherService;
import serp.project.second_mile.service.dto.BagDestinationTarget;
import serp.project.second_mile.service.dto.request.FileUploadRequest;
import serp.project.second_mile.service.dto.response.FileUploadResponse;
import serp.project.second_mile.service.validator.BagValidator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BagDistributionManifestServiceImpl implements BagDistributionManifestService {
    private static final String TRANSITION_SOURCE = "SECOND_MILE";
    private static final String STORAGE_SERVICE_NAME = "second-mile";
    private static final String CHECKIN_PHOTO_FOLDER = "bag-distribution-checkin-photo";
    private static final DateTimeFormatter MANIFEST_CODE_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final double CHECKIN_RADIUS_METERS = 100.0;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;
    private static final List<BagDistributionManifestStatus> ACTIVE_MANIFEST_STATUSES = List.of(
            BagDistributionManifestStatus.CREATED,
            BagDistributionManifestStatus.OUTBOUND_CONFIRMED
    );
    private static final List<HandoverManifestStatus> ACTIVE_HANDOVER_STATUSES = List.of(
            HandoverManifestStatus.CREATED,
            HandoverManifestStatus.OUTBOUND_CONFIRMED
    );

    private final BagDistributionManifestRepository manifestRepository;
    private final BagDistributionManifestBagRepository manifestBagRepository;
    private final BagRepository bagRepository;
    private final BagOrderRepository bagOrderRepository;
    private final CheckinRepository checkinRepository;
    private final HandoverManifestRepository handoverManifestRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final HubRepository hubRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;
    private final HubStaffAssignmentRepository hubStaffAssignmentRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;
    private final FileStorageService fileStorageService;
    private final TmsOrderTransitionPublisherService tmsOrderTransitionPublisherService;
    private final BagDistributionPlanningService planningService;
    private final TmsOrderClient tmsOrderClient;
    private final BagValidator bagValidator;

    @Override
    public PageResponse<BagDistributionManifestResponse> listManifests(
            int page,
            int size,
            BagDistributionManifestFilterRequest filterRequest
    ) {
        secondMileAccessUtils.ensureHubOperationOrDriverRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffOrDriverRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<BagDistributionManifest> specification = BagDistributionManifestSpecification.byFilter(
                tenantId,
                filterRequest
        );
        Long driverStaffId = resolveDriverScopedStaffId();
        if (driverStaffId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("assignedDriverId"), driverStaffId)
            );
        }

        Page<BagDistributionManifest> manifestPage = manifestRepository.findAll(specification, pageable);
        List<BagDistributionManifestResponse> items = manifestPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.<BagDistributionManifestResponse>builder()
                .items(items)
                .page(manifestPage.getNumber())
                .size(manifestPage.getSize())
                .totalElements(manifestPage.getTotalElements())
                .totalPages(manifestPage.getTotalPages())
                .hasNext(manifestPage.hasNext())
                .hasPrevious(manifestPage.hasPrevious())
                .build();
    }

    @Override
    public BagDistributionManifestResponse getManifest(Long manifestId) {
        secondMileAccessUtils.ensureHubOperationOrDriverRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffOrDriverRoleOrThrow();

        BagDistributionManifest manifest = getManifestOrThrow(manifestId);
        Long driverStaffId = resolveDriverScopedStaffId();
        if (driverStaffId != null && !Objects.equals(manifest.getAssignedDriverId(), driverStaffId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return toResponse(manifest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagDistributionManifestResponse createManifest(CreateBagDistributionManifestRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        CreatedManifest createdManifest = createManifestInternal(tenantId, request);
        return toResponse(
                createdManifest.manifest(),
                createdManifest.manifestBags(),
                createdManifest.vehicle(),
                createdManifest.route()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagDistributionPlanResponse autoPlan(AutoPlanBagDistributionRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        validateAutoPlanRequest(request);
        validateHub(tenantId, request.getOriginHubId());

        List<BagDistributionPlanItemResponse> planItems = planningService.plan(tenantId, request);
        boolean execute = Boolean.TRUE.equals(request.getExecute());
        if (execute) {
            planItems = executePlanItems(tenantId, request, planItems);
        }

        int manifestCount = (int) planItems.stream()
                .filter(item -> item.createdManifestId() != null || !planningService.hasBlockingHint(item.hints()))
                .count();
        return new BagDistributionPlanResponse(execute, manifestCount, planItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagDistributionManifestResponse confirmOutbound(Long manifestId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        BagDistributionManifest manifest = getManifestOrThrow(manifestId);
        return processOutbound(manifest, false, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagDistributionManifestResponse confirmInbound(
            Long manifestId,
            ConfirmBagDistributionInboundRequest request
    ) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        BagDistributionManifest manifest = getManifestOrThrow(manifestId);
        return processInbound(manifest, request, false, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagDistributionManifestResponse driverCheckinStart(
            Long manifestId,
            DriverHandoverCheckinRequest request,
            MultipartFile photo
    ) {
        secondMileAccessUtils.ensureHubOperationOrDriverRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffOrDriverRoleOrThrow();

        BagDistributionManifest manifest = getManifestOrThrow(manifestId);
        return processOutbound(manifest, true, request, photo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagDistributionManifestResponse driverCheckinEnd(
            Long manifestId,
            DriverHandoverCheckinRequest request,
            MultipartFile photo
    ) {
        secondMileAccessUtils.ensureHubOperationOrDriverRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffOrDriverRoleOrThrow();

        BagDistributionManifest manifest = getManifestOrThrow(manifestId);
        return processInbound(manifest, null, true, request, photo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagDistributionManifestResponse cancel(Long manifestId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        BagDistributionManifest manifest = getManifestOrThrow(manifestId);
        if (manifest.getStatus() != BagDistributionManifestStatus.CREATED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }
        manifest.setStatus(BagDistributionManifestStatus.CANCELLED);
        BagDistributionManifest savedManifest = manifestRepository.save(manifest);
        return toResponse(savedManifest);
    }

    private CreatedManifest createManifestInternal(Long tenantId, CreateBagDistributionManifestRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        validatePlannedWindow(request.getPlannedDepartureAt(), request.getPlannedArrivalAt());
        String destinationPostOfficeCode = validateDestination(
                request.getOriginHubId(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                request.getDestinationPostOfficeCode()
        );
        validateHub(tenantId, request.getOriginHubId());
        if (request.getDestinationType() == BagDestinationType.HUB) {
            validateHub(tenantId, request.getDestinationHubId());
        } else {
            validatePostOfficeMappedToOriginHub(tenantId, request.getOriginHubId(), destinationPostOfficeCode);
        }

        List<Long> bagIds = normalizeIds(request.getBagIds());
        if (bagIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        Vehicle vehicle = validateVehicleForManifest(tenantId, request.getOriginHubId(), request.getVehicleId());
        validateVehicleHasAssignedDriver(tenantId, vehicle);
        validateDriverAssignedToHub(tenantId, vehicle.getAssignedStaffId(), request.getOriginHubId());
        Route route = validateRouteForManifest(
                tenantId,
                request.getOriginHubId(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                destinationPostOfficeCode,
                request.getRouteId(),
                vehicle.getId()
        );

        List<Bag> bags = loadBagsForUpdate(tenantId, bagIds);
        validateBagsForManifest(
                tenantId,
                bags,
                bagIds,
                request.getOriginHubId(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                destinationPostOfficeCode
        );
        validateVehicleCapacity(vehicle, bags);
        validateScheduleAvailability(
                tenantId,
                vehicle.getId(),
                vehicle.getAssignedStaffId(),
                request.getPlannedDepartureAt(),
                request.getPlannedArrivalAt(),
                null
        );

        String manifestCode = generateManifestCode(tenantId, request.getOriginHubId());
        BagDistributionManifest manifest = BagDistributionManifest.builder()
                .manifestCode(manifestCode)
                .originHubId(request.getOriginHubId())
                .destinationType(request.getDestinationType())
                .destinationHubId(request.getDestinationType() == BagDestinationType.HUB
                        ? request.getDestinationHubId()
                        : null)
                .destinationPostOfficeCode(request.getDestinationType() == BagDestinationType.POST_OFFICE
                        ? destinationPostOfficeCode
                        : null)
                .routeId(route.getId())
                .vehicleId(vehicle.getId())
                .assignedDriverId(vehicle.getAssignedStaffId())
                .plannedDepartureAt(request.getPlannedDepartureAt())
                .plannedArrivalAt(request.getPlannedArrivalAt())
                .status(BagDistributionManifestStatus.CREATED)
                .note(trimToNull(request.getNote()))
                .tenantId(tenantId)
                .build();
        BagDistributionManifest savedManifest = manifestRepository.save(manifest);
        List<BagDistributionManifestBag> manifestBags = bags.stream()
                .map(bag -> toManifestBag(savedManifest, bag, tenantId))
                .toList();
        List<BagDistributionManifestBag> savedManifestBags = manifestBagRepository.saveAll(manifestBags);
        return new CreatedManifest(savedManifest, savedManifestBags, vehicle, route);
    }

    private BagDistributionManifestResponse processOutbound(
            BagDistributionManifest manifest,
            boolean driverOnly,
            DriverHandoverCheckinRequest request,
            MultipartFile photo
    ) {
        boolean validStatus = driverOnly
                ? manifest.getStatus() == BagDistributionManifestStatus.CREATED
                || manifest.getStatus() == BagDistributionManifestStatus.OUTBOUND_CONFIRMED
                : manifest.getStatus() == BagDistributionManifestStatus.CREATED;
        if (!validStatus) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }

        Vehicle vehicle = validateAssignedVehicleForLifecycle(manifest);
        validateDriverAssignedToHub(manifest.getTenantId(), vehicle.getAssignedStaffId(), manifest.getOriginHubId());
        if (driverOnly) {
            secondMileAccessUtils.ensureCurrentUserIsAssignedDriverOrThrow(vehicle.getAssignedStaffId());
            recordDriverStartCheckin(manifest, request, photo);
        }

        List<BagDistributionManifestBag> manifestBags = findManifestBags(manifest.getId(), manifest.getTenantId());
        if (manifestBags.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Manifest has no bags.");
        }
        List<Long> bagIds = manifestBags.stream()
                .map(BagDistributionManifestBag::getBagId)
                .toList();
        List<Bag> bags = loadBagsForUpdate(manifest.getTenantId(), bagIds);
        Map<Long, Bag> bagById = bags.stream().collect(Collectors.toMap(Bag::getId, bag -> bag));
        LocalDateTime now = LocalDateTime.now();
        List<TmsOrderStatusTransitionRequest.Item> transitionItems = new ArrayList<>();
        TmsOrderStatusTransitionRequest.Context context = buildTransitionContext(
                manifest,
                vehicle,
                loadRoute(manifest.getRouteId()),
                now,
                false
        );

        for (BagDistributionManifestBag manifestBag : manifestBags) {
            Bag bag = bagById.get(manifestBag.getBagId());
            if (bag == null || !Objects.equals(bag.getTenantId(), manifest.getTenantId())) {
                throw new AppException(ErrorCode.BAG_NOT_FOUND);
            }
            if (bag.getStatus() != BagStatus.SEALED
                    && bag.getStatus() != BagStatus.ARRIVED
                    && bag.getStatus() != BagStatus.IN_TRANSIT) {
                throw new AppException(ErrorCode.BAG_STATUS_INVALID);
            }
            if (manifestBag.getScanOutTime() == null) {
                manifestBag.setScanOutTime(now);
            }
            bag.setStatus(BagStatus.IN_TRANSIT);
            bag.setVehicleId(vehicle.getId());
            List<BagOrder> bagOrders = bagOrderRepository.findByBag_IdAndTenantId(bag.getId(), manifest.getTenantId());
            for (BagOrder bagOrder : bagOrders) {
                bagOrder.setLastKnownStatus(OrderStatus.BAG_IN_TRANSIT.name());
                transitionItems.add(toTransitionItem(
                        bagOrder,
                        OrderStatus.BAG_IN_TRANSIT,
                        List.of(
                                OrderStatus.BAG_SEALED,
                                OrderStatus.INBOUND_AT_DESTINATION_HUB,
                                OrderStatus.BAG_IN_TRANSIT
                        ),
                        "Đã xác nhận xuất kho phân phối bao trung chuyển.",
                        context
                ));
            }
            bagOrderRepository.saveAll(bagOrders);
        }

        bagRepository.saveAll(bags);
        manifestBagRepository.saveAll(manifestBags);
        if (manifest.getActualDepartureAt() == null) {
            manifest.setActualDepartureAt(now);
        }
        manifest.setStatus(BagDistributionManifestStatus.OUTBOUND_CONFIRMED);
        BagDistributionManifest savedManifest = manifestRepository.save(manifest);
        enqueueTransitions(
                manifest.getTenantId(),
                idempotencyKey("bag-distribution", manifest.getId(), "outbound", savedManifest.getActualDepartureAt()),
                transitionItems
        );
        return toResponse(savedManifest, manifestBags, vehicle, loadRoute(savedManifest.getRouteId()));
    }

    private BagDistributionManifestResponse processInbound(
            BagDistributionManifest manifest,
            ConfirmBagDistributionInboundRequest request,
            boolean driverOnly,
            DriverHandoverCheckinRequest checkinRequest,
            MultipartFile photo
    ) {
        if (manifest.getStatus() != BagDistributionManifestStatus.OUTBOUND_CONFIRMED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }

        Vehicle vehicle = validateAssignedVehicleForLifecycle(manifest);
        if (driverOnly) {
            secondMileAccessUtils.ensureCurrentUserIsAssignedDriverOrThrow(vehicle.getAssignedStaffId());
            recordDriverEndCheckin(manifest, checkinRequest, photo);
        }

        Long tenantId = manifest.getTenantId();
        List<BagDistributionManifestBag> targets = resolveInboundTargets(manifest, request);
        if (targets.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "No bags selected for inbound confirmation.");
        }

        List<Long> bagIds = targets.stream().map(BagDistributionManifestBag::getBagId).toList();
        List<Bag> bags = loadBagsForUpdate(tenantId, bagIds);
        Map<Long, Bag> bagById = bags.stream().collect(Collectors.toMap(Bag::getId, bag -> bag));
        LocalDateTime now = LocalDateTime.now();
        OrderStatus inboundStatus = resolveInboundOrderStatus(manifest.getDestinationType());
        TmsOrderStatusTransitionRequest.Context context = buildTransitionContext(
                manifest,
                vehicle,
                loadRoute(manifest.getRouteId()),
                now,
                true
        );
        List<TmsOrderStatusTransitionRequest.Item> transitionItems = new ArrayList<>();
        int newlyArrivedOrderCount = 0;

        for (BagDistributionManifestBag target : targets) {
            Bag bag = bagById.get(target.getBagId());
            if (bag == null || !Objects.equals(bag.getTenantId(), tenantId)) {
                throw new AppException(ErrorCode.BAG_NOT_FOUND);
            }
            if (bag.getStatus() != BagStatus.IN_TRANSIT && bag.getStatus() != BagStatus.ARRIVED) {
                throw new AppException(ErrorCode.BAG_STATUS_INVALID);
            }
            boolean newlyArrivedBag = target.getScanInTime() == null;
            if (target.getScanOutTime() == null) {
                target.setScanOutTime(now);
            }
            target.setScanInTime(now);
            List<BagOrder> bagOrders = bagOrderRepository.findByBag_IdAndTenantId(bag.getId(), tenantId);
            bag.setStatus(BagStatus.ARRIVED);
            bag.setVehicleId(vehicle.getId());
            prepareArrivedBagForNextLeg(tenantId, manifest, bag, bagOrders);
            if (newlyArrivedBag) {
                newlyArrivedOrderCount += bagOrders.size();
            }
            for (BagOrder bagOrder : bagOrders) {
                bagOrder.setLastKnownStatus(inboundStatus.name());
                transitionItems.add(toTransitionItem(
                        bagOrder,
                        inboundStatus,
                        List.of(OrderStatus.BAG_IN_TRANSIT, inboundStatus),
                        "Đã xác nhận nhập kho phân phối bao trung chuyển.",
                        context
                ));
            }
            bagOrderRepository.saveAll(bagOrders);
        }

        if (manifest.getDestinationType() == BagDestinationType.HUB) {
            addDestinationHubLoad(manifest, newlyArrivedOrderCount);
        }

        bagRepository.saveAll(bags);
        manifestBagRepository.saveAll(targets);
        List<BagDistributionManifestBag> allManifestBags = findManifestBags(manifest.getId(), tenantId);
        boolean allInbound = allManifestBags.stream().allMatch(item -> item.getScanInTime() != null);
        if (allInbound) {
            manifest.setStatus(BagDistributionManifestStatus.INBOUND_CONFIRMED);
            if (manifest.getActualArrivalAt() == null) {
                manifest.setActualArrivalAt(now);
            }
        }
        BagDistributionManifest savedManifest = manifestRepository.save(manifest);
        enqueueTransitions(
                tenantId,
                idempotencyKey("bag-distribution", manifest.getId(), "inbound", now),
                transitionItems
        );
        return toResponse(savedManifest, allManifestBags, vehicle, loadRoute(savedManifest.getRouteId()));
    }

    private List<BagDistributionPlanItemResponse> executePlanItems(
            Long tenantId,
            AutoPlanBagDistributionRequest request,
            List<BagDistributionPlanItemResponse> planItems
    ) {
        List<BagDistributionPlanItemResponse> executedItems = new ArrayList<>();
        for (BagDistributionPlanItemResponse item : planItems) {
            if (planningService.hasBlockingHint(item.hints())) {
                executedItems.add(item);
                continue;
            }

            CreateBagDistributionManifestRequest createRequest = new CreateBagDistributionManifestRequest(
                    item.originHubId(),
                    item.destinationType(),
                    item.destinationHubId(),
                    item.destinationPostOfficeCode(),
                    item.routeId(),
                    item.vehicleId(),
                    request.getPlannedDepartureAt(),
                    request.getPlannedArrivalAt(),
                    item.bagIds(),
                    trimToNull(request.getNote())
            );
            CreatedManifest createdManifest = createManifestInternal(tenantId, createRequest);
            executedItems.add(new BagDistributionPlanItemResponse(
                    item.originHubId(),
                    item.destinationType(),
                    item.destinationHubId(),
                    item.destinationPostOfficeCode(),
                    item.routeId(),
                    item.routeCode(),
                    item.vehicleId(),
                    item.vehicleLicensePlate(),
                    item.assignedDriverId(),
                    item.plannedDepartureAt(),
                    item.plannedArrivalAt(),
                    item.bagIds(),
                    item.bagCodes(),
                    item.totalWeight(),
                    item.totalVolume(),
                    item.totalOrders(),
                    item.score(),
                    item.hints(),
                    createdManifest.manifest().getId(),
                    createdManifest.manifest().getManifestCode()
            ));
        }
        return executedItems;
    }

    private void validateAutoPlanRequest(AutoPlanBagDistributionRequest request) {
        if (request == null || request.getOriginHubId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        validatePlannedWindow(request.getPlannedDepartureAt(), request.getPlannedArrivalAt());
        if (request.getDestinationType() != null) {
            validateDestination(
                    request.getOriginHubId(),
                    request.getDestinationType(),
                    request.getDestinationHubId(),
                    request.getDestinationPostOfficeCode()
            );
        }
    }

    private BagDistributionManifest getManifestOrThrow(Long manifestId) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        return manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Bag distribution manifest not found."));
    }

    private List<BagDistributionManifestBag> findManifestBags(Long manifestId, Long tenantId) {
        return manifestBagRepository.findByManifest_IdAndTenantId(manifestId, tenantId);
    }

    private List<BagDistributionManifestBag> resolveInboundTargets(
            BagDistributionManifest manifest,
            ConfirmBagDistributionInboundRequest request
    ) {
        Long tenantId = manifest.getTenantId();
        if (request == null || request.getBagIds() == null || request.getBagIds().isEmpty()) {
            return findManifestBags(manifest.getId(), tenantId);
        }

        List<Long> bagIds = normalizeIds(request.getBagIds());
        List<BagDistributionManifestBag> targets = manifestBagRepository.findByManifest_IdAndBagIdInAndTenantId(
                manifest.getId(),
                bagIds,
                tenantId
        );
        if (targets.size() != bagIds.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Some bags are not present in this manifest.");
        }
        return targets;
    }

    private List<Bag> loadBagsForUpdate(Long tenantId, List<Long> bagIds) {
        List<Bag> bags = bagRepository.findByIdInAndTenantIdForUpdate(tenantId, bagIds);
        if (bags.size() != bagIds.size()) {
            throw new AppException(ErrorCode.BAG_NOT_FOUND);
        }
        Map<Long, Bag> bagById = bags.stream().collect(Collectors.toMap(Bag::getId, bag -> bag));
        List<Bag> ordered = new ArrayList<>();
        for (Long bagId : bagIds) {
            Bag bag = bagById.get(bagId);
            if (bag == null) {
                throw new AppException(ErrorCode.BAG_NOT_FOUND);
            }
            ordered.add(bag);
        }
        return ordered;
    }

    private void validateBagsForManifest(
            Long tenantId,
            List<Bag> bags,
            List<Long> bagIds,
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode
    ) {
        List<Long> activeBagIds = manifestBagRepository.findActiveBagIds(
                tenantId,
                bagIds,
                ACTIVE_MANIFEST_STATUSES
        );
        if (!activeBagIds.isEmpty()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Bag already belongs to an active distribution manifest: " + activeBagIds
            );
        }

        for (Bag bag : bags) {
            if (!isDispatchReadyBagStatus(bag.getStatus())) {
                throw new AppException(ErrorCode.BAG_STATUS_INVALID);
            }
            if (!Objects.equals(bag.getOriginHubId(), originHubId)) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID, "All bags must match the origin hub.");
            }
            if (bag.getDestinationType() != destinationType) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID, "Manifest cannot mix bag destinations.");
            }
            if (destinationType == BagDestinationType.HUB
                    && !Objects.equals(bag.getDestinationHubId(), destinationHubId)) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID, "All bags must match the destination hub.");
            }
            if (destinationType == BagDestinationType.POST_OFFICE
                    && !Objects.equals(normalizeText(bag.getDestinationPostOfficeCode()), destinationPostOfficeCode)) {
                throw new AppException(
                        ErrorCode.BAG_DESTINATION_INVALID,
                        "All bags must match the destination post office."
                );
            }
        }
    }

    private boolean isDispatchReadyBagStatus(BagStatus status) {
        return status == BagStatus.SEALED || status == BagStatus.ARRIVED;
    }

    private void prepareArrivedBagForNextLeg(
            Long tenantId,
            BagDistributionManifest manifest,
            Bag bag,
            List<BagOrder> bagOrders
    ) {
        if (manifest.getDestinationType() != BagDestinationType.HUB
                || manifest.getDestinationHubId() == null
                || !Objects.equals(bag.getDestinationHubId(), manifest.getDestinationHubId())) {
            return;
        }
        BagDestinationTarget nextTarget = resolveNextTargetForArrivedBag(
                tenantId,
                manifest.getDestinationHubId(),
                bagOrders
        );
        if (nextTarget == null) {
            return;
        }

        bag.setOriginHubId(manifest.getDestinationHubId());
        bag.setDestinationType(nextTarget.destinationType());
        bag.setDestinationHubId(nextTarget.destinationType() == BagDestinationType.HUB
                ? nextTarget.destinationHubId()
                : null);
        bag.setDestinationPostOfficeCode(nextTarget.destinationType() == BagDestinationType.POST_OFFICE
                ? normalizeText(nextTarget.destinationPostOfficeCode())
                : null);
    }

    private BagDestinationTarget resolveNextTargetForArrivedBag(
            Long tenantId,
            Long currentHubId,
            List<BagOrder> bagOrders
    ) {
        if (bagOrders == null || bagOrders.isEmpty()) {
            return null;
        }

        Map<Long, TmsOrderOperationView> orderById = tmsOrderClient.lookupByIds(
                        tenantId,
                        bagOrders.stream()
                                .map(BagOrder::getTmsOrderId)
                                .filter(Objects::nonNull)
                                .toList()
                )
                .stream()
                .filter(order -> order.getId() != null)
                .collect(Collectors.toMap(TmsOrderOperationView::getId, order -> order));
        BagDestinationTarget sharedTarget = null;
        for (BagOrder bagOrder : bagOrders) {
            TmsOrderOperationView order = orderById.get(bagOrder.getTmsOrderId());
            if (order == null) {
                throw new AppException(ErrorCode.BAG_ORDER_NOT_FOUND);
            }
            bagValidator.validateTmsOrderTenant(tenantId, order);
            BagDestinationTarget nextTarget = bagValidator.resolveDestinationTargetForOrder(
                    tenantId,
                    order,
                    currentHubId
            );
            if (sharedTarget == null) {
                sharedTarget = nextTarget;
                continue;
            }
            if (!sameDestinationTarget(sharedTarget, nextTarget)) {
                throw new AppException(
                        ErrorCode.BAG_DESTINATION_INVALID,
                        "Orders in the same bag must share the same next planned route leg."
                );
            }
        }
        return sharedTarget;
    }

    private boolean sameDestinationTarget(BagDestinationTarget first, BagDestinationTarget second) {
        if (first == null || second == null || first.destinationType() != second.destinationType()) {
            return false;
        }
        if (first.destinationType() == BagDestinationType.HUB) {
            return Objects.equals(first.destinationHubId(), second.destinationHubId());
        }
        return Objects.equals(
                normalizeText(first.destinationPostOfficeCode()),
                normalizeText(second.destinationPostOfficeCode())
        );
    }

    private String validateDestination(
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode
    ) {
        if (originHubId == null || destinationType == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (destinationType == BagDestinationType.HUB) {
            if (destinationHubId == null || Objects.equals(originHubId, destinationHubId)) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            return null;
        }
        String normalizedPostOfficeCode = normalizeText(destinationPostOfficeCode);
        if (normalizedPostOfficeCode == null) {
            throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
        }
        return normalizedPostOfficeCode;
    }

    private Hub validateHub(Long tenantId, Long hubId) {
        if (hubId == null) {
            throw new AppException(ErrorCode.HUB_NOT_FOUND);
        }
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND));
        if (!Objects.equals(hub.getTenantId(), tenantId) || !hub.isActive()) {
            throw new AppException(ErrorCode.HUB_NOT_FOUND);
        }
        return hub;
    }

    private void validatePostOfficeMappedToOriginHub(Long tenantId, Long originHubId, String destinationPostOfficeCode) {
        HubPostOfficeMapping mapping = hubPostOfficeMappingRepository
                .findByTenantIdAndPostOfficeCode(tenantId, destinationPostOfficeCode)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_POST_OFFICE_INVALID));
        if (mapping.getHub() == null || !Objects.equals(mapping.getHub().getId(), originHubId)) {
            throw new AppException(
                    ErrorCode.ROUTE_POST_OFFICE_INVALID,
                    "Destination post office must be mapped to the origin hub."
            );
        }
    }

    private Vehicle validateVehicleForManifest(Long tenantId, Long originHubId, Long vehicleId) {
        if (vehicleId == null) {
            throw new AppException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
        if (!Objects.equals(vehicle.getTenantId(), tenantId) || vehicle.getStatus() != VehicleStatus.ACTIVE) {
            throw new AppException(ErrorCode.ROUTE_VEHICLE_INVALID, "Vehicle is not active for bag distribution.");
        }
        if (!Objects.equals(vehicle.getHubId(), originHubId)) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    "Vehicle must belong to the origin hub."
            );
        }
        return vehicle;
    }

    private Vehicle validateAssignedVehicleForLifecycle(BagDistributionManifest manifest) {
        Vehicle vehicle = validateVehicleForManifest(
                manifest.getTenantId(),
                manifest.getOriginHubId(),
                manifest.getVehicleId()
        );
        validateVehicleHasAssignedDriver(manifest.getTenantId(), vehicle);
        return vehicle;
    }

    private void validateVehicleHasAssignedDriver(Long tenantId, Vehicle vehicle) {
        if (vehicle == null) {
            throw new AppException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        secondMileAccessUtils.ensureActiveDriverStaffOrThrow(tenantId, vehicle.getAssignedStaffId());
    }

    private void validateDriverAssignedToHub(Long tenantId, Long driverStaffId, Long hubId) {
        if (driverStaffId == null || hubId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Assigned vehicle driver and hub are required.");
        }
        boolean assignedToHub = hubStaffAssignmentRepository
                .findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(
                        driverStaffId,
                        hubId,
                        tenantId,
                        LocalDate.now()
                )
                .isPresent();
        if (!assignedToHub) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Assigned vehicle driver must be active at the origin hub."
            );
        }
    }

    private Route validateRouteForManifest(
            Long tenantId,
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode,
            Long routeId,
            Long vehicleId
    ) {
        if (routeId == null) {
            throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID, "Route is required.");
        }
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND));
        if (!Objects.equals(route.getTenantId(), tenantId) || route.getStatus() != RouteStatus.ACTIVE) {
            throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
        }
        if (route.getOriginType() != RouteEndpointType.HUB || !Objects.equals(route.getOriginHubId(), originHubId)) {
            throw new AppException(ErrorCode.ROUTE_HUB_INVALID, "Route origin hub must match manifest origin hub.");
        }
        RouteDestinationType expectedRouteDestinationType = RouteDestinationType.valueOf(destinationType.name());
        if (route.getDestinationType() != expectedRouteDestinationType) {
            throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID, "Route destination type does not match.");
        }
        if (destinationType == BagDestinationType.HUB
                && !Objects.equals(route.getDestinationHubId(), destinationHubId)) {
            throw new AppException(ErrorCode.ROUTE_HUB_INVALID);
        }
        if (destinationType == BagDestinationType.POST_OFFICE
                && !Objects.equals(normalizeText(route.getDestinationPostOfficeCode()), destinationPostOfficeCode)) {
            throw new AppException(ErrorCode.ROUTE_POST_OFFICE_INVALID);
        }
        if (route.getVehicleId() != null && !Objects.equals(route.getVehicleId(), vehicleId)) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    "Selected vehicle must match the vehicle assigned to the route."
            );
        }
        return route;
    }

    private void validateVehicleCapacity(Vehicle vehicle, List<Bag> bags) {
        double totalWeight = bags.stream().mapToDouble(this::safeCurrentWeight).sum();
        double totalVolume = bags.stream().mapToDouble(this::safeCurrentVolume).sum();
        int totalBags = bags.size();
        if (vehicle.getMaxWeight() > 0 && totalWeight > vehicle.getMaxWeight()) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    String.format(Locale.ROOT, "Vehicle weight capacity exceeded: %.2f > %.2f", totalWeight, vehicle.getMaxWeight())
            );
        }
        if (vehicle.getMaxVolume() > 0 && totalVolume > vehicle.getMaxVolume()) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    String.format(Locale.ROOT, "Vehicle volume capacity exceeded: %.2f > %.2f", totalVolume, vehicle.getMaxVolume())
            );
        }
        if (vehicle.getMaxBags() > 0 && totalBags > vehicle.getMaxBags()) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    String.format(Locale.ROOT, "Vehicle bag capacity exceeded: %d > %d", totalBags, vehicle.getMaxBags())
            );
        }
    }

    private void validateScheduleAvailability(
            Long tenantId,
            Long vehicleId,
            Long assignedDriverId,
            LocalDateTime plannedDepartureAt,
            LocalDateTime plannedArrivalAt,
            Long excludeManifestId
    ) {
        if (hasScheduleConflict(
                tenantId,
                vehicleId,
                assignedDriverId,
                plannedDepartureAt,
                plannedArrivalAt,
                excludeManifestId
        )) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    "Vehicle or driver is already assigned to another active manifest in this time window."
            );
        }
    }

    private boolean hasScheduleConflict(
            Long tenantId,
            Long vehicleId,
            Long assignedDriverId,
            LocalDateTime plannedDepartureAt,
            LocalDateTime plannedArrivalAt,
            Long excludeManifestId
    ) {
        if (vehicleId == null || plannedDepartureAt == null || plannedArrivalAt == null) {
            return true;
        }
        boolean hasActiveHandover = handoverManifestRepository.existsOverlappingActiveAssignment(
                tenantId,
                vehicleId,
                assignedDriverId,
                plannedDepartureAt,
                plannedArrivalAt,
                ACTIVE_HANDOVER_STATUSES,
                null
        );
        if (hasActiveHandover) {
            return true;
        }
        return manifestRepository.existsOverlappingActiveAssignment(
                tenantId,
                vehicleId,
                assignedDriverId,
                plannedDepartureAt,
                plannedArrivalAt,
                ACTIVE_MANIFEST_STATUSES,
                excludeManifestId
        );
    }

    private void recordDriverStartCheckin(
            BagDistributionManifest manifest,
            DriverHandoverCheckinRequest request,
            MultipartFile photo
    ) {
        if (findManifestCheckin(manifest, CheckinType.BAG_DISTRIBUTION_START) != null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Driver has already checked in at the origin hub.");
        }
        validateCheckinRequest(request);
        validateCheckinPhoto(photo);
        Hub originHub = validateHub(manifest.getTenantId(), manifest.getOriginHubId());
        Point originLocation = originHub.getLocation();
        if (originLocation == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Origin hub location is missing.");
        }
        double distanceMeters = calculateDistanceMeters(
                request.getLatitude(),
                request.getLongitude(),
                originLocation.getY(),
                originLocation.getX()
        );
        validateCheckinDistance(distanceMeters, "Driver must check in within 100m of the origin hub.");

        LocalDateTime now = LocalDateTime.now();
        if (manifest.getActualDepartureAt() == null) {
            manifest.setActualDepartureAt(now);
        }
        saveManifestCheckin(
                manifest,
                CheckinType.BAG_DISTRIBUTION_START,
                request,
                now,
                distanceMeters,
                CHECKIN_RADIUS_METERS,
                uploadCheckinPhoto(photo, manifest.getTenantId())
        );
    }

    private void recordDriverEndCheckin(
            BagDistributionManifest manifest,
            DriverHandoverCheckinRequest request,
            MultipartFile photo
    ) {
        if (manifest.getActualDepartureAt() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Driver departure check-in is required before arrival.");
        }
        if (findManifestCheckin(manifest, CheckinType.BAG_DISTRIBUTION_END) != null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Driver has already checked in at the destination.");
        }
        validateCheckinRequest(request);
        validateCheckinPhoto(photo);

        Double distanceMeters = null;
        if (manifest.getDestinationType() == BagDestinationType.HUB) {
            Hub destinationHub = validateHub(manifest.getTenantId(), manifest.getDestinationHubId());
            Point destinationLocation = destinationHub.getLocation();
            if (destinationLocation == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Destination hub location is missing.");
            }
            distanceMeters = calculateDistanceMeters(
                    request.getLatitude(),
                    request.getLongitude(),
                    destinationLocation.getY(),
                    destinationLocation.getX()
            );
            validateCheckinDistance(distanceMeters, "Driver must check in within 100m of the destination hub.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (manifest.getActualArrivalAt() == null) {
            manifest.setActualArrivalAt(now);
        }
        saveManifestCheckin(
                manifest,
                CheckinType.BAG_DISTRIBUTION_END,
                request,
                now,
                distanceMeters,
                manifest.getDestinationType() == BagDestinationType.HUB ? CHECKIN_RADIUS_METERS : null,
                uploadCheckinPhoto(photo, manifest.getTenantId())
        );
    }

    private void saveManifestCheckin(
            BagDistributionManifest manifest,
            CheckinType checkinType,
            DriverHandoverCheckinRequest request,
            LocalDateTime checkinTime,
            Double distanceMeters,
            Double allowedRadiusMeters,
            String photoUrl
    ) {
        Checkin checkin = Checkin.builder()
                .checkinType(checkinType)
                .bagDistributionManifestId(manifest.getId())
                .driverStaffId(manifest.getAssignedDriverId())
                .checkinTime(checkinTime)
                .checkinLocation(toPoint(request.getLatitude(), request.getLongitude()))
                .distanceM(distanceMeters)
                .allowedRadiusM(allowedRadiusMeters)
                .locationLabel(trimToNull(request.getLocationLabel()))
                .photoUrl(photoUrl)
                .tenantId(manifest.getTenantId())
                .build();
        checkinRepository.save(checkin);
    }

    private void validateCheckinRequest(DriverHandoverCheckinRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Driver check-in location is required.");
        }
        validateGeoCoordinatePair(request.getLatitude(), request.getLongitude(), "Driver check-in location is invalid.");
    }

    private void validateCheckinPhoto(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }
        ImageContentTypeUtils.normalizeImageContentType(photo.getContentType());
    }

    private String uploadCheckinPhoto(MultipartFile photo, Long tenantId) {
        String contentType = ImageContentTypeUtils.normalizeImageContentType(photo.getContentType());
        try {
            FileUploadResponse uploadResponse = fileStorageService.upload(FileUploadRequest.builder()
                    .content(photo.getBytes())
                    .originalFileName(photo.getOriginalFilename())
                    .contentType(contentType)
                    .serviceName(STORAGE_SERVICE_NAME)
                    .folder(CHECKIN_PHOTO_FOLDER)
                    .tenantId(tenantId)
                    .uploaderId(secondMileAccessUtils.getCurrentUserIdOrNull())
                    .publicFile(true)
                    .build());
            return uploadResponse.getUrl();
        } catch (IOException exception) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void validatePlannedWindow(LocalDateTime plannedDepartureAt, LocalDateTime plannedArrivalAt) {
        if (plannedDepartureAt == null || plannedArrivalAt == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "planned_departure_at and planned_arrival_at are required."
            );
        }
        if (!plannedArrivalAt.isAfter(plannedDepartureAt)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "planned_arrival_at must be after planned_departure_at."
            );
        }
    }

    private void validateGeoCoordinatePair(Double latitude, Double longitude, String message) {
        if (latitude == null || longitude == null
                || latitude < -90.0 || latitude > 90.0
                || longitude < -180.0 || longitude > 180.0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, message);
        }
    }

    private void validateCheckinDistance(double distanceMeters, String message) {
        if (distanceMeters > CHECKIN_RADIUS_METERS) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    String.format(Locale.ROOT, "%s Distance: %.1fm.", message, distanceMeters)
            );
        }
    }

    private double calculateDistanceMeters(
            double latitude,
            double longitude,
            double targetLatitude,
            double targetLongitude
    ) {
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(targetLatitude);
        double deltaLat = Math.toRadians(targetLatitude - latitude);
        double deltaLon = Math.toRadians(targetLongitude - longitude);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private Point toPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    private Checkin findManifestCheckin(BagDistributionManifest manifest, CheckinType checkinType) {
        if (manifest == null || manifest.getId() == null || manifest.getTenantId() == null) {
            return null;
        }
        return checkinRepository
                .findByTenantIdAndCheckinTypeAndBagDistributionManifestId(
                        manifest.getTenantId(),
                        checkinType,
                        manifest.getId()
                )
                .orElse(null);
    }

    private Double latitudeOf(Checkin checkin) {
        Point location = checkin == null ? null : checkin.getCheckinLocation();
        return location == null ? null : Double.valueOf(location.getY());
    }

    private Double longitudeOf(Checkin checkin) {
        Point location = checkin == null ? null : checkin.getCheckinLocation();
        return location == null ? null : Double.valueOf(location.getX());
    }

    private void addDestinationHubLoad(BagDistributionManifest manifest, int incomingOrders) {
        if (incomingOrders <= 0 || manifest.getDestinationHubId() == null) {
            return;
        }
        Hub hub = hubRepository.findByIdAndTenantIdForUpdate(manifest.getDestinationHubId(), manifest.getTenantId())
                .orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND));
        hub.addLoad(incomingOrders);
        hubRepository.save(hub);
    }

    private BagDistributionManifestBag toManifestBag(
            BagDistributionManifest manifest,
            Bag bag,
            Long tenantId
    ) {
        return BagDistributionManifestBag.builder()
                .manifest(manifest)
                .bagId(bag.getId())
                .bagCode(bag.getBagCode())
                .originHubId(bag.getOriginHubId())
                .destinationType(bag.getDestinationType())
                .destinationHubId(bag.getDestinationHubId())
                .destinationPostOfficeCode(normalizeText(bag.getDestinationPostOfficeCode()))
                .totalWeightSnapshot(safeCurrentWeight(bag))
                .totalVolumeSnapshot(safeCurrentVolume(bag))
                .totalOrdersSnapshot(safeCurrentOrders(bag))
                .tenantId(tenantId)
                .build();
    }

    private BagDistributionManifestResponse toResponse(BagDistributionManifest manifest) {
        return toResponse(
                manifest,
                findManifestBags(manifest.getId(), manifest.getTenantId()),
                loadVehicle(manifest.getVehicleId()),
                loadRoute(manifest.getRouteId())
        );
    }

    private BagDistributionManifestResponse toResponse(
            BagDistributionManifest manifest,
            List<BagDistributionManifestBag> manifestBags,
            Vehicle vehicle,
            Route route
    ) {
        Hub originHub = loadHub(manifest.getOriginHubId());
        Hub destinationHub = manifest.getDestinationHubId() == null ? null : loadHub(manifest.getDestinationHubId());
        Checkin startCheckin = findManifestCheckin(manifest, CheckinType.BAG_DISTRIBUTION_START);
        Checkin endCheckin = findManifestCheckin(manifest, CheckinType.BAG_DISTRIBUTION_END);
        List<BagDistributionManifestBagResponse> bagResponses = manifestBags.stream()
                .map(item -> new BagDistributionManifestBagResponse(
                        item.getId(),
                        item.getBagId(),
                        item.getBagCode(),
                        item.getOriginHubId(),
                        item.getDestinationType(),
                        item.getDestinationHubId(),
                        item.getDestinationPostOfficeCode(),
                        item.getTotalWeightSnapshot(),
                        item.getTotalVolumeSnapshot(),
                        item.getTotalOrdersSnapshot(),
                        item.getScanOutTime(),
                        item.getScanInTime()
                ))
                .toList();
        return new BagDistributionManifestResponse(
                manifest.getId(),
                manifest.getManifestCode(),
                manifest.getOriginHubId(),
                originHub == null ? null : originHub.getCode(),
                manifest.getDestinationType(),
                manifest.getDestinationHubId(),
                destinationHub == null ? null : destinationHub.getCode(),
                manifest.getDestinationPostOfficeCode(),
                manifest.getRouteId(),
                route == null ? null : route.getRouteCode(),
                manifest.getVehicleId(),
                vehicle == null ? null : vehicle.getLicensePlate(),
                manifest.getAssignedDriverId(),
                manifest.getPlannedDepartureAt(),
                manifest.getPlannedArrivalAt(),
                manifest.getActualDepartureAt(),
                manifest.getActualArrivalAt(),
                startCheckin == null ? null : startCheckin.getId(),
                startCheckin == null ? null : startCheckin.getCheckinTime(),
                latitudeOf(startCheckin),
                longitudeOf(startCheckin),
                startCheckin == null ? null : startCheckin.getDistanceM(),
                startCheckin == null ? null : startCheckin.getLocationLabel(),
                startCheckin == null ? null : startCheckin.getPhotoUrl(),
                endCheckin == null ? null : endCheckin.getId(),
                endCheckin == null ? null : endCheckin.getCheckinTime(),
                latitudeOf(endCheckin),
                longitudeOf(endCheckin),
                endCheckin == null ? null : endCheckin.getDistanceM(),
                endCheckin == null ? null : endCheckin.getLocationLabel(),
                endCheckin == null ? null : endCheckin.getPhotoUrl(),
                manifest.getStatus(),
                manifest.getNote(),
                bagResponses,
                manifest.getCreatedAt(),
                manifest.getUpdatedAt()
        );
    }

    private TmsOrderStatusTransitionRequest.Item toTransitionItem(
            BagOrder bagOrder,
            OrderStatus targetStatus,
            List<OrderStatus> expectedStatuses,
            String description,
            TmsOrderStatusTransitionRequest.Context context
    ) {
        return TmsOrderStatusTransitionRequest.Item.builder()
                .orderId(bagOrder.getTmsOrderId())
                .orderCode(bagOrder.getOrderCode())
                .expectedStatuses(expectedStatuses)
                .targetStatus(targetStatus)
                .description(description)
                .context(context)
                .build();
    }

    private TmsOrderStatusTransitionRequest.Context buildTransitionContext(
            BagDistributionManifest manifest,
            Vehicle vehicle,
            Route route,
            LocalDateTime eventTime,
            boolean inbound
    ) {
        Hub hub = inbound && manifest.getDestinationType() == BagDestinationType.HUB
                ? loadHub(manifest.getDestinationHubId())
                : loadHub(manifest.getOriginHubId());
        return TmsOrderStatusTransitionRequest.Context.builder()
                .eventTime(eventTime)
                .hubId(hub == null ? null : hub.getId())
                .hubCode(hub == null ? null : hub.getCode())
                .hubName(hub == null ? null : hub.getName())
                .postOfficeCode(inbound && manifest.getDestinationType() == BagDestinationType.POST_OFFICE
                        ? manifest.getDestinationPostOfficeCode()
                        : null)
                .manifestId(manifest.getId())
                .manifestCode(manifest.getManifestCode())
                .routeId(route == null ? manifest.getRouteId() : route.getId())
                .routeCode(route == null ? null : route.getRouteCode())
                .driverId(manifest.getAssignedDriverId())
                .vehicleId(vehicle == null ? manifest.getVehicleId() : vehicle.getId())
                .vehicleLicensePlate(vehicle == null ? null : vehicle.getLicensePlate())
                .build();
    }

    private void enqueueTransitions(
            Long tenantId,
            String idempotencyKey,
            List<TmsOrderStatusTransitionRequest.Item> items
    ) {
        if (items == null || items.isEmpty()) {
            return;
        }
        tmsOrderTransitionPublisherService.publish(TmsOrderStatusTransitionRequest.builder()
                .source(TRANSITION_SOURCE)
                .idempotencyKey(idempotencyKey)
                .items(items)
                .build(), tenantId);
    }

    private OrderStatus resolveInboundOrderStatus(BagDestinationType destinationType) {
        if (destinationType == BagDestinationType.HUB) {
            return OrderStatus.INBOUND_AT_DESTINATION_HUB;
        }
        return OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE;
    }

    private Long resolveDriverScopedStaffId() {
        if (secondMileAccessUtils.hasHubOperationRole() || !secondMileAccessUtils.isHubDriver()) {
            return null;
        }
        return secondMileAccessUtils.getCurrentActiveDriverStaffIdOrThrow();
    }

    private Vehicle loadVehicle(Long vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        return vehicleRepository.findById(vehicleId).orElse(null);
    }

    private Route loadRoute(Long routeId) {
        if (routeId == null) {
            return null;
        }
        return routeRepository.findById(routeId).orElse(null);
    }

    private Hub loadHub(Long hubId) {
        if (hubId == null) {
            return null;
        }
        return hubRepository.findById(hubId).orElse(null);
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> normalizedIds = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null && id > 0) {
                normalizedIds.add(id);
            }
        }
        return new ArrayList<>(normalizedIds);
    }

    private String generateManifestCode(Long tenantId, Long originHubId) {
        String suffix = LocalDateTime.now().format(MANIFEST_CODE_SUFFIX_FORMATTER);
        String candidate = String.format("BDM-%d-%d-%s", tenantId, originHubId, suffix);
        if (!manifestRepository.existsByTenantIdAndManifestCodeIgnoreCase(tenantId, candidate)) {
            return candidate;
        }
        return candidate + "-" + System.currentTimeMillis() % 1000;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String idempotencyKey(Object... parts) {
        return java.util.Arrays.stream(parts)
                .filter(Objects::nonNull)
                .map(part -> part.toString().trim())
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(":"));
    }

    private int safeCurrentOrders(Bag bag) {
        return bag == null || bag.getCurrentOrders() == null || bag.getCurrentOrders() < 0 ? 0 : bag.getCurrentOrders();
    }

    private double safeCurrentWeight(Bag bag) {
        return bag == null || bag.getCurrentWeight() == null || bag.getCurrentWeight() < 0 ? 0.0 : bag.getCurrentWeight();
    }

    private double safeCurrentVolume(Bag bag) {
        return bag == null || bag.getCurrentVolume() == null || bag.getCurrentVolume() < 0 ? 0.0 : bag.getCurrentVolume();
    }

    private record CreatedManifest(
            BagDistributionManifest manifest,
            List<BagDistributionManifestBag> manifestBags,
            Vehicle vehicle,
            Route route
    ) {
    }
}
