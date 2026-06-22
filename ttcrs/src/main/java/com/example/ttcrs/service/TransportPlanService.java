package com.example.ttcrs.service;

import com.example.ttcrs.constant.RequestStatus;
import com.example.ttcrs.constant.RequestType;
import com.example.ttcrs.constant.StopAction;
import com.example.ttcrs.constant.TransportPlanStatus;
import com.example.ttcrs.dto.request.transportplan.SaveTransportPlanDTO;
import com.example.ttcrs.dto.response.TransportPlanDetailDTO;
import com.example.ttcrs.dto.response.TransportPlanResponseDTO;
import com.example.ttcrs.entity.LocationEntity;
import com.example.ttcrs.entity.RequestEntity;
import com.example.ttcrs.entity.TransportPlanEntity;
import com.example.ttcrs.entity.TransportPlanStopEntity;
import com.example.ttcrs.entity.TruckEntity;
import com.example.ttcrs.infrastructure.client.AccountClientAdapter;
import com.example.ttcrs.infrastructure.client.dto.AccountUserDTO;
import com.example.ttcrs.repository.*;
import com.example.ttcrs.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportPlanService {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TransportPlanRepository     transportPlanRepository;
    private final TransportPlanStopRepository transportPlanStopRepository;
    private final TruckRepository             truckRepository;
    private final TrailerRepository           trailerRepository;
    private final RequestRepository           requestRepository;
    private final LocationRepository          locationRepository;
    private final AccountClientAdapter        accountClientAdapter;
    private final AuthUtils                   authUtils;

    // =========================================================================
    // Save / List / Detail (existing)
    // =========================================================================

    @Transactional
    public List<TransportPlanResponseDTO> savePlans(SaveTransportPlanDTO dto) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));

        Map<String, TruckEntity> truckByCode = truckRepository.findAllByTenantId(tenantId)
                .stream().collect(Collectors.toMap(TruckEntity::getCode, Function.identity(), (a, b) -> a));

        Map<Long, AccountUserDTO> driverById = accountClientAdapter.getDriverUsers(tenantId)
                .stream().collect(Collectors.toMap(AccountUserDTO::getId, Function.identity(), (a, b) -> a));

        List<TransportPlanResponseDTO> results = new ArrayList<>();

        for (SaveTransportPlanDTO.PlanItemDTO planItem : dto.getPlans()) {
            TruckEntity truck = truckByCode.get(planItem.getTruckCode());
            if (truck == null) {
                throw new IllegalArgumentException("Truck not found: " + planItem.getTruckCode());
            }

            if (planItem.getDriverId() == null) {
                throw new IllegalArgumentException(
                        "Driver not assigned for truck: " + planItem.getTruckCode());
            }
            AccountUserDTO driver = driverById.get(planItem.getDriverId());
            if (driver == null) {
                throw new IllegalArgumentException("Driver not found: " + planItem.getDriverId());
            }

            LocalDateTime startTime = planItem.getStops().stream()
                    .filter(s -> s.getPlannedArrival() != null && !s.getPlannedArrival().isBlank())
                    .findFirst()
                    .map(s -> LocalDateTime.parse(s.getPlannedArrival(), DT_FMT))
                    .orElse(null);

            LocalDateTime endTime = null;
            List<SaveTransportPlanDTO.StopDTO> stops = planItem.getStops();
            for (int i = stops.size() - 1; i >= 0; i--) {
                String pa = stops.get(i).getPlannedArrival();
                if (pa != null && !pa.isBlank()) {
                    endTime = LocalDateTime.parse(pa, DT_FMT);
                    break;
                }
            }

            TransportPlanEntity plan = TransportPlanEntity.builder()
                    .tenantId(tenantId)
                    .truckId(truck.getId())
                    .driverId(driver.getId())
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
            plan = transportPlanRepository.save(plan);
            final Long planId = plan.getId();
            Map<Long, String> oeOriginByRequestId = new java.util.HashMap<>();

            List<TransportPlanStopEntity> stopEntities = new ArrayList<>();
            for (SaveTransportPlanDTO.StopDTO stopDto : stops) {
                LocalDateTime plannedArrival = stopDto.getPlannedArrival() != null && !stopDto.getPlannedArrival().isBlank()
                        ? LocalDateTime.parse(stopDto.getPlannedArrival(), DT_FMT)
                        : null;
                StopAction action = mapAction(stopDto.getAction());

                if (stopDto.getRequestId() != null && action == StopAction.PICKUP_CONTAINER) {
                    oeOriginByRequestId.putIfAbsent(stopDto.getRequestId(), stopDto.getLocationCode());
                }

                stopEntities.add(TransportPlanStopEntity.builder()
                        .tenantId(tenantId)
                        .transportPlanId(planId)
                        .sequence(stopDto.getSequence())
                        .locationCode(stopDto.getLocationCode())
                        .requestId(stopDto.getRequestId())
                    .trailerId(stopDto.getTrailerId())
                        .action(action)
                        .plannedArrivalTime(plannedArrival)
                        .build());
            }
            transportPlanStopRepository.saveAll(stopEntities);

            List<Long> requestIds = stops.stream()
                    .map(SaveTransportPlanDTO.StopDTO::getRequestId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            if (!requestIds.isEmpty()) {
                List<RequestEntity> linkedRequests = requestRepository.findAllById(requestIds);
                for (RequestEntity req : linkedRequests) {
                    req.setTransportPlanId(planId);
                }

                Map<Long, RequestEntity> requestById = linkedRequests.stream()
                        .collect(Collectors.toMap(RequestEntity::getId, Function.identity(), (a, b) -> a));
                oeOriginByRequestId.forEach((requestId, originLocationCode) -> {
                    RequestEntity req = requestById.get(requestId);
                    if (req != null
                            && req.getType() == RequestType.OE
                            && originLocationCode != null
                            && !originLocationCode.isBlank()) {
                        req.setSrcLocationCode(originLocationCode);
                    }
                });
                requestRepository.saveAll(linkedRequests);
            }

            results.add(TransportPlanResponseDTO.builder()
                    .id(planId)
                    .truckId(truck.getId())
                    .truckCode(truck.getCode())
                    .driverId(driver.getId())
                    .driverName(driver.getFullName())
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(plan.getStatus())
                    .stopCount(stops.size())
                    .createdStamp(plan.getCreatedStamp())
                    .build());

            log.info("Saved transport plan id={} for truck={}, driverId={}, stops={}",
                    planId, truck.getCode(), driver.getId(), stops.size());
        }

        return results;
    }

    /**
     * Saves manual routes and updates all linked requests to PLANNED.
     *
     * <p>Unlike the auto-plan flow, manual route creation starts from PENDING requests,
     * therefore request status transition is done after successful plan persistence.
     */
    @Transactional
    public List<TransportPlanResponseDTO> saveManualRoute(SaveTransportPlanDTO dto) {
        Long tenantId = requireTenantId();
        List<Long> requestIds = extractRequestIds(dto);
        if (requestIds.isEmpty()) {
            throw new IllegalArgumentException("Manual route must include at least one request stop");
        }

        List<RequestEntity> requests = requestRepository.findAllById(requestIds);
        if (requests.size() != requestIds.size()) {
            throw new IllegalArgumentException("Some requests were not found");
        }

        List<RequestEntity> invalid = requests.stream()
                .filter(r -> !tenantId.equals(r.getTenantId()) || r.getStatus() != RequestStatus.PENDING)
                .toList();
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException(
                    "Some requests are not in PENDING status or do not belong to this tenant: "
                            + invalid.stream().map(r -> r.getId().toString()).toList());
        }

        List<TransportPlanResponseDTO> savedPlans = savePlans(dto);

        // savePlans() has already linked transportPlanId, here we complete manual-flow status transition.
        requests.forEach(r -> r.setStatus(RequestStatus.PLANNED));
        requestRepository.saveAll(requests);

        return savedPlans;
    }

    @Transactional(readOnly = true)
    public List<TransportPlanResponseDTO> getPlans() {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));

        List<TransportPlanEntity> plans =
                transportPlanRepository.findAllByTenantIdOrderByCreatedStampDesc(tenantId);

        if (plans.isEmpty()) return List.of();

        List<Long> planIds = plans.stream().map(TransportPlanEntity::getId).toList();
        Map<Long, Long> stopCounts = transportPlanStopRepository.countByPlanIds(planIds);

        Map<Long, TruckEntity> trucksById = truckRepository.findAllById(
                        plans.stream().map(TransportPlanEntity::getTruckId).distinct().toList())
                .stream().collect(Collectors.toMap(TruckEntity::getId, Function.identity()));

        Map<Long, AccountUserDTO> driversById = accountClientAdapter.getDriverUsers(tenantId)
                .stream().collect(Collectors.toMap(AccountUserDTO::getId, Function.identity()));

        return plans.stream().map(p -> {
            TruckEntity truck = trucksById.get(p.getTruckId());
            AccountUserDTO driver = p.getDriverId() != null ? driversById.get(p.getDriverId()) : null;
            return TransportPlanResponseDTO.builder()
                    .id(p.getId())
                    .truckId(p.getTruckId())
                    .truckCode(truck != null ? truck.getCode() : null)
                    .driverId(p.getDriverId())
                    .driverName(driver != null ? driver.getFullName() : null)
                    .startTime(p.getStartTime())
                    .endTime(p.getEndTime())
                    .status(p.getStatus())
                    .stopCount(stopCounts.getOrDefault(p.getId(), 0L).intValue())
                    .createdStamp(p.getCreatedStamp())
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<TransportPlanResponseDTO> getPlansByDriver() {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));
        Long driverId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not found in JWT"));

        List<TransportPlanEntity> plans =
                transportPlanRepository.findAllByTenantIdAndDriverIdOrderByStartTimeDesc(tenantId, driverId);

        if (plans.isEmpty()) return List.of();

        List<Long> planIds = plans.stream().map(TransportPlanEntity::getId).toList();
        Map<Long, Long> stopCounts = transportPlanStopRepository.countByPlanIds(planIds);

        Map<Long, TruckEntity> trucksById = truckRepository.findAllById(
                        plans.stream().map(TransportPlanEntity::getTruckId).distinct().toList())
                .stream().collect(Collectors.toMap(TruckEntity::getId, Function.identity()));

        AccountUserDTO driver = accountClientAdapter.getUserById(driverId);

        return plans.stream().map(p -> {
            TruckEntity truck = trucksById.get(p.getTruckId());
            return TransportPlanResponseDTO.builder()
                    .id(p.getId())
                    .truckId(p.getTruckId())
                    .truckCode(truck != null ? truck.getCode() : null)
                    .driverId(driverId)
                    .driverName(driver != null ? driver.getFullName() : null)
                    .startTime(p.getStartTime())
                    .endTime(p.getEndTime())
                    .status(p.getStatus())
                    .stopCount(stopCounts.getOrDefault(p.getId(), 0L).intValue())
                    .createdStamp(p.getCreatedStamp())
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public TransportPlanDetailDTO getPlanDetailForDriver(Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));
        Long driverId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not found in JWT"));

        TransportPlanEntity plan = transportPlanRepository.findByIdAndTenantIdAndDriverId(id, tenantId, driverId)
                .orElseThrow(() -> new IllegalArgumentException("Transport plan not found: " + id));

        return buildDetailDTO(plan, tenantId, driverId);
    }

    @Transactional(readOnly = true)
    public TransportPlanDetailDTO getPlanDetail(Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));

        TransportPlanEntity plan = transportPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Transport plan not found: " + id));

        return buildDetailDTO(plan, tenantId, plan.getDriverId());
    }

    // =========================================================================
    // Driver execution actions
    // =========================================================================

    /** CREATED → EXECUTING. Auto-arrives at DEPOT_START. Validates single-executing constraint. */
    @Transactional
    public TransportPlanDetailDTO startRoute(Long id) {
        Long tenantId = requireTenantId();
        Long driverId = requireDriverId();

        TransportPlanEntity plan = requirePlanForDriver(id, tenantId, driverId);

        if (plan.getStatus() != TransportPlanStatus.CREATED) {
            throw new IllegalStateException("Route must be in CREATED status to start");
        }
        if (transportPlanRepository.existsByDriverIdAndStatus(driverId, TransportPlanStatus.EXECUTING)) {
            throw new IllegalStateException("Driver already has an executing route");
        }

        LocalDateTime now = LocalDateTime.now();
        plan.setStatus(TransportPlanStatus.EXECUTING);
        plan.setActualStartTime(now);

        // Auto-arrive at the first stop (DEPOT_START)
        List<TransportPlanStopEntity> stops =
                transportPlanStopRepository.findAllByTransportPlanIdOrderBySequenceAsc(id);
        if (!stops.isEmpty()) {
            TransportPlanStopEntity first = stops.get(0);
            first.setActualArrivalTime(now);
            first.setIsCompleted(true);
            transportPlanStopRepository.save(first);
            plan.setCurrentStop(first.getSequence());
        }

        transportPlanRepository.save(plan);

        // All linked requests → IN_PROGRESS
        List<RequestEntity> linked = requestRepository.findAllByTransportPlanId(id);
        linked.forEach(r -> r.setStatus(RequestStatus.IN_PROGRESS));
        requestRepository.saveAll(linked);

        log.info("Route {} started by driver {}", id, driverId);
        return buildDetailDTO(plan, tenantId, driverId);
    }

    /** CREATED → CANCELLED (dispatcher). */
    @Transactional
    public TransportPlanDetailDTO cancelRoute(Long id, String reason) {
        Long tenantId = requireTenantId();

        TransportPlanEntity plan = transportPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Transport plan not found: " + id));

        if (plan.getStatus() != TransportPlanStatus.CREATED) {
            throw new IllegalStateException("Route must be in CREATED status to cancel");
        }

        plan.setStatus(TransportPlanStatus.CANCELLED);
        plan.setCancelReason(reason);
        transportPlanRepository.save(plan);

        log.info("Route {} cancelled by dispatcher", id);
        return buildDetailDTO(plan, tenantId, plan.getDriverId());
    }

    /** Driver arrives at a stop: record actual_arrival_time, update current_stop. */
    @Transactional
    public TransportPlanDetailDTO arriveAtStop(Long planId, Integer seq) {
        Long tenantId = requireTenantId();
        Long driverId = requireDriverId();

        TransportPlanEntity plan = requirePlanForDriver(planId, tenantId, driverId);

        if (plan.getStatus() != TransportPlanStatus.EXECUTING) {
            throw new IllegalStateException("Route is not in EXECUTING status");
        }

        TransportPlanStopEntity stop = transportPlanStopRepository
                .findByTransportPlanIdAndSequence(planId, seq)
                .orElseThrow(() -> new IllegalArgumentException("Stop not found: seq=" + seq));

        stop.setActualArrivalTime(LocalDateTime.now());
        transportPlanStopRepository.save(stop);

        plan.setCurrentStop(seq);
        transportPlanRepository.save(plan);

        log.info("Driver {} arrived at stop seq={} on route {}", driverId, seq, planId);
        return buildDetailDTO(plan, tenantId, driverId);
    }

    /**
     * Driver completes a stop: save evidence, mark stop completed, advance state.
     * If this is the last stop → route becomes COMPLETED.
     */
    @Transactional
    public TransportPlanDetailDTO completeStop(Long planId, Integer seq,
                                               String evidenceUrl, Double totalDistance) {
        Long tenantId = requireTenantId();
        Long driverId = requireDriverId();

        TransportPlanEntity plan = requirePlanForDriver(planId, tenantId, driverId);

        if (plan.getStatus() != TransportPlanStatus.EXECUTING) {
            throw new IllegalStateException("Route is not in EXECUTING status");
        }

        TransportPlanStopEntity stop = transportPlanStopRepository
                .findByTransportPlanIdAndSequence(planId, seq)
                .orElseThrow(() -> new IllegalArgumentException("Stop not found: seq=" + seq));

        // Save evidence for the associated request
        if (stop.getRequestId() != null && evidenceUrl != null && !evidenceUrl.isBlank()) {
            requestRepository.findById(stop.getRequestId()).ifPresent(req -> {
                if (
                        req.getSrcLocationCode() != null
                                && stop.getLocationCode().equalsIgnoreCase(req.getSrcLocationCode())
                ) {
                    req.setEvidenceAtSrc(evidenceUrl);
                } else if (
                        req.getDestLocationCode() != null
                                && stop.getLocationCode().equalsIgnoreCase(req.getDestLocationCode())
                ) {
                    req.setEvidenceAtDest(evidenceUrl);
                }
                requestRepository.save(req);
            });
        }

        stop.setIsCompleted(true);
        transportPlanStopRepository.save(stop);

        updateVehicleLocationsForStop(plan, stop);

        // Check if this is the last stop
        List<TransportPlanStopEntity> allStops =
                transportPlanStopRepository.findAllByTransportPlanIdOrderBySequenceAsc(planId);
        boolean isLastStop = allStops.stream()
                .mapToInt(TransportPlanStopEntity::getSequence)
                .max()
                .orElse(-1) == seq;

        if (isLastStop) {
            LocalDateTime now = LocalDateTime.now();
            plan.setStatus(TransportPlanStatus.COMPLETED);
            plan.setActualEndTime(now);
            if (totalDistance != null) {
                plan.setTotalDistance(totalDistance);
            }
            if (plan.getActualStartTime() != null) {
                long minutes = java.time.Duration.between(plan.getActualStartTime(), now).toMinutes();
                plan.setTotalTime((int) minutes);
            }

            // All requests → COMPLETED
            List<RequestEntity> linked = requestRepository.findAllByTransportPlanId(planId);
            linked.forEach(r -> r.setStatus(RequestStatus.COMPLETED));
            requestRepository.saveAll(linked);

            log.info("Route {} completed by driver {}", planId, driverId);
        }

        transportPlanRepository.save(plan);
        return buildDetailDTO(plan, tenantId, driverId);
    }

    /** CANCELLED → CREATED (dispatcher). Linked requests → PENDING. */
    @Transactional
    public TransportPlanDetailDTO restoreRoute(Long id) {
        Long tenantId = requireTenantId();

        TransportPlanEntity plan = transportPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Transport plan not found: " + id));

        if (plan.getStatus() != TransportPlanStatus.CANCELLED) {
            throw new IllegalStateException("Route must be in CANCELLED status to restore");
        }

        plan.setStatus(TransportPlanStatus.CREATED);
        plan.setCancelReason(null);
        transportPlanRepository.save(plan);

        List<RequestEntity> linked = requestRepository.findAllByTransportPlanId(id);
        linked.forEach(r -> r.setStatus(RequestStatus.PENDING));
        requestRepository.saveAll(linked);

        log.info("Route {} restored by dispatcher", id);
        return buildDetailDTO(plan, tenantId, plan.getDriverId());
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private TransportPlanDetailDTO buildDetailDTO(TransportPlanEntity plan, Long tenantId, Long driverId) {
        TruckEntity truck = truckRepository.findById(plan.getTruckId()).orElse(null);
        AccountUserDTO driver = driverId != null ? accountClientAdapter.getUserById(driverId) : null;

        List<TransportPlanStopEntity> stopEntities =
                transportPlanStopRepository.findAllByTransportPlanIdOrderBySequenceAsc(plan.getId());

        // Build locationCode → entity map for lat/lng enrichment
        Map<String, LocationEntity> locationMap = locationRepository.findAllByTenantId(tenantId)
                .stream().collect(Collectors.toMap(LocationEntity::getLocationCode, Function.identity(), (a, b) -> a));

        // Build requestId → entity map for src/dest location codes
        List<Long> requestIds = stopEntities.stream()
                .map(TransportPlanStopEntity::getRequestId)
                .filter(rid -> rid != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, RequestEntity> requestMap = requestIds.isEmpty()
                ? Map.of()
                : requestRepository.findAllById(requestIds)
                        .stream().collect(Collectors.toMap(RequestEntity::getId, Function.identity()));

        List<TransportPlanDetailDTO.StopDTO> stops = stopEntities.stream()
                .map(s -> {
                    LocationEntity loc = locationMap.get(s.getLocationCode());
                    RequestEntity req = s.getRequestId() != null ? requestMap.get(s.getRequestId()) : null;
                    return TransportPlanDetailDTO.StopDTO.builder()
                            .id(s.getId())
                            .sequence(s.getSequence())
                            .locationCode(s.getLocationCode())
                            .lat(loc != null ? loc.getLat() : null)
                            .lng(loc != null ? loc.getLng() : null)
                            .action(s.getAction())
                            .plannedArrivalTime(s.getPlannedArrivalTime())
                            .actualArrivalTime(s.getActualArrivalTime())
                            .isCompleted(s.getIsCompleted())
                            .requestId(s.getRequestId())
                            .requestSrcLocationCode(req != null ? req.getSrcLocationCode() : null)
                            .requestDestLocationCode(req != null ? req.getDestLocationCode() : null)
                            .evidenceUrl(req != null ? resolveEvidenceUrl(s.getLocationCode(), req) : null)
                            .build();
                })
                .toList();

        return TransportPlanDetailDTO.builder()
                .id(plan.getId())
                .truckId(plan.getTruckId())
                .truckCode(truck != null ? truck.getCode() : null)
                .driverId(plan.getDriverId())
                .driverName(driver != null ? driver.getFullName() : null)
                .startTime(plan.getStartTime())
                .endTime(plan.getEndTime())
                .status(plan.getStatus())
                .createdStamp(plan.getCreatedStamp())
                .stops(stops)
                .cancelReason(plan.getCancelReason())
                .currentStop(plan.getCurrentStop())
                .actualStartTime(plan.getActualStartTime())
                .actualEndTime(plan.getActualEndTime())
                .totalDistance(plan.getTotalDistance())
                .totalTime(plan.getTotalTime())
                .build();
    }

    private String resolveEvidenceUrl(String locationCode, RequestEntity req) {
        if (locationCode == null) return null;
        if (
                req.getSrcLocationCode() != null
                        && locationCode.equalsIgnoreCase(req.getSrcLocationCode())
        ) {
            return req.getEvidenceAtSrc();
        }
        if (
                req.getDestLocationCode() != null
                        && locationCode.equalsIgnoreCase(req.getDestLocationCode())
        ) {
            return req.getEvidenceAtDest();
        }
        return null;
    }

    private Long requireTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));
    }

    private Long requireDriverId() {
        return authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not found in JWT"));
    }

    private TransportPlanEntity requirePlanForDriver(Long id, Long tenantId, Long driverId) {
        return transportPlanRepository.findByIdAndTenantIdAndDriverId(id, tenantId, driverId)
                .orElseThrow(() -> new IllegalArgumentException("Transport plan not found: " + id));
    }

    private StopAction mapAction(String raw) {
        if (raw == null) return StopAction.DEPOT_START;
        return switch (raw.toUpperCase()) {
            case "START_TRUCK"                                      -> StopAction.DEPOT_START;
            case "END_TRUCK"                                        -> StopAction.DEPOT_END;
            case "PICKUP_MOOC"                                      -> StopAction.PICKUP_TRAILER;
            case "DELIVERY_MOOC"                                    -> StopAction.DROP_TRAILER;
            case "WH_PICKUP_EMPTYCONT", "WH_PICKUP_FULLCONT",
                 "PORT_PICKUP_EMPTYCONT", "PORT_PICKUP_FULLCONT",
                 "PICKUP_EMPTYCONT"                                 -> StopAction.PICKUP_CONTAINER;
            case "WH_DELIVERY_EMPTYCONT", "WH_DELIVERY_FULLCONT",
                 "PORT_DELIVERY_EMPTYCONT", "PORT_DELIVERY_FULLCONT",
                 "DELIVERY_EMPTYCONT"                               -> StopAction.DELIVERY_CONTAINER;
            default -> {
                log.warn("Unrecognised stop action '{}', defaulting to DEPOT_START", raw);
                yield StopAction.DEPOT_START;
            }
        };
    }

    private void updateVehicleLocationsForStop(TransportPlanEntity plan, TransportPlanStopEntity stop) {
        if (stop.getAction() == StopAction.DEPOT_END) {
            truckRepository.findById(plan.getTruckId()).ifPresent(truck -> {
                truck.setCurrentLocationCode(stop.getLocationCode());
                truckRepository.save(truck);
            });
        }

        if (stop.getAction() == StopAction.DROP_TRAILER && stop.getTrailerId() != null) {
            trailerRepository.findById(stop.getTrailerId()).ifPresent(trailer -> {
                trailer.setCurrentLocationCode(stop.getLocationCode());
                trailerRepository.save(trailer);
            });
        }
    }

    private List<Long> extractRequestIds(SaveTransportPlanDTO dto) {
        return dto.getPlans().stream()
                .flatMap(plan -> plan.getStops().stream())
                .map(SaveTransportPlanDTO.StopDTO::getRequestId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }
}
