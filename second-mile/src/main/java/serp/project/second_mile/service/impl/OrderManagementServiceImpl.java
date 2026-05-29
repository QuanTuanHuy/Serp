/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.domain.BagOrder;
import serp.project.second_mile.domain.Order;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.OrderFilterRequest;
import serp.project.second_mile.dto.response.OrderResponse;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.BagOrderRepository;
import serp.project.second_mile.repository.OrderRepository;
import serp.project.second_mile.repository.specification.OrderSpecification;
import serp.project.second_mile.service.OrderManagementService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderManagementServiceImpl implements OrderManagementService {
    private final OrderRepository orderRepository;
    private final BagOrderRepository bagOrderRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders(int page, int size, OrderFilterRequest filterRequest) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        OrderFilterRequest normalizedFilter = normalizeFilter(filterRequest);

        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecification.byFilter(tenantId, normalizedFilter),
                pageable
        );

        Map<Long, BagOrder> bagOrderByOrderId = mapBagOrderByOrderId(orderPage.getContent(), tenantId);
        List<OrderResponse> mappedItems = orderPage.getContent().stream()
                .map(order -> mapToResponse(order, bagOrderByOrderId.get(order.getId())))
                .filter(response -> shouldInclude(response, normalizedFilter.getAssignedToBag()))
                .toList();

        return PageResponse.<OrderResponse>builder()
                .items(mappedItems)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Order order = orderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_ORDER_NOT_FOUND));
        BagOrder bagOrder = bagOrderRepository.findByOrder_IdInAndTenantId(List.of(order.getId()), tenantId).stream()
                .findFirst()
                .orElse(null);
        return mapToResponse(order, bagOrder);
    }

    private Map<Long, BagOrder> mapBagOrderByOrderId(List<Order> orders, Long tenantId) {
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .filter(Objects::nonNull)
                .toList();
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return bagOrderRepository.findByOrder_IdInAndTenantId(orderIds, tenantId).stream()
                .collect(Collectors.toMap(item -> item.getOrder().getId(), Function.identity(), (left, right) -> left));
    }

    private OrderFilterRequest normalizeFilter(OrderFilterRequest filterRequest) {
        if (filterRequest == null) {
            return OrderFilterRequest.builder().build();
        }
        return OrderFilterRequest.builder()
                .keyword(normalizeText(filterRequest.getKeyword()))
                .orderCode(normalizeText(filterRequest.getOrderCode()))
                .customerOrderCode(normalizeText(filterRequest.getCustomerOrderCode()))
                .originPostOfficeCode(normalizeText(filterRequest.getOriginPostOfficeCode()))
                .destinationPostOfficeCode(normalizeText(filterRequest.getDestinationPostOfficeCode()))
                .status(filterRequest.getStatus())
                .assignedToBag(filterRequest.getAssignedToBag())
                .build();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private OrderResponse mapToResponse(Order order, BagOrder bagOrder) {
        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getOriginPostOfficeCode(),
                order.getDestinationPostOfficeCode(),
                order.getStatus(),
                order.getTotalWeight(),
                order.getTotalVolume(),
                order.getOrderProductCategory(),
                order.getOrderType(),
                order.getNote(),
                bagOrder == null || bagOrder.getBag() == null ? null : bagOrder.getBag().getId(),
                bagOrder == null || bagOrder.getBag() == null ? null : bagOrder.getBag().getBagCode(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getTenantId()
        );
    }

    private boolean shouldInclude(OrderResponse response, Boolean assignedToBag) {
        if (assignedToBag == null) {
            return true;
        }
        boolean isAssigned = response.assignedBagId() != null;
        return assignedToBag.equals(isAssigned);
    }
}
