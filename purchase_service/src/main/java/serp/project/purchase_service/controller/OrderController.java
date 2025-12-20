package serp.project.purchase_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import serp.project.purchase_service.dto.request.OrderCancellationForm;
import serp.project.purchase_service.dto.request.OrderCreationForm;
import serp.project.purchase_service.dto.request.OrderItemUpdateForm;
import serp.project.purchase_service.dto.request.OrderUpdateForm;
import serp.project.purchase_service.dto.response.GeneralResponse;
import serp.project.purchase_service.dto.response.PageResponse;
import serp.project.purchase_service.entity.OrderEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.service.OrderService;
import serp.project.purchase_service.service.ShipmentService;
import serp.project.purchase_service.util.AuthUtils;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase-service/api/v1/order")
@Validated
@Slf4j
public class OrderController {

        private final OrderService orderService;
        private final ShipmentService shipmentService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createOrder(@Valid @RequestBody OrderCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                Long userId = authUtils.getCurrentUserId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Create order {} by userId {} and tenantId {}",
                                form.getOrderName(), userId, tenantId);
                orderService.createPurchaseOrder(form, userId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Order created successfully"));
        }

        @PatchMapping("/update/{orderId}")
        public ResponseEntity<GeneralResponse<?>> updateOrder(
                        @Valid @RequestBody OrderUpdateForm form,
                        @PathVariable String orderId) {

                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Update order {} for tenantId {}", orderId, tenantId);
                orderService.updateOrder(orderId, form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Order updated successfully"));
        }

        @PostMapping("/create/{orderId}/add")
        public ResponseEntity<GeneralResponse<?>> addProductToOrder(
                        @Valid @RequestBody OrderCreationForm.OrderItem itemForm,
                        @PathVariable String orderId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Add product ID {} to order ID {} for tenantId {}", itemForm.getProductId(),
                                orderId, tenantId);
                orderService.createOrderItem(itemForm, orderId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Product added to order successfully"));
        }

        @PatchMapping("/update/{orderId}/delete/{orderItemId}")
        public ResponseEntity<GeneralResponse<?>> deleteProductFromOrder(
                        @PathVariable String orderId,
                        @PathVariable String orderItemId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Delete order item ID {} from order ID {} for tenantId {}", orderItemId,
                                orderId, tenantId);
                orderService.deleteOrderItem(orderItemId, orderId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Product removed from order successfully"));
        }

        @PatchMapping("/update/{orderId}/update/{orderItemId}")
        public ResponseEntity<GeneralResponse<?>> updateProductInOrder(
                        @Valid @RequestBody OrderItemUpdateForm itemForm,
                        @PathVariable String orderId,
                        @PathVariable String orderItemId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Update order item ID {} in order ID {} for tenantId {}", orderItemId,
                                orderId, tenantId);
                orderService.updateOrderItem(orderItemId, itemForm, orderId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Product updated in order successfully"));
        }

        @DeleteMapping("/delete/{orderId}")
        public ResponseEntity<GeneralResponse<?>> deleteOrder(
                        @PathVariable String orderId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Delete order {} for tenantId {}", orderId, tenantId);
                orderService.deleteOrder(orderId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Order deleted successfully"));
        }

        @PatchMapping("/manage/{orderId}/approve")
        public ResponseEntity<GeneralResponse<?>> approveOrder(
                        @PathVariable String orderId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                Long userId = authUtils.getCurrentUserId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Approve order {} by userId {} and tenantId {}", orderId, userId, tenantId);
                orderService.approveOrder(orderId, userId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Order approved successfully"));
        }

        @PatchMapping("/manage/{orderId}/cancel")
        public ResponseEntity<GeneralResponse<?>> cancelOrder(
                        @PathVariable String orderId,
                        @Valid @RequestBody OrderCancellationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                Long userId = authUtils.getCurrentUserId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Cancel order {} by userId {} and tenantId {}", orderId, userId, tenantId);
                orderService.cancelOrder(orderId, form.getNote(), userId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Order cancelled successfully"));
        }

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
                                fromSupplierId,
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
