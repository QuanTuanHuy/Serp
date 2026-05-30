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
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.domain.BagOrder;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.Order;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AddBagOrderRequest;
import serp.project.second_mile.dto.request.AutoBaggingPlanRequest;
import serp.project.second_mile.dto.request.BagFilterRequest;
import serp.project.second_mile.dto.request.CreateBagRequest;
import serp.project.second_mile.dto.request.ReopenBagRequest;
import serp.project.second_mile.dto.request.UpdateBagRequest;
import serp.project.second_mile.dto.request.ValidateBaggingRequest;
import serp.project.second_mile.dto.response.AutoBaggingPlanItemResponse;
import serp.project.second_mile.dto.response.AutoBaggingPlanResponse;
import serp.project.second_mile.dto.response.BagSuggestionResponse;
import serp.project.second_mile.dto.response.BaggingKpiResponse;
import serp.project.second_mile.dto.response.BaggingValidationItemResponse;
import serp.project.second_mile.dto.response.BaggingValidationResponse;
import serp.project.second_mile.dto.response.BagResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.kafka.OrderSyncEventPublisher;
import serp.project.second_mile.mapper.BagMapper;
import serp.project.second_mile.repository.BagOrderRepository;
import serp.project.second_mile.repository.BagRepository;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.OrderRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.repository.specification.BagSpecification;
import serp.project.second_mile.service.BagService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BagServiceImpl implements BagService {
    private static final double DEFAULT_BAG_MAX_WEIGHT = 50.0;
    private static final double DEFAULT_BAG_MAX_VOLUME = 0.5;
    private static final int DEFAULT_BAG_MAX_ORDERS = 30;
    private static final DateTimeFormatter AUTO_BAG_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BagRepository bagRepository;
    private final BagOrderRepository bagOrderRepository;
    private final HubRepository hubRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;
    private final OrderSyncEventPublisher orderSyncEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BagResponse> getBags(int page, int size, BagFilterRequest filterRequest) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        BagFilterRequest normalizedFilterRequest = normalizeFilterRequest(filterRequest);

        Page<Bag> bagPage = bagRepository.findAll(
                BagSpecification.byFilter(tenantId, normalizedFilterRequest),
                pageable
        );

        Page<BagResponse> mappedPage = bagPage.map(this::toBagResponse);
        return PageResponse.<BagResponse>builder()
                .items(mappedPage.getContent())
                .page(mappedPage.getNumber())
                .size(mappedPage.getSize())
                .totalElements(mappedPage.getTotalElements())
                .totalPages(mappedPage.getTotalPages())
                .hasNext(mappedPage.hasNext())
                .hasPrevious(mappedPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BagResponse getBagById(Long id) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Bag bag = getBagOrThrow(id);
        validateTenantAccess(bag);
        return toBagResponse(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse createBag(CreateBagRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        String normalizedBagCode = normalizeText(request.getBagCode());
        if (normalizedBagCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (bagRepository.existsByTenantIdAndBagCodeIgnoreCase(tenantId, normalizedBagCode)) {
            throw new AppException(ErrorCode.BAG_CODE_EXISTED);
        }

        validateRouteAndTransport(tenantId, request.getOriginHubId(), request.getDestinationType(),
                request.getDestinationHubId(), request.getDestinationPostOfficeCode(), request.getVehicleId());

        Bag bag = BagMapper.toEntity(request);
        bag.setBagCode(normalizedBagCode);
        bag.setDestinationPostOfficeCode(normalizeText(request.getDestinationPostOfficeCode()));
        bag.setStatus(BagStatus.CREATED);
        bag.setSealedAt(null);
        bag.setMaxWeight(normalizePositiveOrDefault(request.getMaxWeight(), DEFAULT_BAG_MAX_WEIGHT));
        bag.setMaxVolume(normalizePositiveOrDefault(request.getMaxVolume(), DEFAULT_BAG_MAX_VOLUME));
        bag.setMaxOrders(normalizePositiveOrDefault(request.getMaxOrders(), DEFAULT_BAG_MAX_ORDERS));
        bag.setCurrentWeight(0.0);
        bag.setCurrentVolume(0.0);
        bag.setCurrentOrders(0);
        bag.setTenantId(tenantId);

        Bag savedBag = bagRepository.save(bag);
        return toBagResponse(savedBag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse updateBag(Long id, UpdateBagRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(id);
        validateTenantAccess(bag);
        validateBagEditable(bag);

        String normalizedBagCode = normalizeText(request.getBagCode());
        if (normalizedBagCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!bag.getBagCode().equalsIgnoreCase(normalizedBagCode)
                && bagRepository.existsByTenantIdAndBagCodeIgnoreCase(tenantId, normalizedBagCode)) {
            throw new AppException(ErrorCode.BAG_CODE_EXISTED);
        }

        validateRouteAndTransport(tenantId, request.getOriginHubId(), request.getDestinationType(),
                request.getDestinationHubId(), request.getDestinationPostOfficeCode(), request.getVehicleId());

        BagMapper.mapForUpdate(request, bag);
        bag.setBagCode(normalizedBagCode);
        bag.setDestinationPostOfficeCode(normalizeText(request.getDestinationPostOfficeCode()));
        bag.setStatus(BagStatus.CREATED);
        bag.setMaxWeight(normalizePositiveOrDefault(request.getMaxWeight(), DEFAULT_BAG_MAX_WEIGHT));
        bag.setMaxVolume(normalizePositiveOrDefault(request.getMaxVolume(), DEFAULT_BAG_MAX_VOLUME));
        bag.setMaxOrders(normalizePositiveOrDefault(request.getMaxOrders(), DEFAULT_BAG_MAX_ORDERS));
        recalculateBagMetrics(bag, tenantId);
        bag.setTenantId(tenantId);

        Bag updatedBag = bagRepository.save(bag);
        return toBagResponse(updatedBag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBag(Long id) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Bag bag = getBagOrThrow(id);
        validateTenantAccess(bag);
        validateBagEditable(bag);
        bagRepository.delete(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse addOrderToBag(Long bagId, AddBagOrderRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(bagId);
        validateTenantAccess(bag);
        validateBagEditable(bag);
        validateBagCanAcceptOrders(bag);

        String normalizedOrderCode = normalizeText(request.getOrderCode());
        if (normalizedOrderCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Order order = orderRepository.findByOrderCodeIgnoreCaseAndTenantId(normalizedOrderCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_ORDER_NOT_FOUND));

        if (bagOrderRepository.existsByBag_IdAndOrder_IdAndTenantId(bag.getId(), order.getId(), tenantId)) {
            throw new AppException(ErrorCode.BAG_ORDER_ALREADY_IN_BAG);
        }

        if (bagOrderRepository.existsByOrder_IdAndTenantId(order.getId(), tenantId)) {
            throw new AppException(ErrorCode.BAG_ORDER_ALREADY_ASSIGNED);
        }

        validateOrderForBagAssignment(tenantId, bag, order, 0.0, 0.0, 0);

        BagOrder bagOrder = BagOrder.builder()
                .bag(bag)
                .order(order)
                .tenantId(tenantId)
                .build();
        bagOrderRepository.save(bagOrder);

        order.setStatus(OrderStatus.BAGGED);
        orderRepository.save(order);
        orderSyncEventPublisher.publish(order);
        recalculateBagMetrics(bag, tenantId);
        bagRepository.save(bag);

        return toBagResponse(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse removeOrderFromBag(Long bagId, String orderCode) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(bagId);
        validateTenantAccess(bag);
        validateBagEditable(bag);
        validateBagCanAcceptOrders(bag);

        String normalizedOrderCode = normalizeText(orderCode);
        if (normalizedOrderCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        BagOrder bagOrder = bagOrderRepository.findByBag_IdAndOrder_OrderCodeIgnoreCaseAndTenantId(
                        bag.getId(),
                        normalizedOrderCode,
                        tenantId
                )
                .orElseThrow(() -> new AppException(ErrorCode.BAG_ORDER_MAPPING_NOT_FOUND));

        bagOrderRepository.delete(bagOrder);
        Order order = bagOrder.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.INBOUND_AT_ORIGIN_HUB);
            orderRepository.save(order);
            orderSyncEventPublisher.publish(order);
        }
        recalculateBagMetrics(bag, tenantId);
        bagRepository.save(bag);
        return toBagResponse(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse sealBag(Long bagId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(bagId);
        validateTenantAccess(bag);
        if (bag.getStatus() != BagStatus.CREATED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }

        recalculateBagMetrics(bag, tenantId);
        if (safeInt(bag.getCurrentOrders()) <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Cannot seal an empty bag.");
        }

        bag.setStatus(BagStatus.SEALED);
        bag.setSealedAt(LocalDateTime.now());
        Bag savedBag = bagRepository.save(bag);

        List<BagOrder> bagOrders = bagOrderRepository.findByBag_IdAndTenantId(savedBag.getId(), tenantId);
        List<Order> updatedOrders = new ArrayList<>();
        for (BagOrder bagOrder : bagOrders) {
            if (bagOrder.getOrder() == null) {
                continue;
            }
            bagOrder.getOrder().setStatus(OrderStatus.BAG_SEALED);
            updatedOrders.add(bagOrder.getOrder());
        }
        if (!updatedOrders.isEmpty()) {
            orderRepository.saveAll(updatedOrders);
            orderSyncEventPublisher.publishAll(updatedOrders);
        }
        return toBagResponse(savedBag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse reopenBag(Long bagId, ReopenBagRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        String reason = request == null ? null : normalizeText(request.getReason());
        if (reason == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(bagId);
        validateTenantAccess(bag);

        if (bag.getStatus() != BagStatus.SEALED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }

        bag.setStatus(BagStatus.CREATED);
        bag.setSealedAt(null);
        bag.setNote(reason);
        Bag savedBag = bagRepository.save(bag);

        List<BagOrder> bagOrders = bagOrderRepository.findByBag_IdAndTenantId(savedBag.getId(), tenantId);
        List<Order> updatedOrders = new ArrayList<>();
        for (BagOrder bagOrder : bagOrders) {
            if (bagOrder.getOrder() == null) {
                continue;
            }
            bagOrder.getOrder().setStatus(OrderStatus.BAGGED);
            updatedOrders.add(bagOrder.getOrder());
        }
        if (!updatedOrders.isEmpty()) {
            orderRepository.saveAll(updatedOrders);
            orderSyncEventPublisher.publishAll(updatedOrders);
        }
        return toBagResponse(savedBag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BagSuggestionResponse> suggestBags(String orderCode, Long originHubId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        String normalizedOrderCode = normalizeText(orderCode);
        if (normalizedOrderCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Order order = orderRepository.findByOrderCodeIgnoreCaseAndTenantId(normalizedOrderCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_ORDER_NOT_FOUND));
        if (bagOrderRepository.existsByOrder_IdAndTenantId(order.getId(), tenantId)) {
            return List.of();
        }

        Long resolvedOriginHubId = originHubId != null ? originHubId : resolveOriginHubIdByOrder(tenantId, order);
        BagDestinationTarget destinationTarget = resolveDestinationTargetForOrder(tenantId, order);
        List<Bag> candidates = findEditableBagsByTarget(tenantId, resolvedOriginHubId, destinationTarget);

        return candidates.stream()
                .filter(candidate -> canFit(candidate, order, 0.0, 0.0, 0))
                .map(candidate -> new BagSuggestionResponse(
                        candidate.getId(),
                        candidate.getBagCode(),
                        remainingWeight(candidate),
                        remainingVolume(candidate),
                        remainingOrders(candidate)
                ))
                .sorted(Comparator
                        .comparing((BagSuggestionResponse item) -> nullSafeDouble(item.remainingWeight()))
                        .thenComparing(item -> nullSafeDouble(item.remainingVolume())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BaggingValidationResponse validateBagging(ValidateBaggingRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(request.getBagId());
        validateTenantAccess(bag);
        validateBagEditable(bag);

        List<String> normalizedOrderCodes = normalizeOrderCodes(request.getOrderCodes());
        if (normalizedOrderCodes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<BaggingValidationItemResponse> items = new ArrayList<>();
        double extraWeight = 0.0;
        double extraVolume = 0.0;
        int extraOrders = 0;
        int accepted = 0;

        for (String orderCode : normalizedOrderCodes) {
            Order order = orderRepository.findByOrderCodeIgnoreCaseAndTenantId(orderCode, tenantId).orElse(null);
            if (order == null) {
                items.add(new BaggingValidationItemResponse(orderCode, false, "Order not found."));
                continue;
            }
            if (bagOrderRepository.existsByOrder_IdAndTenantId(order.getId(), tenantId)) {
                items.add(new BaggingValidationItemResponse(orderCode, false, "Order already assigned to another bag."));
                continue;
            }
            try {
                validateOrderForBagAssignment(tenantId, bag, order, extraWeight, extraVolume, extraOrders);
                extraWeight += safeDouble(order.getTotalWeight());
                extraVolume += safeDouble(order.getTotalVolume());
                extraOrders += 1;
                accepted++;
                items.add(new BaggingValidationItemResponse(orderCode, true, null));
            } catch (AppException appException) {
                String reason = appException.getDetail() == null ? appException.getErrorCode().getMessageKey() : appException.getDetail();
                items.add(new BaggingValidationItemResponse(orderCode, false, reason));
            }
        }

        return new BaggingValidationResponse(
                bag.getId(),
                accepted,
                items.size() - accepted,
                items
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoBaggingPlanResponse autoPlanBags(AutoBaggingPlanRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        validateRouteAndTransport(
                tenantId,
                request.getOriginHubId(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                request.getDestinationPostOfficeCode(),
                null
        );

        List<String> normalizedOrderCodes = normalizeOrderCodes(request.getOrderCodes());
        if (normalizedOrderCodes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<Order> orders = new ArrayList<>();
        for (String orderCode : normalizedOrderCodes) {
            Order order = orderRepository.findByOrderCodeIgnoreCaseAndTenantId(orderCode, tenantId)
                    .orElseThrow(() -> new AppException(ErrorCode.BAG_ORDER_NOT_FOUND));
            if (bagOrderRepository.existsByOrder_IdAndTenantId(order.getId(), tenantId)) {
                throw new AppException(ErrorCode.BAG_ORDER_ALREADY_ASSIGNED);
            }
            if (order.getStatus() != OrderStatus.INBOUND_AT_ORIGIN_HUB && order.getStatus() != OrderStatus.BAGGING_IN_PROGRESS) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Only inbound orders can be auto-bagged.");
            }
            validateOrderDestinationMatchesTarget(tenantId, request.getDestinationType(), request.getDestinationHubId(),
                    request.getDestinationPostOfficeCode(), order);
            orders.add(order);
        }

        List<Order> sortedOrders = orders.stream()
                .sorted(Comparator.comparing((Order order) -> sizeScore(order, DEFAULT_BAG_MAX_WEIGHT, DEFAULT_BAG_MAX_VOLUME))
                        .reversed())
                .toList();

        List<AutoBagBin> bins = new ArrayList<>();
        for (Order order : sortedOrders) {
            AutoBagBin selected = null;
            for (AutoBagBin bin : bins) {
                if (bin.canFit(order)) {
                    selected = bin;
                    break;
                }
            }
            if (selected == null) {
                selected = new AutoBagBin(DEFAULT_BAG_MAX_WEIGHT, DEFAULT_BAG_MAX_VOLUME, DEFAULT_BAG_MAX_ORDERS);
                if (!selected.canFit(order)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST, "Order exceeds single bag capacity.");
                }
                bins.add(selected);
            }
            selected.add(order);
        }

        boolean execute = Boolean.TRUE.equals(request.getExecute());
        List<AutoBaggingPlanItemResponse> planItems = new ArrayList<>();
        if (!execute) {
            int index = 1;
            for (AutoBagBin bin : bins) {
                planItems.add(new AutoBaggingPlanItemResponse(
                        "PLAN-" + index++,
                        bin.orderCodes(),
                        bin.totalWeight,
                        bin.totalVolume
                ));
            }
            return new AutoBaggingPlanResponse(false, bins.size(), planItems);
        }

        List<Order> changedOrders = new ArrayList<>();
        int index = 1;
        for (AutoBagBin bin : bins) {
            String bagCode = generateAutoBagCode(tenantId, request.getOriginHubId(), index);
            Bag bag = Bag.builder()
                    .bagCode(bagCode)
                    .originHubId(request.getOriginHubId())
                    .destinationType(request.getDestinationType())
                    .destinationHubId(request.getDestinationHubId())
                    .destinationPostOfficeCode(normalizeText(request.getDestinationPostOfficeCode()))
                    .maxWeight(DEFAULT_BAG_MAX_WEIGHT)
                    .maxVolume(DEFAULT_BAG_MAX_VOLUME)
                    .maxOrders(DEFAULT_BAG_MAX_ORDERS)
                    .currentWeight(bin.totalWeight)
                    .currentVolume(bin.totalVolume)
                    .currentOrders(bin.orders.size())
                    .status(BagStatus.CREATED)
                    .tenantId(tenantId)
                    .build();
            Bag savedBag = bagRepository.save(bag);

            for (Order order : bin.orders) {
                bagOrderRepository.save(BagOrder.builder()
                        .bag(savedBag)
                        .order(order)
                        .tenantId(tenantId)
                        .build());
                order.setStatus(OrderStatus.BAGGED);
                changedOrders.add(order);
            }
            planItems.add(new AutoBaggingPlanItemResponse(
                    savedBag.getBagCode(),
                    bin.orderCodes(),
                    bin.totalWeight,
                    bin.totalVolume
            ));
            index++;
        }
        if (!changedOrders.isEmpty()) {
            orderRepository.saveAll(changedOrders);
            orderSyncEventPublisher.publishAll(changedOrders);
        }
        return new AutoBaggingPlanResponse(true, bins.size(), planItems);
    }

    @Override
    @Transactional(readOnly = true)
    public BaggingKpiResponse getBaggingKpi(Long originHubId, LocalDateTime from, LocalDateTime to) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        if (originHubId == null || from == null || to == null || from.isAfter(to)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        List<Bag> sealedBags = bagRepository.findByTenantIdAndOriginHubIdAndStatusAndSealedAtBetween(
                tenantId,
                originHubId,
                BagStatus.SEALED,
                from,
                to
        );
        if (sealedBags.isEmpty()) {
            return new BaggingKpiResponse(originHubId, 0, 0, 0, 0);
        }

        double sumWeightRate = 0.0;
        double sumVolumeRate = 0.0;
        double sumOrders = 0.0;
        for (Bag bag : sealedBags) {
            sumWeightRate += safeDouble(bag.getCurrentWeight()) / positiveOrDefault(bag.getMaxWeight(), DEFAULT_BAG_MAX_WEIGHT);
            sumVolumeRate += safeDouble(bag.getCurrentVolume()) / positiveOrDefault(bag.getMaxVolume(), DEFAULT_BAG_MAX_VOLUME);
            sumOrders += safeInt(bag.getCurrentOrders());
        }
        double size = sealedBags.size();
        return new BaggingKpiResponse(
                originHubId,
                sealedBags.size(),
                sumWeightRate / size,
                sumVolumeRate / size,
                sumOrders / size
        );
    }

    private Bag getBagOrThrow(Long id) {
        return bagRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_NOT_FOUND));
    }

    private BagResponse toBagResponse(Bag bag) {
        List<BagOrder> bagOrders = bagOrderRepository.findByBag_IdAndTenantId(
                bag.getId(),
                secondMileAccessUtils.getCurrentTenantIdOrThrow()
        );
        return BagMapper.toResponse(bag, bagOrders);
    }

    private void validateTenantAccess(Bag bag) {
        Long currentTenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        if (bag.getTenantId() == null || !bag.getTenantId().equals(currentTenantId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateRouteAndTransport(
            Long tenantId,
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode,
            Long vehicleId
    ) {
        if (originHubId == null) {
            throw new AppException(ErrorCode.BAG_HUB_INVALID);
        }

        Hub originHub = hubRepository.findById(originHubId)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_HUB_INVALID));
        if (!tenantId.equals(originHub.getTenantId())) {
            throw new AppException(ErrorCode.BAG_HUB_INVALID);
        }
        if (vehicleId != null) {
            validateVehicle(tenantId, vehicleId);
        }

        if (destinationType == BagDestinationType.HUB) {
            if (destinationHubId == null) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            Hub destinationHub = hubRepository.findById(destinationHubId)
                    .orElseThrow(() -> new AppException(ErrorCode.BAG_DESTINATION_INVALID));
            if (!tenantId.equals(destinationHub.getTenantId())) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            return;
        }

        if (destinationType == BagDestinationType.POST_OFFICE) {
            String normalizedPostOfficeCode = normalizeText(destinationPostOfficeCode);
            if (normalizedPostOfficeCode == null) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(tenantId, normalizedPostOfficeCode)
                    .orElseThrow(() -> new AppException(ErrorCode.BAG_POST_OFFICE_INVALID));
            return;
        }

        throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
    }

    private void validateVehicle(Long tenantId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_VEHICLE_INVALID));
        if (!tenantId.equals(vehicle.getTenantId()) || vehicle.getStatus() != VehicleStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAG_VEHICLE_INVALID);
        }
    }

    private void validateBagEditable(Bag bag) {
        if (bag.getStatus() != BagStatus.CREATED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }
    }

    private void validateBagCanAcceptOrders(Bag bag) {
        if (bag.getStatus() == BagStatus.ARRIVED
                || bag.getStatus() == BagStatus.CANCELLED
                || bag.getStatus() == BagStatus.IN_TRANSIT
                || bag.getStatus() == BagStatus.SEALED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }
    }

    private void validateOrderForBagAssignment(
            Long tenantId,
            Bag bag,
            Order order,
            double extraWeight,
            double extraVolume,
            int extraOrders
    ) {
        if (order.getStatus() != OrderStatus.INBOUND_AT_ORIGIN_HUB
                && order.getStatus() != OrderStatus.BAGGING_IN_PROGRESS
                && order.getStatus() != OrderStatus.BAGGED) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Order is not ready for bagging.");
        }

        Long originHubId = resolveOriginHubIdByOrder(tenantId, order);
        if (!Objects.equals(originHubId, bag.getOriginHubId())) {
            throw new AppException(ErrorCode.BAG_HUB_INVALID);
        }

        validateOrderDestinationMatchesBag(tenantId, bag, order);
        if (!canFit(bag, order, extraWeight, extraVolume, extraOrders)) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID, "Bag capacity exceeded.");
        }
    }

    private void validateOrderDestinationMatchesBag(Long tenantId, Bag bag, Order order) {
        validateOrderDestinationMatchesTarget(
                tenantId,
                bag.getDestinationType(),
                bag.getDestinationHubId(),
                bag.getDestinationPostOfficeCode(),
                order
        );
    }

    private void validateOrderDestinationMatchesTarget(
            Long tenantId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode,
            Order order
    ) {
        if (destinationType == BagDestinationType.POST_OFFICE) {
            String orderDestinationPo = normalizeText(order.getDestinationPostOfficeCode());
            if (orderDestinationPo == null
                    || !orderDestinationPo.equalsIgnoreCase(normalizeText(destinationPostOfficeCode))) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            return;
        }

        if (destinationType == BagDestinationType.HUB) {
            String orderDestinationPo = normalizeText(order.getDestinationPostOfficeCode());
            if (orderDestinationPo == null) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            HubPostOfficeMapping destinationMapping = hubPostOfficeMappingRepository
                    .findByTenantIdAndPostOfficeCode(tenantId, orderDestinationPo)
                    .orElseThrow(() -> new AppException(ErrorCode.BAG_POST_OFFICE_INVALID));
            Long resolvedDestinationHubId = destinationMapping.getHub() == null ? null : destinationMapping.getHub().getId();
            if (!Objects.equals(resolvedDestinationHubId, destinationHubId)) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            return;
        }
        throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
    }

    private Long resolveOriginHubIdByOrder(Long tenantId, Order order) {
        String originPostOfficeCode = normalizeText(order.getOriginPostOfficeCode());
        if (originPostOfficeCode == null) {
            throw new AppException(ErrorCode.BAG_HUB_INVALID);
        }
        HubPostOfficeMapping mapping = hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(tenantId, originPostOfficeCode)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_HUB_INVALID));
        return mapping.getHub() == null ? null : mapping.getHub().getId();
    }

    private BagDestinationTarget resolveDestinationTargetForOrder(Long tenantId, Order order) {
        String destinationPostOfficeCode = normalizeText(order.getDestinationPostOfficeCode());
        if (destinationPostOfficeCode == null) {
            throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
        }
        return hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(tenantId, destinationPostOfficeCode)
                .map(mapping -> new BagDestinationTarget(
                        BagDestinationType.HUB,
                        mapping.getHub() == null ? null : mapping.getHub().getId(),
                        destinationPostOfficeCode
                ))
                .orElse(new BagDestinationTarget(BagDestinationType.POST_OFFICE, null, destinationPostOfficeCode));
    }

    private List<Bag> findEditableBagsByTarget(Long tenantId, Long originHubId, BagDestinationTarget target) {
        if (target.destinationType == BagDestinationType.HUB) {
            return bagRepository.findByTenantIdAndOriginHubIdAndDestinationTypeAndDestinationHubIdAndStatus(
                    tenantId,
                    originHubId,
                    BagDestinationType.HUB,
                    target.destinationHubId,
                    BagStatus.CREATED
            );
        }
        return bagRepository.findByTenantIdAndOriginHubIdAndDestinationTypeAndDestinationPostOfficeCodeIgnoreCaseAndStatus(
                tenantId,
                originHubId,
                BagDestinationType.POST_OFFICE,
                target.destinationPostOfficeCode,
                BagStatus.CREATED
        );
    }

    private void recalculateBagMetrics(Bag bag, Long tenantId) {
        List<BagOrder> bagOrders = bagOrderRepository.findByBag_IdAndTenantId(bag.getId(), tenantId);
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        int totalOrders = 0;
        for (BagOrder bagOrder : bagOrders) {
            if (bagOrder.getOrder() == null) {
                continue;
            }
            totalWeight += safeDouble(bagOrder.getOrder().getTotalWeight());
            totalVolume += safeDouble(bagOrder.getOrder().getTotalVolume());
            totalOrders++;
        }
        bag.setCurrentWeight(totalWeight);
        bag.setCurrentVolume(totalVolume);
        bag.setCurrentOrders(totalOrders);
    }

    private boolean canFit(Bag bag, Order order, double extraWeight, double extraVolume, int extraOrders) {
        double nextWeight = safeDouble(bag.getCurrentWeight()) + safeDouble(order.getTotalWeight()) + extraWeight;
        double nextVolume = safeDouble(bag.getCurrentVolume()) + safeDouble(order.getTotalVolume()) + extraVolume;
        int nextOrders = safeInt(bag.getCurrentOrders()) + 1 + extraOrders;

        boolean withinWeight = nextWeight <= positiveOrDefault(bag.getMaxWeight(), DEFAULT_BAG_MAX_WEIGHT);
        boolean withinVolume = nextVolume <= positiveOrDefault(bag.getMaxVolume(), DEFAULT_BAG_MAX_VOLUME);
        boolean withinOrders = nextOrders <= positiveOrDefault(bag.getMaxOrders(), DEFAULT_BAG_MAX_ORDERS);
        return withinWeight && withinVolume && withinOrders;
    }

    private double remainingWeight(Bag bag) {
        return positiveOrDefault(bag.getMaxWeight(), DEFAULT_BAG_MAX_WEIGHT) - safeDouble(bag.getCurrentWeight());
    }

    private double remainingVolume(Bag bag) {
        return positiveOrDefault(bag.getMaxVolume(), DEFAULT_BAG_MAX_VOLUME) - safeDouble(bag.getCurrentVolume());
    }

    private int remainingOrders(Bag bag) {
        return positiveOrDefault(bag.getMaxOrders(), DEFAULT_BAG_MAX_ORDERS) - safeInt(bag.getCurrentOrders());
    }

    private List<String> normalizeOrderCodes(List<String> orderCodes) {
        if (orderCodes == null || orderCodes.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String orderCode : orderCodes) {
            String normalizedCode = normalizeText(orderCode);
            if (normalizedCode != null) {
                normalized.add(normalizedCode);
            }
        }
        return new ArrayList<>(normalized);
    }

    private String generateAutoBagCode(Long tenantId, Long originHubId, int index) {
        String code = String.format(
                Locale.ROOT,
                "AB-%d-%d-%s-%d",
                tenantId,
                originHubId,
                LocalDateTime.now().format(AUTO_BAG_SUFFIX_FORMATTER),
                index
        );
        if (!bagRepository.existsByTenantIdAndBagCodeIgnoreCase(tenantId, code)) {
            return code;
        }
        return code + "-" + (System.nanoTime() % 1000);
    }

    private double sizeScore(Order order, double maxWeight, double maxVolume) {
        double weightRatio = safeDouble(order.getTotalWeight()) / maxWeight;
        double volumeRatio = safeDouble(order.getTotalVolume()) / maxVolume;
        return Math.max(weightRatio, volumeRatio);
    }

    private BagFilterRequest normalizeFilterRequest(BagFilterRequest filterRequest) {
        if (filterRequest == null) {
            return BagFilterRequest.builder().build();
        }

        return BagFilterRequest.builder()
                .keyword(normalizeText(filterRequest.getKeyword()))
                .bagCode(normalizeText(filterRequest.getBagCode()))
                .originHubId(filterRequest.getOriginHubId())
                .destinationType(filterRequest.getDestinationType())
                .destinationHubId(filterRequest.getDestinationHubId())
                .destinationPostOfficeCode(normalizeText(filterRequest.getDestinationPostOfficeCode()))
                .vehicleId(filterRequest.getVehicleId())
                .status(filterRequest.getStatus())
                .build();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue.toUpperCase(Locale.ROOT);
    }

    private double safeDouble(Double value) {
        return value == null || value < 0 ? 0.0 : value;
    }

    private int safeInt(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }

    private double positiveOrDefault(Double value, double fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }

    private int positiveOrDefault(Integer value, int fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }

    private double normalizePositiveOrDefault(Double value, double fallback) {
        if (value == null) {
            return fallback;
        }
        if (value <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return value;
    }

    private int normalizePositiveOrDefault(Integer value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return value;
    }

    private double nullSafeDouble(Double value) {
        return value == null ? Double.MAX_VALUE : value;
    }

    private static class BagDestinationTarget {
        private final BagDestinationType destinationType;
        private final Long destinationHubId;
        private final String destinationPostOfficeCode;

        private BagDestinationTarget(BagDestinationType destinationType, Long destinationHubId, String destinationPostOfficeCode) {
            this.destinationType = destinationType;
            this.destinationHubId = destinationHubId;
            this.destinationPostOfficeCode = destinationPostOfficeCode;
        }
    }

    private static class AutoBagBin {
        private final double maxWeight;
        private final double maxVolume;
        private final int maxOrders;
        private final List<Order> orders = new ArrayList<>();
        private double totalWeight = 0.0;
        private double totalVolume = 0.0;

        private AutoBagBin(double maxWeight, double maxVolume, int maxOrders) {
            this.maxWeight = maxWeight;
            this.maxVolume = maxVolume;
            this.maxOrders = maxOrders;
        }

        private boolean canFit(Order order) {
            return (totalWeight + safe(order.getTotalWeight()) <= maxWeight)
                    && (totalVolume + safe(order.getTotalVolume()) <= maxVolume)
                    && (orders.size() + 1 <= maxOrders);
        }

        private void add(Order order) {
            orders.add(order);
            totalWeight += safe(order.getTotalWeight());
            totalVolume += safe(order.getTotalVolume());
        }

        private List<String> orderCodes() {
            return orders.stream().map(Order::getOrderCode).collect(Collectors.toList());
        }

        private static double safe(Double value) {
            return value == null || value < 0 ? 0.0 : value;
        }
    }
}
