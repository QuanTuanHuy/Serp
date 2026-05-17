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
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.Order;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AddBagOrderRequest;
import serp.project.second_mile.dto.request.BagFilterRequest;
import serp.project.second_mile.dto.request.CreateBagRequest;
import serp.project.second_mile.dto.request.UpdateBagRequest;
import serp.project.second_mile.dto.response.BagResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.mapper.BagMapper;
import serp.project.second_mile.repository.BagOrderRepository;
import serp.project.second_mile.repository.BagRepository;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.OrderRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.repository.specification.BagSpecification;
import serp.project.second_mile.service.BagService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BagServiceImpl implements BagService {
    private final BagRepository bagRepository;
    private final BagOrderRepository bagOrderRepository;
    private final HubRepository hubRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BagResponse> getBags(int page, int size, BagFilterRequest filterRequest) {
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
        Bag bag = getBagOrThrow(id);
        validateTenantAccess(bag);
        return toBagResponse(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse createBag(CreateBagRequest request) {
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
        bag.setStatus(request.getStatus() == null ? BagStatus.CREATED : request.getStatus());
        bag.setTenantId(tenantId);

        Bag savedBag = bagRepository.save(bag);
        return toBagResponse(savedBag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse updateBag(Long id, UpdateBagRequest request) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(id);
        validateTenantAccess(bag);

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
        bag.setTenantId(tenantId);

        Bag updatedBag = bagRepository.save(bag);
        return toBagResponse(updatedBag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBag(Long id) {
        Bag bag = getBagOrThrow(id);
        validateTenantAccess(bag);
        bagRepository.delete(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse addOrderToBag(Long bagId, AddBagOrderRequest request) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(bagId);
        validateTenantAccess(bag);
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

        BagOrder bagOrder = BagOrder.builder()
                .bag(bag)
                .order(order)
                .tenantId(tenantId)
                .build();
        bagOrderRepository.save(bagOrder);

        return toBagResponse(bag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagResponse removeOrderFromBag(Long bagId, String orderCode) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Bag bag = getBagOrThrow(bagId);
        validateTenantAccess(bag);
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
        return toBagResponse(bag);
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

    private void validateBagCanAcceptOrders(Bag bag) {
        if (bag.getStatus() == BagStatus.ARRIVED || bag.getStatus() == BagStatus.CANCELLED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }
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
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
