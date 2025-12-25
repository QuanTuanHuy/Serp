package serp.project.logistics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import serp.project.logistics.dto.response.GeneralResponse;
import serp.project.logistics.dto.response.PageResponse;
import serp.project.logistics.entity.OrderEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.service.OrderService;
import serp.project.logistics.service.ShipmentService;
import serp.project.logistics.util.AuthUtils;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics/api/v1/order")
@Validated
@Slf4j
public class OrderController {

        private final OrderService orderService;
        private final ShipmentService shipmentService;
        private final AuthUtils authUtils;

        @GetMapping("/search/{orderId}")
        public ResponseEntity<GeneralResponse<OrderEntity>> getOrderDetail(
                        @PathVariable String orderId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                var order = orderService.getOrder(orderId, tenantId);
                if (order == null) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                var orderItems = orderService.findByOrderId(orderId, tenantId);
                var shipments = shipmentService.findByOrderId(orderId, tenantId);
                order.setItems(orderItems);
                order.setShipments(shipments);

                return ResponseEntity.ok(GeneralResponse.success("Successfully get order detail", order));
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
