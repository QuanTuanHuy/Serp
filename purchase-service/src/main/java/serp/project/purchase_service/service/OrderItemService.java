package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.purchase_service.constant.OrderItemStatus;
import serp.project.purchase_service.dto.request.OrderCreationForm;
import serp.project.purchase_service.dto.request.OrderItemUpdateForm;
import serp.project.purchase_service.entity.OrderItemEntity;
import serp.project.purchase_service.entity.ProductEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.OrderItemRepository;
import serp.project.purchase_service.util.CalculatorUtils;
import serp.project.purchase_service.util.IdUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    public void createOrderItems(OrderCreationForm.OrderItem itemForm, String orderId, Long tenantId) {
        ProductEntity product = productService.getProduct(itemForm.getProductId(), tenantId);
        if (product == null) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        String orderItemId = IdUtils.generateOrderItemId();
        OrderItemEntity orderItem = OrderItemEntity.builder()
                .id(orderItemId)
                .orderId(orderId)
                .orderItemSeqId(itemForm.getOrderItemSeqId())
                .productId(itemForm.getProductId())
                .quantity(itemForm.getQuantity())
                .price(product.getCostPrice())
                .tax(itemForm.getTax())
                .discount(itemForm.getDiscount())
                .amount(CalculatorUtils.calculateTotalAmount(product.getCostPrice(), itemForm.getQuantity(), itemForm.getDiscount(), itemForm.getTax()))
                .statusId(OrderItemStatus.CREATED.value())
                .unit(product.getUnit())
                .tenantId(tenantId)
                .build();
        orderItemRepository.save(orderItem);
    }

    public void updateOrderItem(String orderItemId, OrderItemUpdateForm form, String orderId, Long tenantId) {
        OrderItemEntity orderItem = orderItemRepository.findById(orderItemId).orElse(null);
        if (orderItem == null || !orderItem.getTenantId().equals(tenantId) || !orderItem.getOrderId().equals(orderId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        orderItem.setOrderItemSeqId(form.getOrderItemSeqId());
        orderItem.setQuantity(form.getQuantity());
        orderItem.setTax(form.getTax());
        orderItem.setDiscount(form.getDiscount());
        orderItem.setAmount(CalculatorUtils.calculateTotalAmount(orderItem.getPrice(), form.getQuantity(), form.getDiscount(), form.getTax()));
        orderItemRepository.save(orderItem);
    }

    public List<OrderItemEntity> findByOrderId(String orderId, Long tenantId) {
        return orderItemRepository.findByTenantIdAndOrderId(tenantId, orderId);
    }

    public void deleteOrderItem(String orderItemId, String orderId, Long tenantId) {
        OrderItemEntity orderItem = orderItemRepository.findById(orderItemId).orElse(null);
        if (orderItem == null || !orderItem.getTenantId().equals(tenantId) || !orderItem.getOrderId().equals(orderId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        orderItemRepository.delete(orderItem);
    }

}
