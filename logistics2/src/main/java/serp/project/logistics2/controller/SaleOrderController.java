package serp.project.logistics2.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.dto.response.PageResponse;
import serp.project.logistics2.entity.OrderEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.service.OrderService;
import serp.project.logistics2.util.AuthUtils;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics2/api/v1/sale-order")
@Slf4j
public class SaleOrderController {

    private final OrderService orderService;
    private final AuthUtils authUtils;

    @GetMapping("/search/{orderId}")
    public ResponseEntity<GeneralResponse<OrderEntity>> getSaleOrderDetail(
            @PathVariable String orderId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        var order = orderService.getSaleOrder(orderId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Successfully get sale order detail", order));
    }

    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<PageResponse<OrderEntity>>> getOrders(
            @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String statusId,
            @RequestParam(required = false) String orderTypeId,
            @RequestParam(required = false) String toCustomerId,
            @RequestParam(required = false) String fromSupplierId,
            @RequestParam(required = false) String saleChannelId,
            @RequestParam(required = false) LocalDate orderDateAfter,
            @RequestParam(required = false) LocalDate orderDateBefore,
            @RequestParam(required = false) LocalDate deliveryBefore,
            @RequestParam(required = false) LocalDate deliveryAfter) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OrderController] Search orders of page {}/{} for tenantId {}", page, size, tenantId);
        var orders = orderService.findOrders(
                query,
                orderTypeId,
                fromSupplierId,
                toCustomerId,
                saleChannelId,
                orderDateAfter,
                orderDateBefore,
                deliveryBefore,
                deliveryAfter,
                statusId,
                tenantId,
                page,
                size,
                sortBy,
                sortDirection);
        return ResponseEntity.ok(GeneralResponse.success("Successfully get orders", PageResponse.of(orders)));
    }

}
