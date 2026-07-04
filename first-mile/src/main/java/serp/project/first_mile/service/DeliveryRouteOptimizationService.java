/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.domain.DeliveryManifestOrder;

import java.util.List;

public interface DeliveryRouteOptimizationService {
    List<DeliveryManifestOrder> optimizeRoute(String postOfficeCode, List<DeliveryManifestOrder> orders);
}
