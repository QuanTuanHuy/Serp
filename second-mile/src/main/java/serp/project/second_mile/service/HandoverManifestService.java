/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.ConfirmHandoverInboundRequest;
import serp.project.second_mile.dto.request.CreateHandoverManifestRequest;
import serp.project.second_mile.dto.request.DriverHandoverCheckinRequest;
import serp.project.second_mile.dto.request.HandoverManifestFilterRequest;
import serp.project.second_mile.dto.response.HandoverManifestResponse;
import serp.project.second_mile.kafka.event.HandoverManifestSyncEvent;
import org.springframework.web.multipart.MultipartFile;

public interface HandoverManifestService {
    HandoverManifestResponse createManifest(CreateHandoverManifestRequest request);

    PageResponse<HandoverManifestResponse> listManifests(
            int page,
            int size,
            HandoverManifestFilterRequest filterRequest
    );

    HandoverManifestResponse confirmOutbound(Long manifestId);

    HandoverManifestResponse confirmInbound(Long manifestId, ConfirmHandoverInboundRequest request);

    HandoverManifestResponse driverCheckinStart(
            Long manifestId,
            DriverHandoverCheckinRequest request,
            MultipartFile photo
    );

    HandoverManifestResponse driverCheckinEnd(
            Long manifestId,
            DriverHandoverCheckinRequest request,
            MultipartFile photo
    );

    HandoverManifestResponse getManifest(Long manifestId);

    void validateOutboundSync(HandoverManifestSyncEvent event);

    void applyOutboundSync(HandoverManifestSyncEvent event);
}
