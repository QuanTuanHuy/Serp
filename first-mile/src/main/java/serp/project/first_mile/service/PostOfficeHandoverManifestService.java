/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.message.HandoverManifestSyncEvent;
import serp.project.first_mile.dto.request.CreatePostOfficeHandoverManifestRequest;
import serp.project.first_mile.dto.request.DispatchPostOfficeHandoverManifestRequest;
import serp.project.first_mile.dto.request.ScanOutHandoverOrderRequest;
import serp.project.first_mile.dto.response.PostOfficeHandoverManifestResponse;
import serp.project.first_mile.enums.HandoverManifestStatus;

public interface PostOfficeHandoverManifestService {
    PageResponse<PostOfficeHandoverManifestResponse> listManifests(
            int page,
            int size,
            Long postOfficeId,
            Long targetHubId,
            HandoverManifestStatus status
    );

    PostOfficeHandoverManifestResponse getManifest(Long manifestId);

    PostOfficeHandoverManifestResponse createManifest(CreatePostOfficeHandoverManifestRequest request);

    PostOfficeHandoverManifestResponse scanOrderOut(Long manifestId, ScanOutHandoverOrderRequest request);

    PostOfficeHandoverManifestResponse dispatchManifest(
            Long manifestId,
            DispatchPostOfficeHandoverManifestRequest request
    );

    PostOfficeHandoverManifestResponse cancelManifest(Long manifestId);

    void applyInboundSync(HandoverManifestSyncEvent event);
}
