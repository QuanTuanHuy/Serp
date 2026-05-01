package serp.project.logistics2.orchestrator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.constant.PlanOptimizationStatus;
import serp.project.logistics2.constant.RouteStatus;
import serp.project.logistics2.constant.RouteStopStatus;
import serp.project.logistics2.dto.message.RoutingResponse;
import serp.project.logistics2.entity.DeliveryPlanEntity;
import serp.project.logistics2.entity.DeliverySlipEntity;
import serp.project.logistics2.entity.RouteEntity;
import serp.project.logistics2.entity.RouteStopEntity;
import serp.project.logistics2.entity.VehicleShipperEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.DeliveryPlanRepository;
import serp.project.logistics2.repository.RouteRepository;
import serp.project.logistics2.repository.RouteStopRepository;
import serp.project.logistics2.service.DeliverySlipService;
import serp.project.logistics2.service.VehicleShipperService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingOrchestrator {

    private final DeliveryPlanRepository deliveryPlanRepository;

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;

    private final DeliverySlipService deliverySlipService;
    private final VehicleShipperService vehicleShipperService;

    @Transactional(rollbackFor = Exception.class)
    public void createRouteForDeliveryPlan(RoutingResponse result) {
        DeliveryPlanEntity plan = deliveryPlanRepository.findById(result.getPlanId()).orElse(null);
        if (plan == null) {
            log.error("[RoutingOrchestrator] Không tìm thấy Delivery Plan với ID: {}", result.getPlanId());
            return;
        }

        List<RouteEntity> resultRoutes = new ArrayList<>();
        for (RoutingResponse.RouteResult routeResult : result.getRoutes()) {
            List<RouteStopEntity> routeStops = routeResult.getStops().stream()
                    .map(stop -> RouteStopEntity.create(null, stop.getSlipId(), stop.getSequence(),
                            stop.getEncodedPolyline(), plan.getTenantId()))
                    .toList();
            RouteEntity route = RouteEntity.create(
                    plan.getId(),
                    routeResult.getVehicleId(),
                    routeResult.getRouteDistance(),
                    routeResult.getTotalWeight(),
                    routeResult.getTotalVolume(),
                    plan.getDeliveryDate(),
                    plan.getTenantId(),
                    routeStops);
            resultRoutes.add(route);
        }
        routeRepository.saveAll(resultRoutes);
        log.info("[RoutingOrchestrator] Đã lưu {} routes cho Delivery Plan ID: {}", resultRoutes.size(),
                result.getPlanId());

        routeStopRepository.saveAll(resultRoutes.stream()
                .flatMap(route -> route.getRouteStops().stream())
                .toList());
        log.info("[RoutingOrchestrator] Đã lưu tất cả route stops cho Delivery Plan ID: {}",
                result.getPlanId());

        List<VehicleShipperEntity> unusedVehicleShippers = result.getUnusedVehicleIds().stream()
                .map(vehicleId -> VehicleShipperEntity.createProxy(vehicleId))
                .toList();
        plan.removeVehicleShippers(unusedVehicleShippers);

        List<DeliverySlipEntity> droppedSlips = result.getDroppedSlipIds().stream()
                .map(slipId -> DeliverySlipEntity.createProxy(slipId))
                .toList();
        plan.removeSlips(droppedSlips);
        plan.setOptimizationStatus(PlanOptimizationStatus.COMPLETED.name());
        deliveryPlanRepository.save(plan);
        log.info(
                "[RoutingOrchestrator] Đã cập nhật Delivery Plan ID: {} sau khi tạo routes. Số vehicle shippers sử dụng: {}, số slips sẽ được giao: {}",
                result.getPlanId(), plan.getVehicleShippers().size(), plan.getSlips().size());

        result.getDroppedSlipIds().forEach(slipId -> {
            deliverySlipService.returnSlip(slipId, plan.getTenantId());
        });
        log.info(
                "[RoutingOrchestrator] Đã trả lại tất cả slips bị dropped cho Delivery Plan ID: {}. Số slips bị dropped: {}",
                result.getPlanId(), result.getDroppedSlipIds().size());

        // TODO: Thông báo cho người dùng về đơn hàng bị dropped
    }

    public void failToOptimizePlan(RoutingResponse result) {
        DeliveryPlanEntity plan = deliveryPlanRepository.findById(result.getPlanId()).orElse(null);
        if (plan == null) {
            log.error("[RoutingOrchestrator] Không tìm thấy Delivery Plan với ID: {}", result.getPlanId());
            return;
        }
        plan.setOptimizationStatus(PlanOptimizationStatus.FAILED.name());
        deliveryPlanRepository.save(plan);
        log.info("[RoutingOrchestrator] Đã cập nhật Delivery Plan ID: {} sang trạng thái FAILED do tối ưu kế hoạch thất bại.", result.getPlanId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void selectRouteForDeliver(String routeId) {
        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> new AppException((AppErrorCode.NOT_FOUND)));
        // Kiểm tra xem có route nào đang mang trang thái IN_PROGRESS không, nếu có thì
        // không cho chọn route khác
        if (routeRepository.existsByStatusAndVehicleShipperId(RouteStatus.IN_PROGRESS.name(),
                route.getVehicleShipperId())) {
            log.info(
                    "[RoutingOrchestrator] Đã có route đang được chọn để giao hàng. Vui lòng hoàn thành route đó trước khi chọn route khác.");
            throw new AppException(AppErrorCode.ANOTHER_ROUTE_IN_PROGRESS);
        }

        if (RouteStatus.valueOf(route.getStatus()).ordinal() > RouteStatus.IN_PROGRESS.ordinal()) {
            log.info("[RoutingOrchestrator] Route ID: {} đã được chọn hoặc hoàn thành trước đó. Không thể chọn lại.",
                    routeId);
            throw new AppException(AppErrorCode.ROUTE_ALREADY_SELECTED);
        }
        route.setStatus(RouteStatus.IN_PROGRESS.name());
        routeRepository.save(route);
        log.info("[RoutingOrchestrator] Đã chọn Route ID: {} để giao hàng.", routeId);

        // Export các slip trong route này để chuẩn bị giao hàng
        List<RouteStopEntity> routeStops = routeStopRepository.findByRouteIdOrderBySequenceAsc(routeId);
        routeStops.forEach(stop -> deliverySlipService.exportSlip(stop.getDeliverySlipId(), stop.getTenantId()));
        log.info("[RoutingOrchestrator] Chuyển tất cả đơn hàng của Route ID {} lên phương tiện", route.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void arriveAtStop(String routeStopId) {
        RouteStopEntity stop = routeStopRepository.findById(routeStopId)
                .orElseThrow(() -> new AppException((AppErrorCode.NOT_FOUND)));
        if (RouteStopStatus.valueOf(stop.getStatus()).ordinal() >= RouteStopStatus.ARRIVED.ordinal()) {
            log.info(
                    "[RoutingOrchestrator] Route Stop ID: {} đã được cập nhật trạng thái trước đó. Không thể cập nhật lại.",
                    routeStopId);
            return;
        }
        if (stop.getSequence() > 1) {
            // Kiểm tra xem stop trước đó đã ARRIVED chưa
            RouteStopEntity previousStop = routeStopRepository.findByRouteIdAndSequence(stop.getRouteId(),
                    stop.getSequence() - 1)
                    .orElseThrow(() -> new AppException((AppErrorCode.NOT_FOUND)));
            if (RouteStopStatus.valueOf(previousStop.getStatus()).ordinal() < RouteStopStatus.ARRIVED.ordinal()) {
                log.info(
                        "[RoutingOrchestrator] Route Stop ID: {} chưa thể cập nhật sang ARRIVED vì stop trước đó (ID: {}) chưa ARRIVED.",
                        routeStopId, previousStop.getId());
                throw new AppException(AppErrorCode.PREVIOUS_STOP_NOT_ARRIVED);
            }
        }
        stop.setStatus(RouteStopStatus.ARRIVED.name());
        routeStopRepository.save(stop);
        log.info("[RoutingOrchestrator] Đã cập nhật Route Stop ID: {} sang trạng thái ARRIVED.", routeStopId);

        // Cập nhật trạng thái của Delivery Slip tương ứng
        deliverySlipService.deliverSlip(stop.getDeliverySlipId(), stop.getTenantId());
        log.info("[RoutingOrchestrator] Cập nhật trạng thái Đã giao hàng của Slip ứng với Route Stop ID: {}",
                routeStopId);

        RouteEntity route = routeRepository.findById(stop.getRouteId())
                .orElseThrow(() -> new AppException((AppErrorCode.NOT_FOUND)));
        if (stop.getSequence() == route.getRouteStopCount()) {
            // Nếu đây là stop cuối cùng, cập nhật trạng thái của Route sang COMPLETED
            route.setStatus(RouteStatus.COMPLETED.name());
            routeRepository.save(route);
            log.info("[RoutingOrchestrator] Đã cập nhật Route ID: {} sang trạng thái COMPLETED.", route.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void failAtStop(String routeStopId) {
        RouteStopEntity stop = routeStopRepository.findById(routeStopId)
                .orElseThrow(() -> new AppException((AppErrorCode.NOT_FOUND)));
        if (RouteStopStatus.valueOf(stop.getStatus()).ordinal() >= RouteStopStatus.FAILED.ordinal()) {
            log.info(
                    "[RoutingOrchestrator] Route Stop ID: {} đã được cập nhật trạng thái trước đó. Không thể cập nhật lại.",
                    routeStopId);
            return;
        }
        stop.setStatus(RouteStopStatus.FAILED.name());
        routeStopRepository.save(stop);
        log.info("[RoutingOrchestrator] Đã cập nhật Route Stop ID: {} sang trạng thái FAILED.", routeStopId);

        // Cập nhật trạng thái của Delivery Slip tương ứng
        deliverySlipService.recallSlip(stop.getDeliverySlipId(), stop.getTenantId());
        log.info("[RoutingOrchestrator] Cập nhật trạng thái Giao hàng thất bại của Slip ứng với Route Stop ID: {}",
                routeStopId);

        RouteEntity route = routeRepository.findById(stop.getRouteId())
                .orElseThrow(() -> new AppException((AppErrorCode.NOT_FOUND)));
        if (stop.getSequence() == route.getRouteStopCount()) {
            // Nếu đây là stop cuối cùng, cập nhật trạng thái của Route sang COMPLETED
            route.setStatus(RouteStatus.COMPLETED.name());
            routeRepository.save(route);
            log.info("[RoutingOrchestrator] Đã cập nhật Route ID: {} sang trạng thái COMPLETED.", route.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelVehicleAssignment(String vehicleShipperId) {
        // Thu hồi việc gán xe cho shipper
        vehicleShipperService.cancelVehicleAssignment(vehicleShipperId, null);

        // Cập nhật trạng thái của tất cả route đang PENDING sang ABORTED
        List<String> pendingRoutes = routeRepository.findIdsByVehicleShipperIdAndStatus(vehicleShipperId,
                RouteStatus.PENDING.name());
        pendingRoutes.forEach(routeId -> cancelRoute(routeId));

        // Cập nhật trạng thái của tất cả route đang IN_PROGRESS sang ABORTED
        List<String> inProgressRoutes = routeRepository.findIdsByVehicleShipperIdAndStatus(vehicleShipperId,
                RouteStatus.IN_PROGRESS.name());
        inProgressRoutes.forEach(routeId -> cancelRoute(routeId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelRoute(String routeId) {
        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> new AppException((AppErrorCode.NOT_FOUND)));
        if (RouteStatus.valueOf(route.getStatus()).ordinal() >= RouteStatus.COMPLETED.ordinal()) {
            log.info(
                    "[RoutingOrchestrator] Route ID: {} đã được hoàn thành trước đó. Không thể hủy.",
                    routeId);
            return;
        }
        route.setStatus(RouteStatus.ABORTED.name());
        routeRepository.save(route);
        log.info("[RoutingOrchestrator] Đã cập nhật Route ID: {} sang trạng thái ABORTED.", routeId);

        // Cập nhật trạng thái của tất cả Route Stop trong route này sang FAILED nếu
        // chưa ARRIVED, đồng thời thu hồi tất cả slip chưa được giao hàng
        List<RouteStopEntity> routeStops = routeStopRepository.findByRouteIdOrderBySequenceAsc(routeId);
        routeStops.forEach(stop -> {
            if (RouteStopStatus.valueOf(stop.getStatus()).ordinal() < RouteStopStatus.ARRIVED.ordinal()) {
                stop.setStatus(RouteStopStatus.FAILED.name());
                routeStopRepository.save(stop);
                log.info("[RoutingOrchestrator] Đã cập nhật Route Stop ID: {} sang trạng thái FAILED.",
                        stop.getId());

                // Thu hồi tất cả slip chưa được giao hàng
                if (route.getStatus().equals(RouteStatus.IN_PROGRESS.name())) {
                    deliverySlipService.recallSlip(stop.getDeliverySlipId(), stop.getTenantId());
                    log.info("[RoutingOrchestrator] Thu hồi Slip ứng với Route Stop ID: {} do hủy route.",
                            stop.getId());
                } else {
                    deliverySlipService.returnSlip(stop.getDeliverySlipId(), stop.getTenantId());
                    log.info("[RoutingOrchestrator] Trả Slip ứng với Route Stop ID: {} do hủy route.", stop.getId());
                }
            }
        });
    }

    public RouteStopEntity getNextRouteStop(String vehicleShipperId) {
        Pageable pageable = PageRequest.of(0, 1);
        List<RouteStopEntity> routeStops = routeStopRepository.findNextRouteStop(vehicleShipperId, pageable);
        if (routeStops.isEmpty()) {
            log.info("[RoutingOrchestrator] Không tìm thấy route stop nào đang chờ để giao hàng cho Vehicle Shipper ID: {}", vehicleShipperId);
            return null;
        }
        var routeStop = routeStops.getFirst();
        routeStop.setDeliverySlip(deliverySlipService.getSlip(routeStop.getDeliverySlipId(), routeStop.getTenantId()));
        return routeStop;
    }

}
