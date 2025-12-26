package serp.project.sales.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.sales.dto.request.OrderCancellationForm;
import serp.project.sales.dto.request.OrderCreationForm;
import serp.project.sales.dto.request.OrderUpdateForm;
import serp.project.sales.dto.response.GeneralResponse;
import serp.project.sales.dto.response.PageResponse;
import serp.project.sales.entity.OrderEntity;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;
import serp.project.sales.service.OrderService;
import serp.project.sales.service.ProductService;
import serp.project.sales.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sales/api/v1/order")
@Validated
@Slf4j
public class OrderController {

        private final OrderService orderService;
        private final ProductService productService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createOrder(@Valid @RequestBody OrderCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                Long userId = authUtils.getCurrentUserId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Create order {} by userId {} and tenantId {}",
                                form.getOrderName(), userId, tenantId);
                orderService.createSaleOrder(form, userId, tenantId);
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
                orderService.removeOrderItem(orderItemId, orderId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Product removed from order successfully"));
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
                var order = orderService.getDetailOrder(orderId, tenantId);
                if (order == null) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                order.getItems().forEach(item -> {
                        var product = productService.getProduct(item.getProductId(), tenantId);
                        item.setProduct(product);
                });

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
                        @RequestParam(required = false) String toCustomerId,
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
