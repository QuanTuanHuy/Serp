/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.caller.TmsOrderClient;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.dto.request.SortInboundOrdersRequest;
import serp.project.first_mile.dto.response.InboundOrderResponse;
import serp.project.first_mile.enums.DeliveryOrderStatus;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.TripStatus;
import serp.project.first_mile.enums.TripType;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.repository.DeliveryManifestOrderRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.service.OrderSortingService;
import serp.project.first_mile.service.TmsOrderTransitionPublisherService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderSortingServiceImpl implements OrderSortingService {

    private static final String TRANSITION_SOURCE = "LAST_MILE_DELIVERY";
    private static final List<DeliveryOrderStatus> ACTIVE_DELIVERY_ORDER_STATUSES = List.of(
            DeliveryOrderStatus.PENDING,
            DeliveryOrderStatus.OUT_FOR_DELIVERY
    );

    private final TmsOrderClient tmsOrderClient;
    private final TmsOrderTransitionPublisherService tmsOrderTransitionPublisherService;
    private final DeliveryManifestOrderRepository deliveryManifestOrderRepository;
    private final TripOrderRepository tripOrderRepository;

    @Override
    public List<InboundOrderResponse> getInboundOrders(String postOfficeCode, OrderStatus status, Long tenantId) {
        List<OrderStatus> statuses = status != null
                ? List.of(status)
                : List.of(OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE);
        List<TmsOrderOperationView> orders = tmsOrderClient.lookupAtPostOffice(postOfficeCode, statuses, tenantId);
        if (status == OrderStatus.READY_FOR_DELIVERY) {
            orders = orders.stream()
                    .filter(order -> !isAssignedToActiveDeliveryDispatch(order, tenantId))
                    .toList();
        }
        return orders.stream().map(this::toInboundResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<InboundOrderResponse> confirmInbound(SortInboundOrdersRequest request, Long tenantId) {
        if (request.getOrderCodes() == null || request.getOrderCodes().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "orderCodes must not be empty.");
        }

        List<TmsOrderOperationView> orders = tmsOrderClient.lookupByCodes(tenantId, request.getOrderCodes());

        // Validate: all orders must be INBOUND_AT_DESTINATION_POST_OFFICE at the correct PO
        for (TmsOrderOperationView order : orders) {
            if (order.getStatus() != OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE,
                        "Order " + order.getOrderCode() + " is not at status INBOUND_AT_DESTINATION_POST_OFFICE.");
            }
            if (request.getPostOfficeCode() != null
                    && !request.getPostOfficeCode().equalsIgnoreCase(order.getDestinationPostOfficeCode())) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE,
                        "Order " + order.getOrderCode() + " does not belong to post office " + request.getPostOfficeCode());
            }
        }

        // Transition to READY_FOR_DELIVERY
        List<TmsOrderStatusTransitionRequest.Item> items = orders.stream()
                .map(o -> TmsOrderStatusTransitionRequest.Item.builder()
                        .orderCode(o.getOrderCode())
                        .expectedStatuses(List.of(OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE))
                        .targetStatus(OrderStatus.READY_FOR_DELIVERY)
                        .description("Đã xác nhận nhập kho tại bưu cục đích, sẵn sàng giao hàng.")
                        .build())
                .toList();

        tmsOrderTransitionPublisherService.publish(TmsOrderStatusTransitionRequest.builder()
                .source(TRANSITION_SOURCE)
                .idempotencyKey(UUID.randomUUID().toString())
                .items(items)
                .build(), tenantId);

        return orders.stream().map(this::toInboundResponse).toList();
    }

    private InboundOrderResponse toInboundResponse(TmsOrderOperationView view) {
        return InboundOrderResponse.builder()
                .orderId(view.getId())
                .orderCode(view.getOrderCode())
                .status(view.getStatus())
                .destinationPostOfficeCode(view.getDestinationPostOfficeCode())
                .receiverName(view.getReceiverName())
                .receiverPhone(view.getReceiverPhone())
                .receiverAddressDetail(view.getReceiverAddressDetail())
                .codAmount(view.getCodAmount())
                .totalShippingFee(view.getTotalShippingFee())
                .feePayer(view.getFeePayer())
                .build();
    }

    private boolean isAssignedToActiveDeliveryDispatch(TmsOrderOperationView order, Long tenantId) {
        boolean hasNoActiveManifest = deliveryManifestOrderRepository
                .findByTenantIdAndOrderCodeAndStatusIn(
                        tenantId,
                        order.getOrderCode(),
                        ACTIVE_DELIVERY_ORDER_STATUSES
                )
                .isEmpty();
        if (!hasNoActiveManifest) {
            return true;
        }
        if (order.getId() == null) {
            return false;
        }
        return tripOrderRepository.existsByTenantIdAndOrderIdAndTripTypeAndTripStatusIn(
                tenantId,
                order.getId(),
                TripType.DELIVERY,
                List.of(TripStatus.PLANNED, TripStatus.IN_PROGRESS)
        );
    }
}
