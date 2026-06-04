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
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.CreateRouteRequest;
import serp.project.second_mile.dto.request.RouteFilterRequest;
import serp.project.second_mile.dto.request.UpdateRouteRequest;
import serp.project.second_mile.dto.response.RouteResponse;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.mapper.RouteMapper;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.repository.specification.RouteSpecification;
import serp.project.second_mile.service.RouteService;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final RouteRepository routeRepository;
    private final HubRepository hubRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;
    private final VehicleRepository vehicleRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RouteResponse> getRoutes(int page, int size, RouteFilterRequest filterRequest) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        RouteFilterRequest normalizedFilterRequest = normalizeFilterRequest(filterRequest);

        Page<Route> routePage = routeRepository.findAll(
                RouteSpecification.byFilter(tenantId, normalizedFilterRequest),
                pageable
        );

        Page<RouteResponse> mappedPage = routePage.map(RouteMapper::toResponse);
        return PageResponse.<RouteResponse>builder()
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
    public RouteResponse getRouteById(Long id) {
        Route route = getRouteOrThrow(id);
        validateTenantAccess(route);
        return RouteMapper.toResponse(route);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RouteResponse createRoute(CreateRouteRequest request) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        String normalizedRouteCode = normalizeText(request.getRouteCode());
        if (normalizedRouteCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (routeRepository.existsByTenantIdAndRouteCodeIgnoreCase(tenantId, normalizedRouteCode)) {
            throw new AppException(ErrorCode.ROUTE_CODE_EXISTED);
        }

        validateRouteDefinition(
                tenantId,
                request.getOriginType(),
                request.getOriginHubId(),
                request.getOriginPostOfficeCode(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                request.getDestinationPostOfficeCode(),
                request.getVehicleId()
        );

        Route route = RouteMapper.toEntity(request);
        route.setRouteCode(normalizedRouteCode);
        route.setRouteName(normalizeText(request.getRouteName()));
        route.setDestinationPostOfficeCode(normalizeText(request.getDestinationPostOfficeCode()));
        applyNormalizedEndpoints(route);
        route.setStatus(request.getStatus() == null ? RouteStatus.ACTIVE : request.getStatus());
        route.setTenantId(tenantId);

        Route savedRoute = routeRepository.save(route);
        return RouteMapper.toResponse(savedRoute);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RouteResponse updateRoute(Long id, UpdateRouteRequest request) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Route route = getRouteOrThrow(id);
        validateTenantAccess(route);

        String normalizedRouteCode = normalizeText(request.getRouteCode());
        String normalizedRouteName = normalizeText(request.getRouteName());
        if (normalizedRouteCode == null || normalizedRouteName == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!route.getRouteCode().equalsIgnoreCase(normalizedRouteCode)
                && routeRepository.existsByTenantIdAndRouteCodeIgnoreCase(tenantId, normalizedRouteCode)) {
            throw new AppException(ErrorCode.ROUTE_CODE_EXISTED);
        }

        validateRouteDefinition(
                tenantId,
                request.getOriginType(),
                request.getOriginHubId(),
                request.getOriginPostOfficeCode(),
                request.getDestinationType(),
                request.getDestinationHubId(),
                request.getDestinationPostOfficeCode(),
                request.getVehicleId()
        );

        RouteMapper.mapForUpdate(request, route);
        route.setRouteCode(normalizedRouteCode);
        route.setRouteName(normalizedRouteName);
        route.setDestinationPostOfficeCode(normalizeText(request.getDestinationPostOfficeCode()));
        applyNormalizedEndpoints(route);
        route.setTenantId(tenantId);

        Route updatedRoute = routeRepository.save(route);
        return RouteMapper.toResponse(updatedRoute);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long id) {
        Route route = getRouteOrThrow(id);
        validateTenantAccess(route);
        routeRepository.delete(route);
    }

    private Route getRouteOrThrow(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND));
    }

    private void validateTenantAccess(Route route) {
        Long currentTenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        if (route.getTenantId() == null || !route.getTenantId().equals(currentTenantId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateRouteDefinition(
            Long tenantId,
            RouteEndpointType originType,
            Long originHubId,
            String originPostOfficeCode,
            RouteDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode,
            Long vehicleId
    ) {
        RouteEndpointType normalizedOriginType = originType == null ? RouteEndpointType.HUB : originType;
        if (destinationType == null) {
            throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
        }

        Long operatingHubId = resolveAndValidateOperatingHubId(
                tenantId,
                normalizedOriginType,
                originHubId,
                originPostOfficeCode,
                destinationType,
                destinationHubId
        );

        if (destinationType == RouteDestinationType.HUB) {
            if (destinationHubId == null) {
                throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
            }
            Hub destinationHub = hubRepository.findById(destinationHubId)
                    .orElseThrow(() -> new AppException(ErrorCode.ROUTE_HUB_INVALID));
            if (!tenantId.equals(destinationHub.getTenantId())) {
                throw new AppException(ErrorCode.ROUTE_HUB_INVALID);
            }
            if (normalizedOriginType == RouteEndpointType.HUB && originHubId.equals(destinationHubId)) {
                throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
            }
            if (normalizedOriginType == RouteEndpointType.POST_OFFICE && vehicleId == null) {
                throw new AppException(
                        ErrorCode.ROUTE_VEHICLE_INVALID,
                        "Post office to hub routes require a dedicated vehicle."
                );
            }
            if (vehicleId != null) {
                validateVehicle(tenantId, vehicleId, operatingHubId);
            }
            return;
        }

        if (destinationType == RouteDestinationType.POST_OFFICE) {
            if (normalizedOriginType != RouteEndpointType.HUB) {
                throw new AppException(
                        ErrorCode.ROUTE_DEFINITION_INVALID,
                        "Post office routes can only target a hub."
                );
            }
            if (vehicleId == null) {
                throw new AppException(
                        ErrorCode.ROUTE_VEHICLE_INVALID,
                        "Post office collection routes require a dedicated vehicle."
                );
            }
            String normalizedPostOfficeCode = normalizeText(destinationPostOfficeCode);
            if (normalizedPostOfficeCode == null) {
                throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
            }
            HubPostOfficeMapping mapping = hubPostOfficeMappingRepository
                    .findByTenantIdAndPostOfficeCode(tenantId, normalizedPostOfficeCode)
                    .orElseThrow(() -> new AppException(ErrorCode.ROUTE_POST_OFFICE_INVALID));
            if (mapping.getHub() == null || !originHubId.equals(mapping.getHub().getId())) {
                throw new AppException(ErrorCode.ROUTE_POST_OFFICE_INVALID);
            }
            validateVehicle(tenantId, vehicleId, operatingHubId);
            return;
        }

        throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
    }

    private Long resolveAndValidateOperatingHubId(
            Long tenantId,
            RouteEndpointType originType,
            Long originHubId,
            String originPostOfficeCode,
            RouteDestinationType destinationType,
            Long destinationHubId
    ) {
        if (originType == RouteEndpointType.HUB) {
            if (originHubId == null) {
                throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
            }
            Hub originHub = hubRepository.findById(originHubId)
                    .orElseThrow(() -> new AppException(ErrorCode.ROUTE_HUB_INVALID));
            if (!tenantId.equals(originHub.getTenantId())) {
                throw new AppException(ErrorCode.ROUTE_HUB_INVALID);
            }
            return originHubId;
        }

        if (originType == RouteEndpointType.POST_OFFICE) {
            if (destinationType != RouteDestinationType.HUB || destinationHubId == null) {
                throw new AppException(
                        ErrorCode.ROUTE_DEFINITION_INVALID,
                        "Post office routes must target a hub."
                );
            }
            String normalizedPostOfficeCode = normalizeText(originPostOfficeCode);
            if (normalizedPostOfficeCode == null) {
                throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
            }
            HubPostOfficeMapping mapping = hubPostOfficeMappingRepository
                    .findByTenantIdAndPostOfficeCode(tenantId, normalizedPostOfficeCode)
                    .orElseThrow(() -> new AppException(ErrorCode.ROUTE_POST_OFFICE_INVALID));
            if (mapping.getHub() == null || !destinationHubId.equals(mapping.getHub().getId())) {
                throw new AppException(ErrorCode.ROUTE_POST_OFFICE_INVALID);
            }
            return destinationHubId;
        }

        throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
    }

    private void applyNormalizedEndpoints(Route route) {
        RouteEndpointType originType = route.getOriginType() == null ? RouteEndpointType.HUB : route.getOriginType();
        route.setOriginType(originType);
        if (originType == RouteEndpointType.HUB) {
            route.setOriginPostOfficeCode(null);
        } else {
            route.setOriginHubId(null);
            route.setOriginPostOfficeCode(normalizeText(route.getOriginPostOfficeCode()));
        }

        if (route.getDestinationType() == RouteDestinationType.HUB) {
            route.setDestinationPostOfficeCode(null);
        } else {
            route.setDestinationHubId(null);
            route.setDestinationPostOfficeCode(normalizeText(route.getDestinationPostOfficeCode()));
        }
    }

    private void validateVehicle(Long tenantId, Long vehicleId, Long originHubId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_VEHICLE_INVALID));
        if (!tenantId.equals(vehicle.getTenantId()) || vehicle.getStatus() != VehicleStatus.ACTIVE) {
            throw new AppException(ErrorCode.ROUTE_VEHICLE_INVALID);
        }
        if (!originHubId.equals(vehicle.getHubId())) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    "Route vehicle must belong to the origin hub."
            );
        }
        secondMileAccessUtils.ensureActiveDriverStaffOrThrow(vehicle.getAssignedStaffId());
    }

    private RouteFilterRequest normalizeFilterRequest(RouteFilterRequest filterRequest) {
        if (filterRequest == null) {
            return RouteFilterRequest.builder().build();
        }
        return RouteFilterRequest.builder()
                .keyword(normalizeText(filterRequest.getKeyword()))
                .routeCode(normalizeText(filterRequest.getRouteCode()))
                .originType(filterRequest.getOriginType())
                .originHubId(filterRequest.getOriginHubId())
                .originPostOfficeCode(normalizeText(filterRequest.getOriginPostOfficeCode()))
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
