package com.example.ttcrs.service;

import com.example.ttcrs.constant.RequestStatus;
import com.example.ttcrs.constant.StopAction;
import com.example.ttcrs.dto.request.transportplan.SaveTransportPlanDTO;
import com.example.ttcrs.dto.response.TransportPlanDetailDTO;
import com.example.ttcrs.dto.response.TransportPlanResponseDTO;
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
    private final RequestRepository           requestRepository;
    private final AccountClientAdapter        accountClientAdapter;
    private final AuthUtils                   authUtils;

    /**
     * Persists the reviewed transport plan (Step 3 "Finish").
     * One {@link TransportPlanEntity} per truck route,
     * one {@link TransportPlanStopEntity} per stop.
     * Also links each operational request to its transport plan.
     */
    @Transactional
    public List<TransportPlanResponseDTO> savePlans(SaveTransportPlanDTO dto) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));

        Map<String, TruckEntity> truckByCode = truckRepository.findAllByTenantId(tenantId)
                .stream().collect(Collectors.toMap(TruckEntity::getCode, Function.identity(), (a, b) -> a));

        // Pre-load all TTCRS_DRIVER users for this tenant into a map
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

            List<TransportPlanStopEntity> stopEntities = new ArrayList<>();
            for (SaveTransportPlanDTO.StopDTO stopDto : stops) {
                LocalDateTime plannedArrival = stopDto.getPlannedArrival() != null && !stopDto.getPlannedArrival().isBlank()
                        ? LocalDateTime.parse(stopDto.getPlannedArrival(), DT_FMT)
                        : null;

                stopEntities.add(TransportPlanStopEntity.builder()
                        .tenantId(tenantId)
                        .transportPlanId(planId)
                        .sequence(stopDto.getSequence())
                        .locationCode(stopDto.getLocationCode())
                        .requestId(stopDto.getRequestId())
                        .action(mapAction(stopDto.getAction()))
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

    /** Returns all transport plans for the current tenant, newest first. */
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

        // Load all TTCRS_DRIVER users once, then look up by userId
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

    /** Returns all transport plans assigned to the currently authenticated driver. */
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

    /** Returns the full detail of one transport plan, validating that it belongs to the current driver. */
    @Transactional(readOnly = true)
    public TransportPlanDetailDTO getPlanDetailForDriver(Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));
        Long driverId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not found in JWT"));

        TransportPlanEntity plan = transportPlanRepository.findByIdAndTenantIdAndDriverId(id, tenantId, driverId)
                .orElseThrow(() -> new IllegalArgumentException("Transport plan not found: " + id));

        TruckEntity truck = truckRepository.findById(plan.getTruckId()).orElse(null);
        AccountUserDTO driver = accountClientAdapter.getUserById(driverId);

        List<TransportPlanStopEntity> stopEntities =
                transportPlanStopRepository.findAllByTransportPlanIdOrderBySequenceAsc(id);

        List<TransportPlanDetailDTO.StopDTO> stops = stopEntities.stream()
                .map(s -> TransportPlanDetailDTO.StopDTO.builder()
                        .id(s.getId())
                        .sequence(s.getSequence())
                        .locationCode(s.getLocationCode())
                        .action(s.getAction())
                        .plannedArrivalTime(s.getPlannedArrivalTime())
                        .actualArrivalTime(s.getActualArrivalTime())
                        .requestId(s.getRequestId())
                        .build())
                .toList();

        return TransportPlanDetailDTO.builder()
                .id(plan.getId())
                .truckId(plan.getTruckId())
                .truckCode(truck != null ? truck.getCode() : null)
                .driverId(driverId)
                .driverName(driver != null ? driver.getFullName() : null)
                .startTime(plan.getStartTime())
                .endTime(plan.getEndTime())
                .status(plan.getStatus())
                .createdStamp(plan.getCreatedStamp())
                .stops(stops)
                .build();
    }

    /** Returns the full detail of one transport plan (with stops). */
    @Transactional(readOnly = true)
    public TransportPlanDetailDTO getPlanDetail(Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));

        TransportPlanEntity plan = transportPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Transport plan not found: " + id));

        TruckEntity truck = truckRepository.findById(plan.getTruckId()).orElse(null);
        AccountUserDTO driver = plan.getDriverId() != null
                ? accountClientAdapter.getUserById(plan.getDriverId())
                : null;

        List<TransportPlanStopEntity> stopEntities =
                transportPlanStopRepository.findAllByTransportPlanIdOrderBySequenceAsc(id);

        List<TransportPlanDetailDTO.StopDTO> stops = stopEntities.stream()
                .map(s -> TransportPlanDetailDTO.StopDTO.builder()
                        .id(s.getId())
                        .sequence(s.getSequence())
                        .locationCode(s.getLocationCode())
                        .action(s.getAction())
                        .plannedArrivalTime(s.getPlannedArrivalTime())
                        .actualArrivalTime(s.getActualArrivalTime())
                        .requestId(s.getRequestId())
                        .build())
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
                .build();
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
}
