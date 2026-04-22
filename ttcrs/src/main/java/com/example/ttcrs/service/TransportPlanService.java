package com.example.ttcrs.service;

import com.example.ttcrs.constant.RequestStatus;
import com.example.ttcrs.constant.StopAction;
import com.example.ttcrs.dto.request.transportplan.SaveTransportPlanDTO;
import com.example.ttcrs.dto.response.TransportPlanDetailDTO;
import com.example.ttcrs.dto.response.TransportPlanResponseDTO;
import com.example.ttcrs.entity.RequestEntity;
import com.example.ttcrs.entity.TransportPlanEntity;
import com.example.ttcrs.entity.TransportPlanStopEntity;
import com.example.ttcrs.entity.DriverEntity;
import com.example.ttcrs.entity.TruckEntity;
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
    private final DriverRepository            driverRepository;
    private final RequestRepository           requestRepository;
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

        // Pre-load all trucks and drivers into maps for efficient lookup
        Map<String, TruckEntity> truckByCode = truckRepository.findAllByTenantId(tenantId)
                .stream().collect(Collectors.toMap(TruckEntity::getCode, Function.identity(), (a, b) -> a));

        Map<String, DriverEntity> driverByName = driverRepository.findAllByTenantId(tenantId)
                .stream().collect(Collectors.toMap(DriverEntity::getName, Function.identity(), (a, b) -> a));

        List<TransportPlanResponseDTO> results = new ArrayList<>();

        for (SaveTransportPlanDTO.PlanItemDTO planItem : dto.getPlans()) {
            TruckEntity truck = truckByCode.get(planItem.getTruckCode());
            if (truck == null) {
                throw new IllegalArgumentException("Truck not found: " + planItem.getTruckCode());
            }

            if (planItem.getDriverName() == null || planItem.getDriverName().isBlank()) {
                throw new IllegalArgumentException(
                        "Driver not assigned for truck: " + planItem.getTruckCode());
            }
            DriverEntity driver = driverByName.get(planItem.getDriverName());
            if (driver == null) {
                throw new IllegalArgumentException("Driver not found: " + planItem.getDriverName());
            }

            // Derive plan start/end from first and last stop's plannedArrival
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

            // Persist stops
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

            // Link requests to this plan
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
                    .driverName(driver.getName())
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(plan.getStatus())
                    .stopCount(stops.size())
                    .build());

            log.info("Saved transport plan id={} for truck={}, driver={}, stops={}",
                    planId, truck.getCode(), driver.getName(), stops.size());
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

        Map<Long, DriverEntity> driversById = driverRepository.findAllById(
                        plans.stream().map(TransportPlanEntity::getDriverId).filter(java.util.Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(DriverEntity::getId, Function.identity()));

        return plans.stream().map(p -> {
            TruckEntity  truck  = trucksById.get(p.getTruckId());
            DriverEntity driver = p.getDriverId() != null ? driversById.get(p.getDriverId()) : null;
            return TransportPlanResponseDTO.builder()
                    .id(p.getId())
                    .truckId(p.getTruckId())
                    .truckCode(truck  != null ? truck.getCode()    : null)
                    .driverId(p.getDriverId())
                    .driverName(driver != null ? driver.getName() : null)
                    .startTime(p.getStartTime())
                    .endTime(p.getEndTime())
                    .status(p.getStatus())
                    .stopCount(stopCounts.getOrDefault(p.getId(), 0L).intValue())
                    .build();
        }).toList();
    }

    /** Returns the full detail of one transport plan (with stops). */
    @Transactional(readOnly = true)
    public TransportPlanDetailDTO getPlanDetail(Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));

        TransportPlanEntity plan = transportPlanRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Transport plan not found: " + id));

        TruckEntity truck = truckRepository.findById(plan.getTruckId()).orElse(null);
        DriverEntity driver = plan.getDriverId() != null
                ? driverRepository.findById(plan.getDriverId()).orElse(null)
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
                .truckCode(truck  != null ? truck.getCode()    : null)
                .driverId(plan.getDriverId())
                .driverName(driver != null ? driver.getName() : null)
                .startTime(plan.getStartTime())
                .endTime(plan.getEndTime())
                .status(plan.getStatus())
                .createdStamp(plan.getCreatedStamp())
                .stops(stops)
                .build();
    }

    /**
     * Maps algorithm action strings to the {@link StopAction} enum.
     * Any unrecognised string falls through to {@code DEPOT_START} as a safe default.
     */
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
