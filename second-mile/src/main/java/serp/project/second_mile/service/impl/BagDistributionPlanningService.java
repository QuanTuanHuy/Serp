/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.request.AutoPlanBagDistributionRequest;
import serp.project.second_mile.dto.response.BagDistributionPlanItemResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagDistributionManifestStatus;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.HandoverManifestStatus;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.BagDistributionManifestBagRepository;
import serp.project.second_mile.repository.BagDistributionManifestRepository;
import serp.project.second_mile.repository.BagRepository;
import serp.project.second_mile.repository.HandoverManifestRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BagDistributionPlanningService {
    private static final int DEFAULT_SEALED_SLA_HOURS = 24;
    private static final List<BagStatus> DISPATCH_READY_BAG_STATUSES = List.of(
            BagStatus.SEALED,
            BagStatus.ARRIVED
    );
    private static final List<BagDistributionManifestStatus> ACTIVE_MANIFEST_STATUSES = List.of(
            BagDistributionManifestStatus.CREATED,
            BagDistributionManifestStatus.OUTBOUND_CONFIRMED
    );
    private static final List<HandoverManifestStatus> ACTIVE_HANDOVER_STATUSES = List.of(
            HandoverManifestStatus.CREATED,
            HandoverManifestStatus.OUTBOUND_CONFIRMED
    );
    private static final Set<String> BLOCKING_HINTS = Set.of("NO_ROUTE", "NO_DRIVER", "SCHEDULE_CONFLICT");

    private final BagRepository bagRepository;
    private final BagDistributionManifestRepository manifestRepository;
    private final BagDistributionManifestBagRepository manifestBagRepository;
    private final HandoverManifestRepository handoverManifestRepository;
    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final HubStaffAssignmentRepository hubStaffAssignmentRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;

    public List<BagDistributionPlanItemResponse> plan(Long tenantId, AutoPlanBagDistributionRequest request) {
        String destinationPostOfficeCode = normalizeText(request.getDestinationPostOfficeCode());
        List<Bag> candidateBags = findCandidateBags(
                tenantId,
                request.getOriginHubId(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                destinationPostOfficeCode,
                normalizeIds(request.getBagIds())
        );
        if (candidateBags.isEmpty()) {
            return List.of();
        }

        int slaHours = request.getSealedSlaHours() == null || request.getSealedSlaHours() <= 0
                ? DEFAULT_SEALED_SLA_HOURS
                : request.getSealedSlaHours();
        Map<DestinationKey, List<Bag>> bagsByDestination = candidateBags.stream()
                .collect(Collectors.groupingBy(
                        this::destinationKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<BagDistributionPlanItemResponse> planItems = new ArrayList<>();
        for (Map.Entry<DestinationKey, List<Bag>> entry : bagsByDestination.entrySet()) {
            planItems.addAll(planDestinationGroup(
                    tenantId,
                    entry.getKey(),
                    entry.getValue(),
                    request,
                    slaHours
            ));
        }
        return planItems;
    }

    public boolean hasBlockingHint(List<String> hints) {
        if (hints == null || hints.isEmpty()) {
            return false;
        }
        return hints.stream().anyMatch(BLOCKING_HINTS::contains);
    }

    private List<BagDistributionPlanItemResponse> planDestinationGroup(
            Long tenantId,
            DestinationKey destinationKey,
            List<Bag> candidateBags,
            AutoPlanBagDistributionRequest request,
            int slaHours
    ) {
        List<Route> routes = findMatchingRoutes(tenantId, destinationKey);
        if (routes.isEmpty()) {
            return List.of(toBlockedPlanItem(destinationKey, candidateBags, request, List.of("NO_ROUTE")));
        }

        List<RouteOption> routeOptions = new ArrayList<>();
        List<String> blockedHints = new ArrayList<>();
        for (Route route : routes) {
            List<Vehicle> vehicles = resolveRouteVehicles(tenantId, destinationKey.originHubId(), route);
            if (vehicles.isEmpty()) {
                blockedHints.add("NO_DRIVER");
            }
            for (Vehicle vehicle : vehicles) {
                routeOptions.add(toRouteOption(tenantId, destinationKey.originHubId(), route, vehicle, request));
            }
        }

        List<RouteOption> availableOptions = routeOptions.stream()
                .filter(option -> option.blockingHints().isEmpty())
                .sorted(Comparator.comparingDouble(RouteOption::baseScore).reversed())
                .toList();
        if (availableOptions.isEmpty()) {
            List<String> hints = routeOptions.stream()
                    .flatMap(option -> option.blockingHints().stream())
                    .collect(Collectors.toCollection(ArrayList::new));
            hints.addAll(blockedHints);
            if (hints.isEmpty()) {
                hints.add("SCHEDULE_CONFLICT");
            }
            return List.of(toBlockedPlanItem(destinationKey, candidateBags, request, distinctHints(hints)));
        }

        List<Bag> sortedBags = candidateBags.stream()
                .sorted(Comparator
                        .comparingDouble(this::bagSizeScore)
                        .reversed()
                        .thenComparing(Bag::getSealedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Bag::getId))
                .toList();
        List<PlanBin> bins = new ArrayList<>();
        for (Bag bag : sortedBags) {
            boolean placed = false;
            for (PlanBin bin : bins) {
                if (bin.canFit(bag)) {
                    bin.add(bag);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                RouteOption option = firstUnusedOptionThatFits(availableOptions, bins, bag);
                if (option == null) {
                    PlanBin fallbackBin = new PlanBin(availableOptions.get(0));
                    fallbackBin.add(bag);
                    fallbackBin.hints.add("CAPACITY_RISK");
                    fallbackBin.hints.add("SCHEDULE_CONFLICT");
                    bins.add(fallbackBin);
                } else {
                    PlanBin newBin = new PlanBin(option);
                    newBin.add(bag);
                    bins.add(newBin);
                }
            }
        }

        return bins.stream()
                .map(bin -> toPlanItem(destinationKey, bin, request, slaHours))
                .toList();
    }

    private List<Bag> findCandidateBags(
            Long tenantId,
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode,
            List<Long> requestedBagIds
    ) {
        Set<Long> requestedBagIdSet = requestedBagIds.isEmpty()
                ? Set.of()
                : new LinkedHashSet<>(requestedBagIds);
        List<Bag> readyBags = bagRepository.findByTenantIdAndOriginHubIdAndStatusIn(
                tenantId,
                originHubId,
                DISPATCH_READY_BAG_STATUSES
        );
        List<Long> readyBagIds = readyBags.stream().map(Bag::getId).toList();
        Set<Long> activeBagIds = readyBagIds.isEmpty()
                ? Set.of()
                : new LinkedHashSet<>(manifestBagRepository.findActiveBagIds(
                tenantId,
                readyBagIds,
                ACTIVE_MANIFEST_STATUSES
        ));
        return readyBags.stream()
                .filter(bag -> requestedBagIdSet.isEmpty() || requestedBagIdSet.contains(bag.getId()))
                .filter(bag -> !activeBagIds.contains(bag.getId()))
                .filter(bag -> destinationType == null || bag.getDestinationType() == destinationType)
                .filter(bag -> destinationHubId == null || Objects.equals(bag.getDestinationHubId(), destinationHubId))
                .filter(bag -> destinationPostOfficeCode == null
                        || Objects.equals(normalizeText(bag.getDestinationPostOfficeCode()), destinationPostOfficeCode))
                .sorted(Comparator
                        .comparing(Bag::getSealedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Comparator.comparingInt(this::safeCurrentOrders).reversed())
                        .thenComparing(Comparator.comparingDouble(this::safeCurrentWeight).reversed())
                        .thenComparing(Bag::getId))
                .toList();
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

    private List<Route> findMatchingRoutes(Long tenantId, DestinationKey key) {
        if (key.destinationType() == BagDestinationType.HUB) {
            return routeRepository.findByTenantIdAndStatusAndOriginTypeAndOriginHubIdAndDestinationTypeAndDestinationHubId(
                    tenantId,
                    RouteStatus.ACTIVE,
                    RouteEndpointType.HUB,
                    key.originHubId(),
                    RouteDestinationType.HUB,
                    key.destinationHubId()
            );
        }
        return routeRepository.findByTenantIdAndStatusAndOriginTypeAndOriginHubIdAndDestinationTypeAndDestinationPostOfficeCodeIgnoreCase(
                tenantId,
                RouteStatus.ACTIVE,
                RouteEndpointType.HUB,
                key.originHubId(),
                RouteDestinationType.POST_OFFICE,
                key.destinationPostOfficeCode()
        );
    }

    private List<Vehicle> resolveRouteVehicles(Long tenantId, Long originHubId, Route route) {
        if (route.getVehicleId() != null) {
            return vehicleRepository.findById(route.getVehicleId())
                    .filter(vehicle -> Objects.equals(vehicle.getTenantId(), tenantId))
                    .filter(vehicle -> Objects.equals(vehicle.getHubId(), originHubId))
                    .filter(vehicle -> vehicle.getStatus() == VehicleStatus.ACTIVE)
                    .map(List::of)
                    .orElseGet(List::of);
        }
        return vehicleRepository.findByTenantIdAndHubIdAndStatus(tenantId, originHubId, VehicleStatus.ACTIVE);
    }

    private RouteOption toRouteOption(
            Long tenantId,
            Long originHubId,
            Route route,
            Vehicle vehicle,
            AutoPlanBagDistributionRequest request
    ) {
        List<String> blockingHints = new ArrayList<>();
        if (vehicle.getAssignedStaffId() == null) {
            blockingHints.add("NO_DRIVER");
        } else {
            try {
                validateVehicleHasAssignedDriver(tenantId, vehicle);
                validateDriverAssignedToHub(tenantId, vehicle.getAssignedStaffId(), originHubId);
            } catch (AppException exception) {
                blockingHints.add("NO_DRIVER");
            }
        }
        if (!blockingHints.contains("NO_DRIVER") && hasScheduleConflict(
                tenantId,
                vehicle.getId(),
                vehicle.getAssignedStaffId(),
                request.getPlannedDepartureAt(),
                request.getPlannedArrivalAt()
        )) {
            blockingHints.add("SCHEDULE_CONFLICT");
        }
        double scheduleScore = scheduleScore(route.getFixedDepartureTime(), request.getPlannedDepartureAt().toLocalTime());
        double driverScore = blockingHints.contains("NO_DRIVER") ? 0.0 : 20.0;
        return new RouteOption(route, vehicle, distinctHints(blockingHints), 100.0 + scheduleScore + driverScore);
    }

    private RouteOption firstUnusedOptionThatFits(List<RouteOption> options, List<PlanBin> bins, Bag bag) {
        Set<Long> usedVehicleIds = bins.stream()
                .map(bin -> bin.option.vehicle().getId())
                .collect(Collectors.toSet());
        for (RouteOption option : options) {
            if (usedVehicleIds.contains(option.vehicle().getId())) {
                continue;
            }
            PlanBin probe = new PlanBin(option);
            if (probe.canFit(bag)) {
                return option;
            }
        }
        return null;
    }

    private BagDistributionPlanItemResponse toPlanItem(
            DestinationKey destinationKey,
            PlanBin bin,
            AutoPlanBagDistributionRequest request,
            int slaHours
    ) {
        List<String> hints = new ArrayList<>(bin.hints);
        double capacityUsage = capacityUsage(bin.option.vehicle(), bin.totalWeight, bin.totalVolume);
        if (capacityUsage > 0.9) {
            hints.add("CAPACITY_RISK");
        } else if (capacityUsage < 0.4) {
            hints.add("LOW_UTILIZATION");
        }
        if (hasHighPriorityBag(bin.bags, slaHours)) {
            hints.add("HIGH_PRIORITY");
        }

        double vehicleFitScore = vehicleFitScore(capacityUsage);
        double delayPenalty = delayPenalty(bin.bags, slaHours);
        double score = clamp(bin.option.baseScore() + vehicleFitScore - delayPenalty, 0.0, 180.0);
        Route route = bin.option.route();
        Vehicle vehicle = bin.option.vehicle();
        return new BagDistributionPlanItemResponse(
                destinationKey.originHubId(),
                destinationKey.destinationType(),
                destinationKey.destinationHubId(),
                destinationKey.destinationPostOfficeCode(),
                route.getId(),
                route.getRouteCode(),
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getAssignedStaffId(),
                request.getPlannedDepartureAt(),
                request.getPlannedArrivalAt(),
                bin.bags.stream().map(Bag::getId).toList(),
                bin.bags.stream().map(Bag::getBagCode).toList(),
                bin.totalWeight,
                bin.totalVolume,
                bin.totalOrders,
                score,
                distinctHints(hints),
                null,
                null
        );
    }

    private BagDistributionPlanItemResponse toBlockedPlanItem(
            DestinationKey destinationKey,
            List<Bag> bags,
            AutoPlanBagDistributionRequest request,
            List<String> hints
    ) {
        return new BagDistributionPlanItemResponse(
                destinationKey.originHubId(),
                destinationKey.destinationType(),
                destinationKey.destinationHubId(),
                destinationKey.destinationPostOfficeCode(),
                null,
                null,
                null,
                null,
                null,
                request.getPlannedDepartureAt(),
                request.getPlannedArrivalAt(),
                bags.stream().map(Bag::getId).toList(),
                bags.stream().map(Bag::getBagCode).toList(),
                bags.stream().mapToDouble(this::safeCurrentWeight).sum(),
                bags.stream().mapToDouble(this::safeCurrentVolume).sum(),
                bags.stream().mapToInt(this::safeCurrentOrders).sum(),
                0.0,
                distinctHints(hints),
                null,
                null
        );
    }

    private void validateVehicleHasAssignedDriver(Long tenantId, Vehicle vehicle) {
        secondMileAccessUtils.ensureActiveDriverStaffOrThrow(tenantId, vehicle.getAssignedStaffId());
    }

    private void validateDriverAssignedToHub(Long tenantId, Long driverStaffId, Long hubId) {
        boolean assignedToHub = hubStaffAssignmentRepository
                .findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(
                        driverStaffId,
                        hubId,
                        tenantId,
                        LocalDate.now()
                )
                .isPresent();
        if (!assignedToHub) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private boolean hasScheduleConflict(
            Long tenantId,
            Long vehicleId,
            Long assignedDriverId,
            LocalDateTime plannedDepartureAt,
            LocalDateTime plannedArrivalAt
    ) {
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
                null
        );
    }

    private DestinationKey destinationKey(Bag bag) {
        return new DestinationKey(
                bag.getOriginHubId(),
                bag.getDestinationType(),
                bag.getDestinationHubId(),
                normalizeText(bag.getDestinationPostOfficeCode())
        );
    }

    private double bagSizeScore(Bag bag) {
        return Math.max(safeCurrentWeight(bag), safeCurrentVolume(bag));
    }

    private boolean hasHighPriorityBag(List<Bag> bags, int slaHours) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(slaHours);
        return bags.stream()
                .map(Bag::getSealedAt)
                .filter(Objects::nonNull)
                .anyMatch(sealedAt -> sealedAt.isBefore(threshold));
    }

    private double delayPenalty(List<Bag> bags, int slaHours) {
        LocalDateTime now = LocalDateTime.now();
        long maxDelayHours = bags.stream()
                .map(Bag::getSealedAt)
                .filter(Objects::nonNull)
                .mapToLong(sealedAt -> Math.max(Duration.between(sealedAt, now).toHours() - slaHours, 0))
                .max()
                .orElse(0);
        return Math.min(maxDelayHours, 20);
    }

    private double scheduleScore(LocalTime fixedDepartureTime, LocalTime requestedDepartureTime) {
        if (fixedDepartureTime == null || requestedDepartureTime == null) {
            return 15.0;
        }
        long diffMinutes = Math.abs(Duration.between(fixedDepartureTime, requestedDepartureTime).toMinutes());
        diffMinutes = Math.min(diffMinutes, 24 * 60 - diffMinutes);
        return clamp(30.0 - (diffMinutes / 4.0), 0.0, 30.0);
    }

    private double capacityUsage(Vehicle vehicle, double totalWeight, double totalVolume) {
        double weightUsage = vehicle.getMaxWeight() > 0 ? totalWeight / vehicle.getMaxWeight() : 0.0;
        double volumeUsage = vehicle.getMaxVolume() > 0 ? totalVolume / vehicle.getMaxVolume() : 0.0;
        return Math.max(weightUsage, volumeUsage);
    }

    private double vehicleFitScore(double capacityUsage) {
        return clamp(30.0 * (1.0 - Math.abs(0.75 - capacityUsage)), 0.0, 30.0);
    }

    private List<String> distinctHints(List<String> hints) {
        if (hints == null || hints.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(hints));
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

    private int safeCurrentOrders(Bag bag) {
        return bag == null || bag.getCurrentOrders() == null || bag.getCurrentOrders() < 0 ? 0 : bag.getCurrentOrders();
    }

    private double safeCurrentWeight(Bag bag) {
        return bag == null || bag.getCurrentWeight() == null || bag.getCurrentWeight() < 0 ? 0.0 : bag.getCurrentWeight();
    }

    private double safeCurrentVolume(Bag bag) {
        return bag == null || bag.getCurrentVolume() == null || bag.getCurrentVolume() < 0 ? 0.0 : bag.getCurrentVolume();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record DestinationKey(
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode
    ) {
    }

    private record RouteOption(
            Route route,
            Vehicle vehicle,
            List<String> blockingHints,
            double baseScore
    ) {
    }

    private class PlanBin {
        private final RouteOption option;
        private final List<Bag> bags = new ArrayList<>();
        private final List<String> hints = new ArrayList<>();
        private double totalWeight;
        private double totalVolume;
        private int totalOrders;

        private PlanBin(RouteOption option) {
            this.option = option;
        }

        private boolean canFit(Bag bag) {
            Vehicle vehicle = option.vehicle();
            double nextWeight = totalWeight + safeCurrentWeight(bag);
            double nextVolume = totalVolume + safeCurrentVolume(bag);
            int nextBags = bags.size() + 1;
            if (vehicle.getMaxWeight() > 0 && nextWeight > vehicle.getMaxWeight()) {
                return false;
            }
            if (vehicle.getMaxVolume() > 0 && nextVolume > vehicle.getMaxVolume()) {
                return false;
            }
            return vehicle.getMaxBags() <= 0 || nextBags <= vehicle.getMaxBags();
        }

        private void add(Bag bag) {
            bags.add(bag);
            totalWeight += safeCurrentWeight(bag);
            totalVolume += safeCurrentVolume(bag);
            totalOrders += safeCurrentOrders(bag);
        }
    }
}
