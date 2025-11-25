package serp.project.purchase_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.purchase_service.dto.request.OrderCancellationForm;
import serp.project.purchase_service.dto.request.OrderCreationForm;
import serp.project.purchase_service.dto.request.OrderItemUpdateForm;
import serp.project.purchase_service.dto.request.OrderUpdateForm;
import serp.project.purchase_service.dto.response.GeneralResponse;
import serp.project.purchase_service.dto.response.OrderDetailResponse;
import serp.project.purchase_service.dto.response.PageResponse;
import serp.project.purchase_service.entity.OrderEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.service.OrderItemService;
import serp.project.purchase_service.service.OrderService;
import serp.project.purchase_service.util.AuthUtils;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase-service/api/v1/order")
@Validated
@Slf4j
public class OrderController {

        private final OrderService orderService;
        private final OrderItemService orderItemService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createOrder(@RequestBody OrderCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                Long userId = authUtils.getCurrentUserId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Create order {} by userId {} and tenantId {}",
                                form.getOrderName(), userId, tenantId);
                orderService.createOrder(form, userId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Order created successfully"));
        }

        @PatchMapping("/update/{orderId}")
        public ResponseEntity<GeneralResponse<?>> updateOrder(
                        @RequestBody OrderUpdateForm form,
                        @PathVariable String orderId) {

                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Update order {} for tenantId {}", orderId, tenantId);
                orderService.updateOrder(orderId, form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Order updated successfully"));
        }

        @PostMapping("/create/{orderId}/add")
        public ResponseEntity<GeneralResponse<?>> addProductToOrder(
                        @RequestBody OrderCreationForm.OrderItem itemForm,
                        @PathVariable String orderId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Add product ID {} to order ID {} for tenantId {}", itemForm.getProductId(),
                                orderId, tenantId);
                orderItemService.createOrderItems(itemForm, orderId, tenantId);
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
                orderItemService.deleteOrderItem(orderItemId, orderId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Product removed from order successfully"));
        }

        @PatchMapping("/update/{orderId}/update/{orderItemId}")
        public ResponseEntity<GeneralResponse<?>> updateProductInOrder(
                        @RequestBody OrderItemUpdateForm itemForm,
                        @PathVariable String orderId,
                        @PathVariable String orderItemId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Update order item ID {} in order ID {} for tenantId {}", orderItemId,
                                orderId, tenantId);
                orderItemService.updateOrderItem(orderItemId, itemForm, orderId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Product updated in order successfully"));
        }

        @DeleteMapping("/delete/{orderId}")
        public ResponseEntity<GeneralResponse<?>> deleteOrder(
                        @PathVariable String orderId) {
                throw new AppException(AppErrorCode.UNIMPLEMENTED);
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
                        @RequestBody OrderCancellationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                Long userId = authUtils.getCurrentUserId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Cancel order {} by userId {} and tenantId {}", orderId, userId, tenantId);
                orderService.cancelOrder(orderId, form.getNote(), userId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Order cancelled successfully"));
        }

        @PatchMapping("/update/{orderId}/ready")
        public ResponseEntity<GeneralResponse<?>> markOrderAsReady(
                        @PathVariable String orderId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Mark order {} as ready for delivery for tenantId {}", orderId, tenantId);
                orderService.readyForDeliveryOrder(orderId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Order marked as ready for delivery successfully"));
        }

        @GetMapping("/search/{orderId}")
        public ResponseEntity<GeneralResponse<OrderDetailResponse>> getOrderDetail(
                        @PathVariable String orderId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                var order = orderService.getOrder(orderId, tenantId);
                if (order == null) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }
                log.info("[OrderController] Get order detail for order ID {} and tenantId {}", orderId, tenantId);
                var orderItems = orderItemService.findByOrderId(orderId, tenantId);
                OrderDetailResponse response = OrderDetailResponse.fromEntity(
                                order,
                                orderItems);
                return ResponseEntity.ok(GeneralResponse.success("Successfully get order detail", response));
        }

        @GetMapping("/search")
        public ResponseEntity<GeneralResponse<PageResponse<OrderEntity>>> getOrders(
                        @RequestParam(required = false, defaultValue = "1") int page,
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
