package serp.project.logistics2.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.entity.*;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.*;
import serp.project.logistics2.util.PaginationUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;

    private final DeliverySlipRepository deliverySlipRepository;
    private final AddressRepository addressRepository;

    private final VehicleRepository vehicleRepository;
    private final VehicleShipperRepository vehicleShipperRepository;

    public Page<RouteEntity> search(
            String deliveryPlanId,
            String vehicleShipperId,
            String status,
            LocalDate deliveryDate,
            String tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        var routePage = routeRepository.search(deliveryPlanId, vehicleShipperId, status, deliveryDate, tenantId, pageable);
        return aggregatePage(routePage);
    }

    public Page<RouteEntity> search(
            String deliverySlipId,
            String tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        var routePage = routeRepository.search(deliverySlipId, tenantId, pageable);
        return aggregatePage(routePage);
    }

    private Page<RouteEntity> aggregatePage(Page<RouteEntity> routePage) {
        List<String> vehicleShipperIds = routePage.getContent().stream()
                .map(RouteEntity::getVehicleShipperId)
                .distinct()
                .toList();
        List<VehicleShipperEntity> vehicleShipperEntities = vehicleShipperRepository.findAllById(vehicleShipperIds);

        List<String> vehicleIds = vehicleShipperEntities.stream()
                .map(VehicleShipperEntity::getVehicleId)
                .distinct()
                .toList();
        List<VehicleEntity> vehicleEntities = vehicleRepository.findAllById(vehicleIds);
        Map<String, VehicleEntity> vehicleMap = vehicleEntities.stream()
                .collect(Collectors.toMap(VehicleEntity::getId, v -> v));

        vehicleShipperEntities.forEach(vehicleShipper -> {vehicleShipper.setVehicle(vehicleMap.get(vehicleShipper.getVehicleId()));});
        Map<String, VehicleShipperEntity> vehicleShipperMap = vehicleShipperEntities.stream()
                .collect(Collectors.toMap(VehicleShipperEntity::getId, vs -> vs));
        routePage.getContent().forEach(route -> route.setVehicleShipper(vehicleShipperMap.get(route.getVehicleShipperId())));
        return routePage;
    }

    public RouteEntity getDetailRoute(String routeId) {
        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        List<RouteStopEntity> routeStops = routeStopRepository.findByRouteIdOrderBySequenceAsc(routeId);

        List<String> deliverySlipIds = routeStops.stream()
                .map(RouteStopEntity::getDeliverySlipId)
                .toList();
        List<DeliverySlipEntity> deliverySlips = deliverySlipRepository.findAllById(deliverySlipIds);

        List<String> addressIds = deliverySlips.stream()
                .map(DeliverySlipEntity::getCustomerAddressId)
                .toList();
        List<AddressEntity> addresses = addressRepository.findAllById(addressIds);
        Map<String, AddressEntity> addressMap = addresses.stream()
                .collect(Collectors.toMap(AddressEntity::getId, a -> a));

        deliverySlips.forEach(ds -> ds.setCustomerAddress(addressMap.get(ds.getCustomerAddressId())));
        Map<String, DeliverySlipEntity> deliverySlipMap = deliverySlips.stream()
                .collect(Collectors.toMap(DeliverySlipEntity::getId, ds -> ds));
        routeStops.forEach(rs -> rs.setDeliverySlip(deliverySlipMap.get(rs.getDeliverySlipId())));
        route.setRouteStops(routeStops);

        VehicleShipperEntity vehicleShipper = vehicleShipperRepository.findById(route.getVehicleShipperId()).orElse(null);
        if (vehicleShipper != null) {
            VehicleEntity vehicle = vehicleRepository.findById(vehicleShipper.getVehicleId()).orElse(null);
            vehicleShipper.setVehicle(vehicle);
        }
        route.setVehicleShipper(vehicleShipper);

        return route;
    }

}
