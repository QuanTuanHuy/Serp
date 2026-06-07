/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.first_mile.domain.DeliveryManifestOrder;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.kernel.utils.DeliveryRouteOptimizationUtils;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.service.DeliveryRouteOptimizationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryRouteOptimizationServiceImpl implements DeliveryRouteOptimizationService {

    private final PostOfficeRepository postOfficeRepository;
    private final AuthUtils authUtils;

    @Override
    public List<DeliveryManifestOrder> optimizeRoute(
            String postOfficeCode, List<DeliveryManifestOrder> orders) {

        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        PostOffice postOffice = postOfficeRepository.findByCodeIgnoreCaseAndTenantId(postOfficeCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));

        Double depotLat = postOffice.getLocationLatitude();
        Double depotLng = postOffice.getLocationLongitude();
        if (depotLat == null || depotLng == null) {
            throw new AppException(ErrorCode.POST_OFFICE_LOCATION_NOT_SET);
        }

        // Filter orders with valid coordinates
        List<DeliveryManifestOrder> withCoords = orders.stream()
                .filter(o -> o.getReceiverLat() != null && o.getReceiverLng() != null)
                .collect(Collectors.toCollection(ArrayList::new));

        // Orders without coordinates → placed at end
        List<DeliveryManifestOrder> noCoords = orders.stream()
                .filter(o -> o.getReceiverLat() == null || o.getReceiverLng() == null)
                .toList();

        List<DeliveryManifestOrder> optimized = DeliveryRouteOptimizationUtils.optimize(
                depotLat, depotLng, withCoords);

        // Merge: optimized route + orders without coords at end
        List<DeliveryManifestOrder> result = new ArrayList<>(optimized);
        result.addAll(noCoords);

        // Assign 1-based sequence numbers
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setSequence(i + 1);
        }
        return result;
    }
}
