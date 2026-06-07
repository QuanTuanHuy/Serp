/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.response.DeliveryManifestResponse;

public interface CodCollectionService {
    DeliveryManifestResponse getFinancialSummary(Long manifestId, Long tenantId);
}
