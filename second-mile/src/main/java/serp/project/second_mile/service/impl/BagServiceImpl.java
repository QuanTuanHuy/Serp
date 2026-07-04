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
import serp.project.second_mile.caller.TmsOrderClient;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.domain.BagOrder;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.domain.Route;
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
import serp.project.second_mile.dto.response.BagCapacitySettingsResponse;
import serp.project.second_mile.dto.response.BagResponse;
import serp.project.second_mile.dto.response.BagSuggestionResponse;
import serp.project.second_mile.dto.response.BaggingKpiResponse;
import serp.project.second_mile.dto.response.BaggingValidationItemResponse;
import serp.project.second_mile.dto.response.BaggingValidationResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.mapper.BagMapper;
import serp.project.second_mile.repository.BagOrderRepository;
import serp.project.second_mile.repository.BagRepository;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.repository.specification.BagSpecification;
import serp.project.second_mile.service.BagCapacityCalculator;
import serp.project.second_mile.service.BagCapacitySettingsService;
import serp.project.second_mile.service.BagService;
import serp.project.second_mile.service.TmsOrderTransitionPublisherService;
import serp.project.second_mile.service.dto.BagDestinationTarget;
import serp.project.second_mile.service.validator.BagValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static serp.project.second_mile.kernel.utils.CommonValueUtils.gramsToKilograms;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.idempotencyKey;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.normalizeCodes;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.normalizeText;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.nullSafeDouble;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.positiveOrDefault;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.safeDouble;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.safeInt;
import static serp.project.second_mile.service.BagCapacityCalculator.canFit;
import static serp.project.second_mile.service.BagCapacityCalculator.normalizePositiveOrDefault;
import static serp.project.second_mile.service.BagCapacityCalculator.orderWeightKg;
import static serp.project.second_mile.service.BagCapacityCalculator.planAutoBags;
import static serp.project.second_mile.service.BagCapacityCalculator.remainingOrders;
import static serp.project.second_mile.service.BagCapacityCalculator.remainingVolume;
import static serp.project.second_mile.service.BagCapacityCalculator.remainingWeight;

@Service
@RequiredArgsConstructor
public class BagServiceImpl implements BagService {
    private static final String TRANSITION_SOURCE = "SECOND_MILE";
    private static final DateTimeFormatter AUTO_BAG_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BagRepository bagRepository;
    private final BagOrderRepository bagOrderRepository;
    private final HubRepository hubRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;
    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;
    private final TmsOrderClient tmsOrderClient;
    private final TmsOrderTransitionPublisherService tmsOrderTransitionPublisherService;
    private final BagCapacitySettingsService bagCapacitySettingsService;
    private final BagValidator bagValidator;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BagResponse> getBags(int page, int size, BagFilterRequest filterRequest) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        BagFilterRequest normalizedFilterRequest = BagMapper.normalizeFilterRequest(filterRequest);

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
        bagValidator.validateTenantAccess(bag);
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

        bagValidator.validateBagLane(
                tenantId,
                request.getOriginHubId(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                request.getDestinationPostOfficeCode()
        );

        BagCapacitySettingsResponse capacitySettings = bagCapacitySettingsService.getSettingsForTenant(tenantId);
        Bag bag = BagMapper.toEntity(request);
        bag.setBagCode(normalizedBagCode);
        bag.setDestinationPostOfficeCode(normalizeText(request.getDestinationPostOfficeCode()));
        bag.setRouteId(null);
        bag.setVehicleId(null);
        bag.setStatus(BagStatus.CREATED);
        bag.setSealedAt(null);
        bag.setMaxWeight(normalizePositiveOrDefault(request.getMaxWeight(), capacitySettings.maxWeight()));
        bag.setMaxVolume(normalizePositiveOrDefault(request.getMaxVolume(), capacitySettings.maxVolume()));
        bag.setMaxOrders(normalizePositiveOrDefault(request.getMaxOrders(), capacitySettings.maxOrders()));
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
        bagValidator.validateTenantAccess(bag);
        bagValidator.validateBagEditable(bag);

        String normalizedBagCode = normalizeText(request.getBagCode());
        if (normalizedBagCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!bag.getBagCode().equalsIgnoreCase(normalizedBagCode)
                && bagRepository.existsByTenantIdAndBagCodeIgnoreCase(tenantId, normalizedBagCode)) {
            throw new AppException(ErrorCode.BAG_CODE_EXISTED);
        }

        bagValidator.validateBagLane(
                tenantId,
                request.getOriginHubId(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                request.getDestinationPostOfficeCode()
        );

        BagCapacitySettingsResponse capacitySettings = bagCapacitySettingsService.getSettingsForTenant(tenantId);
        BagMapper.mapForUpdate(request, bag);
        bag.setBagCode(normalizedBagCode);
        bag.setDestinationPostOfficeCode(normalizeText(request.getDestinationPostOfficeCode()));
        bag.setRouteId(null);
        bag.setVehicleId(null);
        bag.setStatus(BagStatus.CREATED);
        bag.setMaxWeight(normalizePositiveOrDefault(request.getMaxWeight(), capacitySettings.maxWeight()));
        bag.setMaxVolume(normalizePositiveOrDefault(request.getMaxVolume(), capacitySettings.maxVolume()));
        bag.setMaxOrders(normalizePositiveOrDefault(request.getMaxOrders(), capacitySettings.maxOrders()));
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
        bagValidator.validateTenantAccess(bag);
        bagValidator.validateBagEditable(bag);
        bagRepository.delete(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse addOrderToBag(Long bagId, AddBagOrderRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(bagId);
        bagValidator.validateTenantAccess(bag);
        bagValidator.validateBagEditable(bag);

        String normalizedOrderCode = normalizeText(request.getOrderCode());
        if (normalizedOrderCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        TmsOrderOperationView order = lookupOrdersByCodes(tenantId, List.of(normalizedOrderCode)).getFirst();
        if (bagOrderRepository.existsByBag_IdAndTmsOrderIdAndTenantId(bag.getId(), order.getId(), tenantId)) {
            throw new AppException(ErrorCode.BAG_ORDER_ALREADY_IN_BAG);
        }
        if (bagOrderRepository.existsByTmsOrderIdAndTenantId(order.getId(), tenantId)) {
            throw new AppException(ErrorCode.BAG_ORDER_ALREADY_ASSIGNED);
        }

        BagCapacitySettingsResponse capacitySettings = bagCapacitySettingsService.getSettingsForTenant(tenantId);
        bagValidator.validateOrderForBagAssignment(tenantId, bag, order, 0.0, 0.0, 0, capacitySettings);

        BagOrder bagOrder = BagMapper.toBagOrder(bag, order, tenantId);
        BagOrder savedBagOrder = bagOrderRepository.save(bagOrder);
        recalculateBagMetrics(bag, tenantId);
        bagRepository.save(bag);

        enqueueBagTransition(
                tenantId,
                idempotencyKey("bag-order", savedBagOrder.getId(), "bagged"),
                List.of(BagMapper.toTransitionItem(
                        order,
                        OrderStatus.BAGGED,
                        List.of(OrderStatus.INBOUND_AT_ORIGIN_HUB, OrderStatus.BAGGING_IN_PROGRESS),
                        "Order assigned to second-mile bag.",
                        buildBagContext(bag)
                ))
        );

        return toBagResponse(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse removeOrderFromBag(Long bagId, String orderCode) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(bagId);
        bagValidator.validateTenantAccess(bag);
        bagValidator.validateBagEditable(bag);

        String normalizedOrderCode = normalizeText(orderCode);
        if (normalizedOrderCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        BagOrder bagOrder = bagOrderRepository.findByBag_IdAndOrderCodeIgnoreCaseAndTenantId(
                        bag.getId(),
                        normalizedOrderCode,
                        tenantId
                )
                .orElseThrow(() -> new AppException(ErrorCode.BAG_ORDER_MAPPING_NOT_FOUND));

        bagOrderRepository.delete(bagOrder);
        recalculateBagMetrics(bag, tenantId);
        bagRepository.save(bag);

        enqueueBagTransition(
                tenantId,
                idempotencyKey("bag-order", bagOrder.getId(), "inbound-origin-hub"),
                List.of(BagMapper.toTransitionItem(
                        bagOrder,
                        OrderStatus.INBOUND_AT_ORIGIN_HUB,
                        List.of(OrderStatus.BAGGED, OrderStatus.INBOUND_AT_ORIGIN_HUB),
                        "Order removed from second-mile bag.",
                        buildBagContext(bag)
                ))
        );

        return toBagResponse(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse sealBag(Long bagId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(bagId);
        bagValidator.validateTenantAccess(bag);
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
        for (BagOrder bagOrder : bagOrders) {
            bagOrder.setLastKnownStatus(OrderStatus.BAG_SEALED.name());
        }
        bagOrderRepository.saveAll(bagOrders);

        enqueueBagTransition(
                tenantId,
                idempotencyKey("bag", savedBag.getId(), "seal", savedBag.getSealedAt()),
                bagOrders.stream()
                        .map(item -> BagMapper.toTransitionItem(
                                item,
                                OrderStatus.BAG_SEALED,
                                List.of(OrderStatus.BAGGED),
                                "Second-mile bag sealed.",
                                buildBagContext(savedBag)
                        ))
                        .toList()
        );

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
        bagValidator.validateTenantAccess(bag);

        if (bag.getStatus() != BagStatus.SEALED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }

        bag.setStatus(BagStatus.CREATED);
        bag.setSealedAt(null);
        bag.setNote(reason);
        Bag savedBag = bagRepository.save(bag);

        List<BagOrder> bagOrders = bagOrderRepository.findByBag_IdAndTenantId(savedBag.getId(), tenantId);
        for (BagOrder bagOrder : bagOrders) {
            bagOrder.setLastKnownStatus(OrderStatus.BAGGED.name());
        }
        bagOrderRepository.saveAll(bagOrders);

        enqueueBagTransition(
                tenantId,
                idempotencyKey("bag", savedBag.getId(), "reopen", LocalDateTime.now()),
                bagOrders.stream()
                        .map(item -> BagMapper.toTransitionItem(
                                item,
                                OrderStatus.BAGGED,
                                List.of(OrderStatus.BAG_SEALED),
                                "Second-mile bag reopened. Reason: " + reason,
                                buildBagContext(savedBag)
                        ))
                        .toList()
        );

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

        TmsOrderOperationView order = lookupOrdersByCodes(tenantId, List.of(normalizedOrderCode)).getFirst();
        if (bagOrderRepository.existsByTmsOrderIdAndTenantId(order.getId(), tenantId)) {
            return List.of();
        }
        if (!OrderStatus.isReadyForBagging(order.getStatus())) {
            return List.of();
        }

        Long resolvedOriginHubId = originHubId != null ? originHubId : bagValidator.resolveOriginHubIdByOrder(tenantId, order);
        BagDestinationTarget destinationTarget = resolveDestinationTargetForOrder(tenantId, order, resolvedOriginHubId);
        List<Bag> candidates = findEditableBagsByTarget(tenantId, resolvedOriginHubId, destinationTarget);
        BagCapacitySettingsResponse capacitySettings = bagCapacitySettingsService.getSettingsForTenant(tenantId);

        return candidates.stream()
                .filter(candidate -> canFit(candidate, order, 0.0, 0.0, 0, capacitySettings))
                .map(candidate -> new BagSuggestionResponse(
                        candidate.getId(),
                        candidate.getBagCode(),
                        remainingWeight(candidate, capacitySettings),
                        remainingVolume(candidate, capacitySettings),
                        remainingOrders(candidate, capacitySettings)
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
        bagValidator.validateTenantAccess(bag);
        bagValidator.validateBagEditable(bag);

        List<String> normalizedOrderCodes = normalizeCodes(request.getOrderCodes());
        if (normalizedOrderCodes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Map<String, TmsOrderOperationView> orderByCode = lookupOrderMapByCodes(tenantId, normalizedOrderCodes);
        List<BaggingValidationItemResponse> items = new ArrayList<>();
        double extraWeight = 0.0;
        double extraVolume = 0.0;
        int extraOrders = 0;
        int accepted = 0;
        BagCapacitySettingsResponse capacitySettings = bagCapacitySettingsService.getSettingsForTenant(tenantId);

        for (String orderCode : normalizedOrderCodes) {
            TmsOrderOperationView order = orderByCode.get(orderCode);
            if (order == null) {
                items.add(new BaggingValidationItemResponse(orderCode, false, "Order not found."));
                continue;
            }
            if (bagOrderRepository.existsByTmsOrderIdAndTenantId(order.getId(), tenantId)) {
                items.add(new BaggingValidationItemResponse(orderCode, false, "Order already assigned to another bag."));
                continue;
            }
            try {
                bagValidator.validateOrderForBagAssignment(
                        tenantId,
                        bag,
                        order,
                        extraWeight,
                        extraVolume,
                        extraOrders,
                        capacitySettings
                );
                extraWeight += orderWeightKg(order);
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
        bagValidator.validateBagLane(
                tenantId,
                request.getOriginHubId(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                request.getDestinationPostOfficeCode()
        );

        List<String> normalizedOrderCodes = normalizeCodes(request.getOrderCodes());
        if (normalizedOrderCodes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<TmsOrderOperationView> orders = lookupOrdersByCodes(tenantId, normalizedOrderCodes);
        for (TmsOrderOperationView order : orders) {
            if (bagOrderRepository.existsByTmsOrderIdAndTenantId(order.getId(), tenantId)) {
                throw new AppException(ErrorCode.BAG_ORDER_ALREADY_ASSIGNED);
            }
            if (!OrderStatus.isReadyForBagging(order.getStatus())) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Only inbound orders can be auto-bagged.");
            }
            bagValidator.validateOrderOriginMatchesHub(tenantId, order, request.getOriginHubId());
            bagValidator.validateOrderDestinationMatchesTarget(tenantId, request.getOriginHubId(), request.getDestinationType(), request.getDestinationHubId(),
                    request.getDestinationPostOfficeCode(), order);
        }

        BagCapacitySettingsResponse capacitySettings = bagCapacitySettingsService.getSettingsForTenant(tenantId);

        List<BagCapacityCalculator.AutoBagBin> bins = planAutoBags(orders, capacitySettings);

        boolean execute = Boolean.TRUE.equals(request.getExecute());
        List<AutoBaggingPlanItemResponse> planItems = new ArrayList<>();
        if (!execute) {
            int index = 1;
            for (BagCapacityCalculator.AutoBagBin bin : bins) {
                planItems.add(new AutoBaggingPlanItemResponse(
                        "PLAN-" + index++,
                        bin.orderCodes(),
                        bin.totalWeight(),
                        bin.totalVolume()
                ));
            }
            return new AutoBaggingPlanResponse(false, bins.size(), planItems);
        }

        List<TmsOrderStatusTransitionRequest.Item> transitionItems = new ArrayList<>();
        int index = 1;
        for (BagCapacityCalculator.AutoBagBin bin : bins) {
            String bagCode = generateAutoBagCode(tenantId, request.getOriginHubId(), index);
            Bag bag = Bag.builder()
                    .bagCode(bagCode)
                    .originHubId(request.getOriginHubId())
                    .destinationType(request.getDestinationType())
                    .destinationHubId(request.getDestinationHubId())
                    .destinationPostOfficeCode(normalizeText(request.getDestinationPostOfficeCode()))
                    .maxWeight(capacitySettings.maxWeight())
                    .maxVolume(capacitySettings.maxVolume())
                    .maxOrders(capacitySettings.maxOrders())
                    .currentWeight(bin.totalWeight())
                    .currentVolume(bin.totalVolume())
                    .currentOrders(bin.orders().size())
                    .status(BagStatus.CREATED)
                    .tenantId(tenantId)
                    .build();
            Bag savedBag = bagRepository.save(bag);
            TmsOrderStatusTransitionRequest.Context context = buildBagContext(savedBag);

            for (TmsOrderOperationView order : bin.orders()) {
                BagOrder bagOrder = bagOrderRepository.save(BagMapper.toBagOrder(savedBag, order, tenantId));
                transitionItems.add(BagMapper.toTransitionItem(
                        order,
                        OrderStatus.BAGGED,
                        List.of(OrderStatus.INBOUND_AT_ORIGIN_HUB, OrderStatus.BAGGING_IN_PROGRESS),
                        "Order auto-assigned to second-mile bag.",
                        context
                ));
                bagOrder.setLastKnownStatus(OrderStatus.BAGGED.name());
                bagOrderRepository.save(bagOrder);
            }
            planItems.add(new AutoBaggingPlanItemResponse(
                    savedBag.getBagCode(),
                    bin.orderCodes(),
                    bin.totalWeight(),
                    bin.totalVolume()
            ));
            index++;
        }

        enqueueBagTransition(
                tenantId,
                idempotencyKey("auto-bag", tenantId, request.getOriginHubId(), LocalDateTime.now()),
                transitionItems
        );
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
        BagCapacitySettingsResponse capacitySettings = bagCapacitySettingsService.getSettingsForTenant(tenantId);
        for (Bag bag : sealedBags) {
            sumWeightRate += safeDouble(bag.getCurrentWeight()) / positiveOrDefault(
                    bag.getMaxWeight(),
                    capacitySettings.maxWeight()
            );
            sumVolumeRate += safeDouble(bag.getCurrentVolume()) / positiveOrDefault(
                    bag.getMaxVolume(),
                    capacitySettings.maxVolume()
            );
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

    private BagDestinationTarget resolveDestinationTargetForOrder(
            Long tenantId,
            TmsOrderOperationView order,
            Long originHubId
    ) {
        String destinationPostOfficeCode = normalizeText(order.getDestinationPostOfficeCode());
        if (destinationPostOfficeCode == null) {
            throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
        }
        return hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(tenantId, destinationPostOfficeCode)
                .map(mapping -> {
                    Long destinationHubId = mapping.getHub() == null ? null : mapping.getHub().getId();
                    if (destinationHubId == null || Objects.equals(originHubId, destinationHubId)) {
                        return new BagDestinationTarget(BagDestinationType.POST_OFFICE, null, destinationPostOfficeCode);
                    }
                    return new BagDestinationTarget(BagDestinationType.HUB, destinationHubId, destinationPostOfficeCode);
                })
                .orElse(new BagDestinationTarget(BagDestinationType.POST_OFFICE, null, destinationPostOfficeCode));
    }

    private List<Bag> findEditableBagsByTarget(Long tenantId, Long originHubId, BagDestinationTarget target) {
        if (target.destinationType() == BagDestinationType.HUB) {
            return bagRepository.findByTenantIdAndOriginHubIdAndDestinationTypeAndDestinationHubIdAndStatus(
                    tenantId,
                    originHubId,
                    BagDestinationType.HUB,
                    target.destinationHubId(),
                    BagStatus.CREATED
            );
        }
        return bagRepository.findByTenantIdAndOriginHubIdAndDestinationTypeAndDestinationPostOfficeCodeIgnoreCaseAndStatus(
                tenantId,
                originHubId,
                BagDestinationType.POST_OFFICE,
                target.destinationPostOfficeCode(),
                BagStatus.CREATED
        );
    }

    private void recalculateBagMetrics(Bag bag, Long tenantId) {
        List<BagOrder> bagOrders = bagOrderRepository.findByBag_IdAndTenantId(bag.getId(), tenantId);
        double totalWeightKg = 0.0;
        double totalVolume = 0.0;
        int totalOrders = 0;
        for (BagOrder bagOrder : bagOrders) {
            totalWeightKg += gramsToKilograms(safeDouble(bagOrder.getTotalWeightSnapshot()));
            totalVolume += safeDouble(bagOrder.getTotalVolumeSnapshot());
            totalOrders++;
        }
        bag.setCurrentWeight(totalWeightKg);
        bag.setCurrentVolume(totalVolume);
        bag.setCurrentOrders(totalOrders);
    }

    private List<TmsOrderOperationView> lookupOrdersByCodes(Long tenantId, List<String> orderCodes) {
        Map<String, TmsOrderOperationView> orderByCode = lookupOrderMapByCodes(tenantId, orderCodes);
        List<TmsOrderOperationView> orders = new ArrayList<>();
        for (String orderCode : orderCodes) {
            TmsOrderOperationView order = orderByCode.get(orderCode);
            if (order == null) {
                throw new AppException(ErrorCode.BAG_ORDER_NOT_FOUND);
            }
            orders.add(order);
        }
        return orders;
    }

    private Map<String, TmsOrderOperationView> lookupOrderMapByCodes(Long tenantId, List<String> orderCodes) {
        Map<String, TmsOrderOperationView> orderByCode = new LinkedHashMap<>();
        List<TmsOrderOperationView> orders = tmsOrderClient.lookupByCodes(orderCodes);
        for (TmsOrderOperationView order : orders) {
            bagValidator.validateTmsOrderTenant(tenantId, order);
            String normalizedOrderCode = normalizeText(order.getOrderCode());
            if (normalizedOrderCode != null) {
                orderByCode.put(normalizedOrderCode, order);
            }
        }
        return orderByCode;
    }

    private void enqueueBagTransition(
            Long tenantId,
            String idempotencyKey,
            List<TmsOrderStatusTransitionRequest.Item> items
    ) {
        if (items == null || items.isEmpty()) {
            return;
        }
        tmsOrderTransitionPublisherService.publish(TmsOrderStatusTransitionRequest.builder()
                .source(TRANSITION_SOURCE)
                .idempotencyKey(idempotencyKey)
                .items(items)
                .build(), tenantId);
    }

    private TmsOrderStatusTransitionRequest.Context buildBagContext(Bag bag) {
        Hub hub = bag.getOriginHubId() == null
                ? null
                : hubRepository.findById(bag.getOriginHubId()).orElse(null);
        Vehicle vehicle = bag.getVehicleId() == null
                ? null
                : vehicleRepository.findById(bag.getVehicleId()).orElse(null);
        Route route = bag.getRouteId() == null
                ? null
                : routeRepository.findById(bag.getRouteId()).orElse(null);
        return BagMapper.toBagContext(bag, hub, vehicle, route);
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

}
