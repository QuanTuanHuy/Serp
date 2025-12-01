package serp.project.logistics.dto.response;

import lombok.Builder;
import lombok.Data;
import serp.project.logistics.entity.OrderEntity;
import serp.project.logistics.entity.OrderItemEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDetailResponse {

    private String id;
    private String orderTypeId;
    private String fromSupplierId;
    private String toCustomerId;
    private Long createdByUserId;
    private LocalDateTime createdStamp;
    private LocalDate orderDate;
    private String statusId;
    private LocalDateTime lastUpdatedStamp;
    private LocalDate deliveryBeforeDate;
    private LocalDate deliveryAfterDate;
    private String note;
    private String orderName;
    private int priority;
    private String deliveryAddressId;
    private String deliveryPhone;
    private String saleChannelId;
    private String deliveryFullAddress;
    private int totalQuantity;
    private Long totalAmount;
    private String costs;
    private Long userApprovedId;
    private Long userCancelledId;
    private String cancellationNote;
    private List<OrderItemEntity> orderItems;

    public static OrderDetailResponse fromEntity(
            OrderEntity order,
            List<OrderItemEntity> orderItems
    ) {
        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderTypeId(order.getOrderTypeId())
                .fromSupplierId(order.getFromSupplierId())
                .toCustomerId(order.getToCustomerId())
                .createdByUserId(order.getCreatedByUserId())
                .createdStamp(order.getCreatedStamp())
                .orderDate(order.getOrderDate())
                .statusId(order.getStatusId())
                .lastUpdatedStamp(order.getLastUpdatedStamp())
                .deliveryBeforeDate(order.getDeliveryBeforeDate())
                .deliveryAfterDate(order.getDeliveryAfterDate())
                .note(order.getNote())
                .orderName(order.getOrderName())
                .priority(order.getPriority())
                .deliveryAddressId(order.getDeliveryAddressId())
                .deliveryPhone(order.getDeliveryPhone())
                .saleChannelId(order.getSaleChannelId())
                .deliveryFullAddress(order.getDeliveryFullAddress())
                .totalQuantity(order.getTotalQuantity())
                .totalAmount(order.getTotalAmount())
                .costs(order.getCosts())
                .userApprovedId(order.getUserApprovedId())
                .userCancelledId(order.getUserCancelledId())
                .cancellationNote(order.getCancellationNote())
                .orderItems(orderItems)
                .build();
    }

}
