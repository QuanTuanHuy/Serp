/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.request.UpdateBagCapacitySettingsRequest;
import serp.project.second_mile.dto.response.BagCapacitySettingsResponse;

public interface BagCapacitySettingsService {
    BagCapacitySettingsResponse getCurrentSettings();

    BagCapacitySettingsResponse getSettingsForTenant(Long tenantId);

    BagCapacitySettingsResponse updateCurrentSettings(UpdateBagCapacitySettingsRequest request);
}
