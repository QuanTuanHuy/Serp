/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.ConfirmHandoverInboundRequest;
import serp.project.second_mile.dto.request.CreateHandoverManifestRequest;
import serp.project.second_mile.dto.request.HandoverManifestFilterRequest;
import serp.project.second_mile.dto.response.HandoverManifestResponse;
import serp.project.second_mile.kafka.event.HandoverManifestSyncEvent;

public interface HandoverManifestService {
    HandoverManifestResponse createManifest(CreateHandoverManifestRequest request);

    PageResponse<HandoverManifestResponse> listManifests(
            int page,
            int size,
            HandoverManifestFilterRequest filterRequest
    );

    HandoverManifestResponse confirmOutbound(Long manifestId);

    HandoverManifestResponse confirmInbound(Long manifestId, ConfirmHandoverInboundRequest request);

    HandoverManifestResponse driverCheckinStart(Long manifestId);

    HandoverManifestResponse driverCheckinEnd(Long manifestId);

    HandoverManifestResponse getManifest(Long manifestId);

    void applyOutboundSync(HandoverManifestSyncEvent event);
}
