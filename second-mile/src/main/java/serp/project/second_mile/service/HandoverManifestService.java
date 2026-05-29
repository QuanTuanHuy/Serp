/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.request.ConfirmHandoverInboundRequest;
import serp.project.second_mile.dto.request.CreateHandoverManifestRequest;
import serp.project.second_mile.dto.response.HandoverManifestResponse;

public interface HandoverManifestService {
    HandoverManifestResponse createManifest(CreateHandoverManifestRequest request);

    HandoverManifestResponse confirmOutbound(Long manifestId);

    HandoverManifestResponse confirmInbound(Long manifestId, ConfirmHandoverInboundRequest request);

    HandoverManifestResponse getManifest(Long manifestId);
}
