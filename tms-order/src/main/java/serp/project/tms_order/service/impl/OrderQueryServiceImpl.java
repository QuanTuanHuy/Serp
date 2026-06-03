/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.request.InternalOrderLookupRequest;
import serp.project.tms_order.dto.request.InternalPickupCandidateRequest;
import serp.project.tms_order.dto.response.OrderOperationView;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.mapper.OrderOperationMapper;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.service.OrderQueryService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private static final int DEFAULT_PICKUP_CANDIDATE_LIMIT = 300;
    private static final int MAX_PICKUP_CANDIDATE_LIMIT = 1000;

    private final OrderRepository orderRepository;

    @Override
    public List<OrderOperationView> lookupOrders(InternalOrderLookupRequest request, Long tenantId) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Map<Long, Order> orderById = new LinkedHashMap<>();
        Set<Long> orderIds = normalizeOrderIds(request.getOrderIds());
        if (!orderIds.isEmpty()) {
            for (Order order : orderRepository.findByIdInAndTenantId(orderIds, tenantId)) {
                orderById.put(order.getId(), order);
            }
        }

        Set<String> orderCodes = normalizeOrderCodes(request.getOrderCodes());
        if (!orderCodes.isEmpty()) {
            for (Order order : orderRepository.findByTenantIdAndUpperOrderCodeIn(tenantId, orderCodes)) {
                orderById.put(order.getId(), order);
            }
        }

        return orderById.values().stream()
                .map(OrderOperationMapper::toView)
                .toList();
    }

    @Override
    public List<OrderOperationView> findPickupCandidates(InternalPickupCandidateRequest request, Long tenantId) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<OrderStatus> statuses = request.getStatuses() == null
                ? List.of()
                : request.getStatuses().stream().filter(status -> status != null).distinct().toList();
        if (statuses.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "statuses must contain at least one status.");
        }

        int limit = normalizeLimit(request.getLimit());
        return orderRepository.findPickupCandidateOrders(
                        tenantId,
                        statuses,
                        normalizeText(request.getPostOfficeCode()),
                        request.getHorizonStart(),
                        request.getHorizonEnd(),
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(OrderOperationMapper::toView)
                .toList();
    }

    private Set<Long> normalizeOrderIds(List<Long> orderIds) {
        Set<Long> normalized = new LinkedHashSet<>();
        if (orderIds != null) {
            for (Long orderId : orderIds) {
                if (orderId != null && orderId > 0) {
                    normalized.add(orderId);
                }
            }
        }
        return normalized;
    }

    private Set<String> normalizeOrderCodes(List<String> orderCodes) {
        Set<String> normalized = new LinkedHashSet<>();
        if (orderCodes != null) {
            for (String orderCode : orderCodes) {
                String value = normalizeText(orderCode);
                if (value != null) {
                    normalized.add(value.toUpperCase(Locale.ROOT));
                }
            }
        }
        return normalized;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_PICKUP_CANDIDATE_LIMIT;
        }
        return Math.min(limit, MAX_PICKUP_CANDIDATE_LIMIT);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
