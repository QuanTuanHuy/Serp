/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.first_mile.enums.HandoverManifestStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PostOfficeHandoverManifestResponse(
        Long id,
        @JsonProperty("manifest_code") String manifestCode,
        @JsonProperty("origin_post_office_id") Long originPostOfficeId,
        @JsonProperty("origin_post_office_code") String originPostOfficeCode,
        @JsonProperty("target_hub_id") Long targetHubId,
        HandoverManifestStatus status,
        @JsonProperty("total_orders") int totalOrders,
        @JsonProperty("scanned_out_orders") int scannedOutOrders,
        @JsonProperty("scanned_in_orders") int scannedInOrders,
        @JsonProperty("dispatched_at") LocalDateTime dispatchedAt,
        @JsonProperty("inbound_confirmed_at") LocalDateTime inboundConfirmedAt,
        @JsonProperty("seal_code") String sealCode,
        String note,
        List<PostOfficeHandoverManifestOrderResponse> orders,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}
