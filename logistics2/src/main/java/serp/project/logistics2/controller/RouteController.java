package serp.project.logistics2.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.dto.response.PageResponse;
import serp.project.logistics2.entity.RouteEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.orchestrator.RoutingOrchestrator;
import serp.project.logistics2.service.RouteService;
import serp.project.logistics2.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics2/api/v1/routes")
@Validated
@Slf4j
public class RouteController {

    private final RouteService routeService;
    private final RoutingOrchestrator routingOrchestrator;
    private final AuthUtils authUtils;

    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<PageResponse<RouteEntity>>> searchRoute(
            @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String deliveryPlanId,
            @RequestParam(required = false) String vehicleShipperId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate deliveryDate) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info(
                "[RouteController] Search routes with deliveryPlanId {}, vehicleShipperId {}, status {}, deliveryDate {}, page {}, size {}, sortBy {}, sortDirection {} by tenant id {}",
                deliveryPlanId, vehicleShipperId, status, deliveryDate, page, size, sortBy, sortDirection, tenantId);
        var routePage = routeService.search(deliveryPlanId, vehicleShipperId, status, deliveryDate, tenantId.toString(),
                page, size, sortBy, sortDirection);
        return ResponseEntity
                .ok(GeneralResponse.success("Truy vấn danh sách lộ trình thành công", PageResponse.of(routePage)));
    }

    @GetMapping("/search/{routeId}")
    public ResponseEntity<GeneralResponse<RouteEntity>> getRouteById(@PathVariable String routeId) {
        log.info("[RouteController] Get route by id {}", routeId);
        var route = routeService.getDetailRoute(routeId);
        return ResponseEntity.ok(GeneralResponse.success("Truy vấn thông tin lộ trình thành công", route));
    }

    @PutMapping("/select/{routeId}")
    public ResponseEntity<GeneralResponse<?>> selectRoute(@PathVariable String routeId) {
        log.info("[RouteController] Select route id {} for delivery", routeId);
        routingOrchestrator.selectRouteForDeliver(routeId);
        return ResponseEntity.ok(GeneralResponse.success("Chọn chuyến hàng để bắt đầu giao thành công"));
    }

    @PutMapping("/complete/{routeStopId}")
    public ResponseEntity<GeneralResponse<?>> completeRouteStop(@PathVariable String routeStopId) {
        log.info("[RouteController] Complete route stop id {}", routeStopId);
        routingOrchestrator.arriveAtStop(routeStopId);
        return ResponseEntity.ok(GeneralResponse.success("Hoàn thành giao hàng thành công tại điểm " + routeStopId));
    }

    @PutMapping("/abort/{routeStopId}")
    public ResponseEntity<GeneralResponse<?>> abortRouteStop(@PathVariable String routeStopId) {
        log.info("[RouteController] Abort route stop id {}", routeStopId);
        routingOrchestrator.failAtStop(routeStopId);
        return ResponseEntity.ok(GeneralResponse.success("Giao hàng thất bại tại điểm " + routeStopId));
    }

}
