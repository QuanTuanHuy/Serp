/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AutoPlanBagDistributionRequest;
import serp.project.second_mile.dto.request.BagDistributionManifestFilterRequest;
import serp.project.second_mile.dto.request.ConfirmBagDistributionInboundRequest;
import serp.project.second_mile.dto.request.CreateBagDistributionManifestRequest;
import serp.project.second_mile.dto.request.DriverHandoverCheckinRequest;
import serp.project.second_mile.dto.response.BagDistributionManifestResponse;
import serp.project.second_mile.dto.response.BagDistributionPlanResponse;

public interface BagDistributionManifestService {
    PageResponse<BagDistributionManifestResponse> listManifests(
            int page,
            int size,
            BagDistributionManifestFilterRequest filterRequest
    );

    BagDistributionManifestResponse getManifest(Long manifestId);

    BagDistributionManifestResponse createManifest(CreateBagDistributionManifestRequest request);

    BagDistributionPlanResponse autoPlan(AutoPlanBagDistributionRequest request);

    BagDistributionManifestResponse confirmOutbound(Long manifestId);

    BagDistributionManifestResponse confirmInbound(Long manifestId, ConfirmBagDistributionInboundRequest request);

    BagDistributionManifestResponse driverCheckinStart(
            Long manifestId,
            DriverHandoverCheckinRequest request,
            MultipartFile photo
    );

    BagDistributionManifestResponse driverCheckinEnd(
            Long manifestId,
            DriverHandoverCheckinRequest request,
            MultipartFile photo
    );

    BagDistributionManifestResponse cancel(Long manifestId);
}
