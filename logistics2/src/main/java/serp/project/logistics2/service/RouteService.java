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
import serp.project.logistics2.entity.AddressEntity;
import serp.project.logistics2.entity.DeliverySlipEntity;
import serp.project.logistics2.entity.RouteEntity;
import serp.project.logistics2.entity.RouteStopEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.AddressRepository;
import serp.project.logistics2.repository.DeliverySlipRepository;
import serp.project.logistics2.repository.RouteRepository;
import serp.project.logistics2.repository.RouteStopRepository;
import serp.project.logistics2.util.PaginationUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;

    private final DeliverySlipRepository deliverySlipRepository;
    private final AddressRepository addressRepository;

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
        return routeRepository.search(deliveryPlanId, vehicleShipperId, status, deliveryDate, tenantId, pageable);
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
        return route;
    }

}
