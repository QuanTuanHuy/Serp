/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.caller.FirstMilePostOfficeCaller;
import serp.project.tms_order.caller.FirstMilePostOfficeSuggestionCaller;
import serp.project.tms_order.caller.dto.firstmile.DestinationPostOfficeReservationResponse;
import serp.project.tms_order.caller.dto.firstmile.OriginPostOfficeReservationResponse;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.request.ConfirmDropOffOrderRequest;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kafka.OrderEventDispatcher;
import serp.project.tms_order.mapper.OrderConfirmationMapper;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.service.OrderDropOffService;
import serp.project.tms_order.service.OrderTimelineService;
import serp.project.tms_order.service.order.OrderAccessPolicy;
import serp.project.tms_order.service.order.OrderLocationUtils;
import serp.project.tms_order.service.order.OrderTextUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderDropOffServiceImpl implements OrderDropOffService {

    private static final Set<OrderStatus> CONFIRMABLE_ORDER_STATUSES = EnumSet.of(
            OrderStatus.CREATED,
            OrderStatus.PICKUP_FAILED
    );
    private static final int DEFAULT_DROP_OFF_SUGGESTION_LIMIT = 5;
    private static final int MAX_DROP_OFF_SUGGESTION_LIMIT = 20;

    private final OrderRepository orderRepository;
    private final FirstMilePostOfficeCaller firstMilePostOfficeCaller;
    private final FirstMilePostOfficeSuggestionCaller firstMilePostOfficeSuggestionCaller;
    private final OrderTimelineService orderTimelineService;
    private final OrderAccessPolicy orderAccessPolicy;
    private final OrderEventDispatcher orderEventDispatcher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderConfirmationResponse confirmDropOffOrderAtPostOffice(
            Long orderId,
            Long tenantId,
            ConfirmDropOffOrderRequest request
    ) {
        if (request == null || request.getPostOfficeId() == null || request.getPostOfficeId() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        orderAccessPolicy.ensurePostOfficeManager();

        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        ensureDropOffPickupMethod(order);

        if (Boolean.TRUE.equals(order.getIsConfirm())) {
            return confirmAlreadyConfirmedDropOffOrder(order, request);
        }

        if (OrderTextUtils.hasText(order.getOriginPostOfficeCode())) {
            return confirmLinkedDropOffOrder(order, request);
        }

        validateOrderForConfirmation(order);
        DestinationPostOfficeReservationResponse reservedDestinationPostOffice =
                reserveDestinationPostOfficeIfNeeded(order);

        OriginPostOfficeReservationResponse reservedPostOffice =
                firstMilePostOfficeCaller.reserveDropOffOriginPostOffice(
                        request.getPostOfficeId(),
                        OrderLocationUtils.toLatitude(order.getSenderLocation()),
                        OrderLocationUtils.toLongitude(order.getSenderLocation())
                );

        order.setOriginPostOfficeCode(reservedPostOffice.getCode());
        order.setIsConfirm(true);
        order.setStatus(OrderStatus.AT_ORIGIN_POST_OFFICE);

        Order savedOrder = saveDropOffConfirmation(order);
        return OrderConfirmationMapper.toResponse(
                savedOrder,
                reservedPostOffice,
                reservedDestinationPostOffice,
                false
        );
    }

    @Override
    public List<OrderDropOffPostOfficeSuggestionResponse> getDropOffPostOfficeSuggestions(
            Long orderId,
            Integer limit,
            Long tenantId
    ) {
        if (orderId == null || orderId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        orderAccessPolicy.validateCanMutateOrder(order);
        ensureDropOffPickupMethod(order);
        validateOrderForConfirmation(order);

        return firstMilePostOfficeSuggestionCaller.getDropOffSuggestions(
                OrderLocationUtils.toLatitude(order.getSenderLocation()),
                OrderLocationUtils.toLongitude(order.getSenderLocation()),
                normalizeDropOffSuggestionLimit(limit)
        );
    }

    private OrderConfirmationResponse confirmAlreadyConfirmedDropOffOrder(
            Order order,
            ConfirmDropOffOrderRequest request
    ) {
        OriginPostOfficeReservationResponse managedPostOffice =
                firstMilePostOfficeCaller.validateManagedPostOffice(request.getPostOfficeId());
        if (hasSamePostOfficeCode(order.getOriginPostOfficeCode(), managedPostOffice.getCode())) {
            if (!OrderStatus.AT_ORIGIN_POST_OFFICE.equals(order.getStatus())) {
                order.setStatus(OrderStatus.AT_ORIGIN_POST_OFFICE);
                Order savedOrder = orderRepository.save(order);
                recordDropOffConfirmation(savedOrder);
                orderEventDispatcher.publishOrderAfterCommit(savedOrder);
                return OrderConfirmationMapper.toResponse(savedOrder, managedPostOffice, null, true);
            }
            return OrderConfirmationMapper.toResponse(order, managedPostOffice, null, true);
        }

        throw new AppException(
                ErrorCode.INVALID_REQUEST,
                "Order has already been confirmed at another post office."
        );
    }

    private OrderConfirmationResponse confirmLinkedDropOffOrder(
            Order order,
            ConfirmDropOffOrderRequest request
    ) {
        OriginPostOfficeReservationResponse managedPostOffice =
                firstMilePostOfficeCaller.validateManagedPostOffice(request.getPostOfficeId());
        if (!hasSamePostOfficeCode(order.getOriginPostOfficeCode(), managedPostOffice.getCode())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Order is already linked to another origin post office."
            );
        }

        validateOrderForConfirmation(order);
        DestinationPostOfficeReservationResponse reservedDestinationPostOffice =
                reserveDestinationPostOfficeIfNeeded(order);

        order.setIsConfirm(true);
        order.setStatus(OrderStatus.AT_ORIGIN_POST_OFFICE);
        Order savedOrder = saveDropOffConfirmation(order);
        return OrderConfirmationMapper.toResponse(savedOrder, managedPostOffice, reservedDestinationPostOffice, true);
    }

    private Order saveDropOffConfirmation(Order order) {
        Order savedOrder = orderRepository.save(order);
        recordDropOffConfirmation(savedOrder);
        orderEventDispatcher.publishOrderAfterCommit(savedOrder);
        orderEventDispatcher.publishOrderConfirmedNotificationAfterCommit(savedOrder);
        return savedOrder;
    }

    private void recordDropOffConfirmation(Order order) {
        orderTimelineService.recordStatusEvent(
                order,
                OrderStatus.AT_ORIGIN_POST_OFFICE,
                "Drop-off order confirmed at origin post office.",
                null
        );
    }

    private void ensureDropOffPickupMethod(Order order) {
        if (!OrderPickupMethod.DROP_OFF_AT_POST_OFFICE.equals(resolveOrderPickupMethod(order))) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Order pickup method is not drop-off at post office."
            );
        }
    }

    private OrderPickupMethod resolveOrderPickupMethod(Order order) {
        if (order == null || order.getPickupMethod() == null) {
            return OrderPickupMethod.COURIER_PICKUP;
        }
        return order.getPickupMethod();
    }

    private void validateOrderForConfirmation(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == null || !CONFIRMABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        validateOrderLocation(
                order.getSenderLocation(),
                "Sender coordinates are required before order confirmation."
        );
        validateOrderLocation(
                order.getReceiverLocation(),
                "Receiver coordinates are required before order confirmation."
        );
    }

    private void validateOrderLocation(Point location, String detail) {
        if (location == null) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE, detail);
        }

        double latitude = location.getY();
        double longitude = location.getX();
        if (!OrderLocationUtils.isValidCoordinate(latitude, longitude)) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE, detail);
        }
    }

    private DestinationPostOfficeReservationResponse reserveDestinationPostOfficeIfNeeded(Order order) {
        if (OrderTextUtils.hasText(order.getDestinationPostOfficeCode())) {
            return null;
        }

        DestinationPostOfficeReservationResponse reservedDestinationPostOffice =
                firstMilePostOfficeCaller.reserveBestDestinationPostOffice(
                        OrderLocationUtils.toLatitude(order.getReceiverLocation()),
                        OrderLocationUtils.toLongitude(order.getReceiverLocation())
                );
        order.setDestinationPostOfficeCode(reservedDestinationPostOffice.getCode());
        return reservedDestinationPostOffice;
    }

    private int normalizeDropOffSuggestionLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_DROP_OFF_SUGGESTION_LIMIT;
        }
        return Math.min(limit, MAX_DROP_OFF_SUGGESTION_LIMIT);
    }

    private boolean hasSamePostOfficeCode(String left, String right) {
        return OrderTextUtils.hasText(left) && OrderTextUtils.hasText(right) && left.trim().equalsIgnoreCase(right.trim());
    }
}
