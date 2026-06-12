/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.dto.request.DashboardFilterRequest;
import serp.project.tms_order.dto.response.dashboard.DashboardAlertResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardAlertsResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardBreakdownItemResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardDeliverySuccessResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardFinanceResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardFirstMileResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardLastMileResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardLegsResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardMiddleMileResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardOrderVolumeResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardOverviewResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardPeriodResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardScopeResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardStatusResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardTopEntityResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardTrendPointResponse;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.repository.projection.OrderDashboardSliceProjection;
import serp.project.tms_order.service.DashboardReportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardReportServiceImpl implements DashboardReportService {
    private static final String DEFAULT_TIMEZONE = "Asia/Saigon";
    private static final String DEFAULT_GRANULARITY = "DAY";
    private static final int MAX_DATE_RANGE_DAYS = 90;
    private static final int TOP_LIMIT = 5;

    private static final Set<OrderStatus> NEW_STATUSES = EnumSet.of(OrderStatus.CREATED);
    private static final Set<OrderStatus> COMPLETED_STATUSES = EnumSet.of(OrderStatus.DELIVERED);
    private static final Set<OrderStatus> CANCELLED_STATUSES = EnumSet.of(OrderStatus.CANCELLED);
    private static final Set<OrderStatus> RETURNED_STATUSES = EnumSet.of(OrderStatus.RETURNED_TO_SENDER);
    private static final Set<OrderStatus> FAILED_DELIVERY_STATUSES = EnumSet.of(
            OrderStatus.DELIVERY_FAILED,
            OrderStatus.LOST_OR_DAMAGED
    );
    private static final Set<OrderStatus> TERMINAL_STATUSES = EnumSet.of(
            OrderStatus.DELIVERED,
            OrderStatus.DELIVERY_FAILED,
            OrderStatus.RETURNED_TO_SENDER,
            OrderStatus.CANCELLED,
            OrderStatus.LOST_OR_DAMAGED
    );
    private static final Set<OrderStatus> FIRST_MILE_STATUSES = EnumSet.of(
            OrderStatus.CREATED,
            OrderStatus.ASSIGNED_TO_PICKUP,
            OrderStatus.PICKING_UP,
            OrderStatus.PICKUP_FAILED,
            OrderStatus.PICKED_UP,
            OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND,
            OrderStatus.AT_ORIGIN_POST_OFFICE,
            OrderStatus.OUTBOUND_READY_FROM_PO,
            OrderStatus.INBOUND_AT_ORIGIN_HUB
    );
    private static final Set<OrderStatus> PICKUP_SUCCESS_STATUSES = EnumSet.of(
            OrderStatus.PICKED_UP,
            OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND,
            OrderStatus.AT_ORIGIN_POST_OFFICE,
            OrderStatus.OUTBOUND_READY_FROM_PO,
            OrderStatus.INBOUND_AT_ORIGIN_HUB,
            OrderStatus.BAGGING_IN_PROGRESS,
            OrderStatus.BAGGED,
            OrderStatus.BAG_SEALED,
            OrderStatus.BAG_IN_TRANSIT,
            OrderStatus.INBOUND_AT_DESTINATION_HUB,
            OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE,
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED
    );
    private static final Set<OrderStatus> PICKUP_PENDING_STATUSES = EnumSet.of(
            OrderStatus.CREATED,
            OrderStatus.ASSIGNED_TO_PICKUP,
            OrderStatus.PICKING_UP,
            OrderStatus.PICKUP_FAILED
    );
    private static final Set<OrderStatus> MIDDLE_MILE_STATUSES = EnumSet.of(
            OrderStatus.INBOUND_AT_ORIGIN_HUB,
            OrderStatus.BAGGING_IN_PROGRESS,
            OrderStatus.BAGGED,
            OrderStatus.BAG_SEALED,
            OrderStatus.BAG_IN_TRANSIT,
            OrderStatus.INBOUND_AT_DESTINATION_HUB
    );
    private static final Set<OrderStatus> LAST_MILE_STATUSES = EnumSet.of(
            OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE,
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED,
            OrderStatus.DELIVERY_FAILED,
            OrderStatus.RETURNED_TO_SENDER
    );

    private final OrderRepository orderRepository;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview(DashboardFilterRequest filterRequest, Long tenantId) {
        ResolvedFilter filter = resolveFilter(filterRequest);
        List<OrderDashboardSliceProjection> orders = loadOrders(tenantId, filter);
        long previousOrders = countPreviousPeriod(tenantId, filter);
        String now = ZonedDateTime.now(filter.zoneId()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return new DashboardOverviewResponse(
                resolveScope(filter),
                resolvePeriod(filter),
                buildOrderVolume(orders, previousOrders),
                buildStatuses(orders, now),
                buildDeliverySuccess(orders),
                buildFinance(orders),
                now
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardLegsResponse getLegs(DashboardFilterRequest filterRequest, Long tenantId) {
        ResolvedFilter filter = resolveFilter(filterRequest);
        List<OrderDashboardSliceProjection> orders = loadOrders(tenantId, filter);
        String now = ZonedDateTime.now(filter.zoneId()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return new DashboardLegsResponse(
                resolveScope(filter),
                resolvePeriod(filter),
                buildFirstMile(orders, filter),
                buildMiddleMile(orders),
                buildLastMile(orders, filter),
                now
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardAlertsResponse getAlerts(DashboardFilterRequest filterRequest, Long tenantId, int size) {
        ResolvedFilter filter = resolveFilter(filterRequest);
        List<OrderDashboardSliceProjection> orders = loadOrders(tenantId, filter);
        String now = ZonedDateTime.now(filter.zoneId()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        List<DashboardAlertResponse> alerts = buildAlerts(orders, filter, Math.max(1, Math.min(size, 50)));

        return new DashboardAlertsResponse(resolveScope(filter), resolvePeriod(filter), alerts, now);
    }

    private DashboardOrderVolumeResponse buildOrderVolume(
            List<OrderDashboardSliceProjection> orders,
            long previousOrders
    ) {
        long total = orders.size();
        long newOrders = countByStatuses(orders, NEW_STATUSES);
        long completed = countByStatuses(orders, COMPLETED_STATUSES);
        long cancelled = countByStatuses(orders, CANCELLED_STATUSES);
        long returned = countByStatuses(orders, RETURNED_STATUSES);
        long inProgress = orders.stream()
                .map(OrderDashboardSliceProjection::getStatus)
                .filter(Objects::nonNull)
                .filter(status -> !NEW_STATUSES.contains(status)
                        && !TERMINAL_STATUSES.contains(status))
                .count();

        return new DashboardOrderVolumeResponse(
                total,
                newOrders,
                inProgress,
                completed,
                cancelled,
                returned,
                growthRate(total, previousOrders)
        );
    }

    private List<DashboardStatusResponse> buildStatuses(
            List<OrderDashboardSliceProjection> orders,
            String lastUpdatedAt
    ) {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderDashboardSliceProjection order : orders) {
            if (order.getStatus() != null) {
                counts.merge(order.getStatus(), 1L, Long::sum);
            }
        }

        long total = orders.size();
        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardStatusResponse(
                        entry.getKey().name(),
                        humanize(entry.getKey().name()),
                        entry.getValue(),
                        percentage(entry.getValue(), total),
                        lastUpdatedAt
                ))
                .toList();
    }

    private DashboardDeliverySuccessResponse buildDeliverySuccess(
            List<OrderDashboardSliceProjection> orders
    ) {
        long delivered = countByStatuses(orders, COMPLETED_STATUSES);
        long failed = countByStatuses(orders, FAILED_DELIVERY_STATUSES);
        long returned = countByStatuses(orders, RETURNED_STATUSES);
        long denominator = delivered + failed + returned;

        List<DashboardBreakdownItemResponse> failedReasons = List.of(
                new DashboardBreakdownItemResponse(
                        "DELIVERY_FAILED",
                        "Delivery failed",
                        countByStatuses(orders, EnumSet.of(OrderStatus.DELIVERY_FAILED)),
                        percentage(countByStatuses(orders, EnumSet.of(OrderStatus.DELIVERY_FAILED)), failed)
                ),
                new DashboardBreakdownItemResponse(
                        "LOST_OR_DAMAGED",
                        "Lost or damaged",
                        countByStatuses(orders, EnumSet.of(OrderStatus.LOST_OR_DAMAGED)),
                        percentage(countByStatuses(orders, EnumSet.of(OrderStatus.LOST_OR_DAMAGED)), failed)
                )
        );

        return new DashboardDeliverySuccessResponse(
                delivered,
                failed,
                returned,
                percentage(delivered, denominator),
                failedReasons
        );
    }

    private DashboardFinanceResponse buildFinance(List<OrderDashboardSliceProjection> orders) {
        long grossRevenue = orders.stream().mapToLong(this::shippingFee).sum();
        long codAmount = orders.stream().mapToLong(order -> nullToZero(order.getCodAmount())).sum();
        long codCollected = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .mapToLong(order -> nullToZero(order.getCodAmount()))
                .sum();

        return new DashboardFinanceResponse(
                grossRevenue,
                grossRevenue,
                codAmount,
                codCollected,
                0,
                Math.max(codAmount - codCollected, 0),
                "VND",
                true,
                "tms-order order fields"
        );
    }

    private DashboardFirstMileResponse buildFirstMile(
            List<OrderDashboardSliceProjection> orders,
            ResolvedFilter filter
    ) {
        List<OrderDashboardSliceProjection> firstMileOrders = filterByStatuses(orders, FIRST_MILE_STATUSES);
        long pickupSuccess = countByStatuses(orders, PICKUP_SUCCESS_STATUSES);
        long pickupFailed = countByStatuses(orders, EnumSet.of(OrderStatus.PICKUP_FAILED));
        long pickupDenominator = pickupSuccess + pickupFailed;

        return new DashboardFirstMileResponse(
                firstMileOrders.size(),
                percentage(pickupSuccess, pickupDenominator),
                null,
                countPickupSlaBreaches(orders, filter.now()),
                buildBreakdown(firstMileOrders),
                buildTrend(firstMileOrders, filter),
                buildTopPostOffices(firstMileOrders, true)
        );
    }

    private DashboardMiddleMileResponse buildMiddleMile(List<OrderDashboardSliceProjection> orders) {
        List<OrderDashboardSliceProjection> middleMileOrders = filterByStatuses(orders, MIDDLE_MILE_STATUSES);
        return new DashboardMiddleMileResponse(
                middleMileOrders.size(),
                0,
                0,
                0,
                null,
                buildBreakdown(middleMileOrders),
                List.of()
        );
    }

    private DashboardLastMileResponse buildLastMile(
            List<OrderDashboardSliceProjection> orders,
            ResolvedFilter filter
    ) {
        List<OrderDashboardSliceProjection> lastMileOrders = filterByStatuses(orders, LAST_MILE_STATUSES);
        DashboardDeliverySuccessResponse success = buildDeliverySuccess(lastMileOrders);
        return new DashboardLastMileResponse(
                lastMileOrders.size(),
                success.successRatePercent(),
                null,
                success.failedReasons(),
                countDeliverySlaBreaches(lastMileOrders, filter.now()),
                buildTrend(lastMileOrders, filter),
                buildBreakdown(lastMileOrders)
        );
    }

    private List<DashboardAlertResponse> buildAlerts(
            List<OrderDashboardSliceProjection> orders,
            ResolvedFilter filter,
            int size
    ) {
        List<DashboardAlertResponse> alerts = new ArrayList<>();
        LocalDateTime now = filter.now();

        for (OrderDashboardSliceProjection order : orders) {
            if (isPickupSlaBreached(order, now)) {
                alerts.add(toAlert(
                        order,
                        "PICKUP_SLA_BREACH",
                        "HIGH",
                        "Pickup SLA breached",
                        "Pickup window has passed while the order is not picked up.",
                        "FIRST_MILE",
                        order.getPickupTimeEnd()
                ));
            } else if (order.getStatus() == OrderStatus.DELIVERY_FAILED) {
                alerts.add(toAlert(
                        order,
                        "DELIVERY_FAILED",
                        "HIGH",
                        "Delivery failed",
                        "Delivery failed and needs follow-up or return handling.",
                        "LAST_MILE",
                        order.getUpdatedAt()
                ));
            } else if (isStuck(order, now)) {
                alerts.add(toAlert(
                        order,
                        "ORDER_STUCK",
                        "MEDIUM",
                        "Order status is stale",
                        "Order has not moved for more than 48 hours.",
                        resolveLeg(order.getStatus()),
                        order.getUpdatedAt()
                ));
            }
        }

        return alerts.stream()
                .sorted(Comparator.comparing(DashboardAlertResponse::occurredAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(size)
                .toList();
    }

    private DashboardAlertResponse toAlert(
            OrderDashboardSliceProjection order,
            String type,
            String severity,
            String title,
            String description,
            String leg,
            LocalDateTime occurredAt
    ) {
        String statusCode = order.getStatus() == null ? null : order.getStatus().name();
        String entityCode = firstText(order.getOrderCode(), order.getCustomerOrderCode());
        return new DashboardAlertResponse(
                type + "-" + order.getId(),
                type,
                severity,
                title,
                description,
                "ORDER",
                order.getId(),
                entityCode,
                statusCode,
                leg,
                occurredAt == null ? null : occurredAt.toString(),
                occurredAt == null ? null : occurredAt.toString(),
                "/first-mile/orders?orderCode=" + urlValue(entityCode)
        );
    }

    private List<DashboardBreakdownItemResponse> buildBreakdown(
            List<OrderDashboardSliceProjection> orders
    ) {
        long total = orders.size();
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderDashboardSliceProjection order : orders) {
            if (order.getStatus() != null) {
                counts.merge(order.getStatus(), 1L, Long::sum);
            }
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardBreakdownItemResponse(
                        entry.getKey().name(),
                        humanize(entry.getKey().name()),
                        entry.getValue(),
                        percentage(entry.getValue(), total)
                ))
                .toList();
    }

    private List<DashboardTrendPointResponse> buildTrend(
            List<OrderDashboardSliceProjection> orders,
            ResolvedFilter filter
    ) {
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        LocalDate cursor = filter.fromDate();
        while (!cursor.isAfter(filter.toDate())) {
            counts.put(cursor, 0L);
            cursor = nextBucket(cursor, filter.granularity());
        }

        for (OrderDashboardSliceProjection order : orders) {
            if (order.getCreatedAt() == null) {
                continue;
            }
            LocalDate bucket = bucketDate(order.getCreatedAt().toLocalDate(), filter.granularity());
            counts.computeIfPresent(bucket, (key, value) -> value + 1);
        }

        return counts.entrySet().stream()
                .map(entry -> new DashboardTrendPointResponse(
                        trendLabel(entry.getKey(), filter.granularity()),
                        entry.getKey().toString(),
                        entry.getValue()
                ))
                .toList();
    }

    private List<DashboardTopEntityResponse> buildTopPostOffices(
            List<OrderDashboardSliceProjection> orders,
            boolean origin
    ) {
        Map<String, Long> counts = new HashMap<>();
        for (OrderDashboardSliceProjection order : orders) {
            String code = origin ? order.getOriginPostOfficeCode() : order.getDestinationPostOfficeCode();
            if (hasText(code)) {
                counts.merge(code.trim().toUpperCase(Locale.ROOT), 1L, Long::sum);
            }
        }

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_LIMIT)
                .map(entry -> new DashboardTopEntityResponse(
                        entry.getKey(),
                        entry.getKey(),
                        entry.getValue(),
                        percentage(entry.getValue(), total)
                ))
                .toList();
    }

    private ResolvedFilter resolveFilter(DashboardFilterRequest request) {
        ZoneId zoneId = resolveZoneId(request.getTimezone());
        LocalDate today = LocalDate.now(zoneId);
        LocalDate fromDate = request.getFromDate() == null ? today.minusDays(6) : request.getFromDate();
        LocalDate toDate = request.getToDate() == null ? today : request.getToDate();
        if (fromDate.isAfter(toDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        long dateRangeDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (dateRangeDays > MAX_DATE_RANGE_DAYS) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String granularity = hasText(request.getGranularity())
                ? request.getGranularity().trim().toUpperCase(Locale.ROOT)
                : DEFAULT_GRANULARITY;
        if (!Set.of("DAY", "WEEK", "MONTH").contains(granularity)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<String> postOfficeCodes = resolvePostOfficeCodes(request);
        return new ResolvedFilter(
                fromDate,
                toDate,
                zoneId,
                granularity,
                request.getHubId(),
                postOfficeCodes,
                request.getServiceType(),
                LocalDateTime.now(zoneId)
        );
    }

    private List<OrderDashboardSliceProjection> loadOrders(Long tenantId, ResolvedFilter filter) {
        List<String> postOfficeCodes = queryPostOfficeCodes(filter.postOfficeCodes());
        return orderRepository.findDashboardSlices(
                tenantId,
                filter.fromDate().atStartOfDay(),
                filter.toDate().plusDays(1).atStartOfDay(),
                postOfficeCodes,
                filter.postOfficeCodes().isEmpty(),
                filter.serviceType()
        );
    }

    private long countPreviousPeriod(Long tenantId, ResolvedFilter filter) {
        long days = ChronoUnit.DAYS.between(filter.fromDate(), filter.toDate()) + 1;
        LocalDate previousToDate = filter.fromDate().minusDays(1);
        LocalDate previousFromDate = previousToDate.minusDays(days - 1);
        List<String> postOfficeCodes = queryPostOfficeCodes(filter.postOfficeCodes());
        return orderRepository.countDashboardOrders(
                tenantId,
                previousFromDate.atStartOfDay(),
                previousToDate.plusDays(1).atStartOfDay(),
                postOfficeCodes,
                filter.postOfficeCodes().isEmpty(),
                filter.serviceType()
        );
    }

    private DashboardScopeResponse resolveScope(ResolvedFilter filter) {
        List<String> roles = authUtils.getAllRoles().stream()
                .filter(Objects::nonNull)
                .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
                .distinct()
                .toList();
        String accessLevel = "VIEWER";
        if (roles.contains("TMS_ADMIN")) {
            accessLevel = "ADMIN";
        } else if (roles.contains("TMS_HUB_MANAGER")) {
            accessLevel = "HUB_MANAGER";
        } else if (roles.contains("TMS_POSTOFFICER_MANAGER")) {
            accessLevel = "POST_OFFICE_MANAGER";
        }

        List<Long> hubIds = filter.hubId() == null ? List.of() : List.of(filter.hubId());
        return new DashboardScopeResponse(accessLevel, hubIds, filter.postOfficeCodes(), roles);
    }

    private DashboardPeriodResponse resolvePeriod(ResolvedFilter filter) {
        return new DashboardPeriodResponse(
                filter.fromDate().toString(),
                filter.toDate().toString(),
                filter.zoneId().getId(),
                filter.granularity()
        );
    }

    private List<OrderDashboardSliceProjection> filterByStatuses(
            List<OrderDashboardSliceProjection> orders,
            Set<OrderStatus> statuses
    ) {
        return orders.stream()
                .filter(order -> order.getStatus() != null)
                .filter(order -> statuses.contains(order.getStatus()))
                .toList();
    }

    private long countByStatuses(
            List<OrderDashboardSliceProjection> orders,
            Set<OrderStatus> statuses
    ) {
        return orders.stream()
                .filter(order -> order.getStatus() != null)
                .filter(order -> statuses.contains(order.getStatus()))
                .count();
    }

    private long countPickupSlaBreaches(
            List<OrderDashboardSliceProjection> orders,
            LocalDateTime now
    ) {
        return orders.stream().filter(order -> isPickupSlaBreached(order, now)).count();
    }

    private long countDeliverySlaBreaches(
            List<OrderDashboardSliceProjection> orders,
            LocalDateTime now
    ) {
        return orders.stream().filter(order -> isDeliverySlaBreached(order, now)).count();
    }

    private boolean isPickupSlaBreached(OrderDashboardSliceProjection order, LocalDateTime now) {
        return order.getPickupTimeEnd() != null
                && order.getPickupTimeEnd().isBefore(now)
                && order.getStatus() != null
                && PICKUP_PENDING_STATUSES.contains(order.getStatus());
    }

    private boolean isDeliverySlaBreached(OrderDashboardSliceProjection order, LocalDateTime now) {
        return order.getUpdatedAt() != null
                && order.getUpdatedAt().plusHours(24).isBefore(now)
                && order.getStatus() != null
                && (order.getStatus() == OrderStatus.READY_FOR_DELIVERY
                || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY
                || order.getStatus() == OrderStatus.DELIVERY_FAILED);
    }

    private boolean isStuck(OrderDashboardSliceProjection order, LocalDateTime now) {
        return order.getUpdatedAt() != null
                && order.getUpdatedAt().plusHours(48).isBefore(now)
                && order.getStatus() != null
                && !TERMINAL_STATUSES.contains(order.getStatus());
    }

    private String resolveLeg(OrderStatus status) {
        if (status == null) {
            return "ALL";
        }
        if (FIRST_MILE_STATUSES.contains(status)) {
            return "FIRST_MILE";
        }
        if (MIDDLE_MILE_STATUSES.contains(status)) {
            return "MIDDLE_MILE";
        }
        if (LAST_MILE_STATUSES.contains(status)) {
            return "LAST_MILE";
        }
        return "ALL";
    }

    private ZoneId resolveZoneId(String timezone) {
        try {
            return ZoneId.of(hasText(timezone) ? timezone.trim() : DEFAULT_TIMEZONE);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private List<String> resolvePostOfficeCodes(DashboardFilterRequest request) {
        List<String> values = new ArrayList<>();
        if (hasText(request.getPostOfficeCode())) {
            values.add(request.getPostOfficeCode());
        }
        if (request.getPostOfficeCodes() != null) {
            values.addAll(request.getPostOfficeCodes());
        }
        return values.stream()
                .filter(this::hasText)
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    private List<String> queryPostOfficeCodes(List<String> postOfficeCodes) {
        return postOfficeCodes.isEmpty() ? List.of("__NO_POST_OFFICE__") : postOfficeCodes;
    }

    private LocalDate nextBucket(LocalDate date, String granularity) {
        return switch (granularity) {
            case "WEEK" -> date.plusWeeks(1);
            case "MONTH" -> date.plusMonths(1);
            default -> date.plusDays(1);
        };
    }

    private LocalDate bucketDate(LocalDate date, String granularity) {
        return switch (granularity) {
            case "WEEK" -> date.with(WeekFields.ISO.dayOfWeek(), 1);
            case "MONTH" -> date.withDayOfMonth(1);
            default -> date;
        };
    }

    private String trendLabel(LocalDate date, String granularity) {
        return switch (granularity) {
            case "WEEK" -> "Week " + date.get(WeekFields.ISO.weekOfWeekBasedYear());
            case "MONTH" -> date.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
            default -> date.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH));
        };
    }

    private long shippingFee(OrderDashboardSliceProjection order) {
        if (order.getTotalShippingFee() != null) {
            return order.getTotalShippingFee();
        }
        return nullToZero(order.getBaseShippingFee())
                + nullToZero(order.getCodFee())
                + nullToZero(order.getExtraFee());
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }

    private double growthRate(long current, long previous) {
        if (previous == 0) {
            return current == 0 ? 0 : 100;
        }
        return round(((double) (current - previous) / previous) * 100);
    }

    private double percentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return round(((double) numerator / denominator) * 100);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String humanize(String value) {
        if (!hasText(value)) {
            return "";
        }
        String[] parts = value.toLowerCase(Locale.ROOT).split("_");
        List<String> words = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                words.add(part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1));
            }
        }
        return String.join(" ", words);
    }

    private String firstText(String primary, String fallback) {
        return hasText(primary) ? primary : fallback;
    }

    private String urlValue(String value) {
        return value == null ? "" : value.replace(" ", "%20");
    }

    private record ResolvedFilter(
            LocalDate fromDate,
            LocalDate toDate,
            ZoneId zoneId,
            String granularity,
            Long hubId,
            List<String> postOfficeCodes,
            serp.project.tms_order.enums.OrderType serviceType,
            LocalDateTime now
    ) {
    }
}
