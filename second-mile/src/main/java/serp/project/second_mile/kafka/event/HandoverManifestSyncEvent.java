/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.second_mile.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.HandoverManifestStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverManifestSyncEvent {
    @JsonProperty("event_type")
    private HandoverManifestSyncEventType eventType;

    private HandoverManifestSyncOrigin origin;

    @JsonProperty("tenant_id")
    private Long tenantId;

    @JsonProperty("manifest_code")
    private String manifestCode;

    @JsonProperty("origin_post_office_code")
    private String originPostOfficeCode;

    @JsonProperty("target_hub_id")
    private Long targetHubId;

    private HandoverManifestStatus status;

    @JsonProperty("dispatched_at")
    private LocalDateTime dispatchedAt;

    @JsonProperty("inbound_confirmed_at")
    private LocalDateTime inboundConfirmedAt;

    @JsonProperty("order_codes")
    private List<String> orderCodes;

    @JsonProperty("scanned_order_codes")
    private List<String> scannedOrderCodes;

    @JsonProperty("seal_code")
    private String sealCode;

    private String note;
}
