/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.HandoverManifestStatus;

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

    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("route_id")
    private Long routeId;

    @JsonProperty("planned_departure_at")
    private LocalDateTime plannedDepartureAt;

    @JsonProperty("planned_arrival_at")
    private LocalDateTime plannedArrivalAt;

    @JsonProperty("origin_post_office_latitude")
    private Double originPostOfficeLatitude;

    @JsonProperty("origin_post_office_longitude")
    private Double originPostOfficeLongitude;

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
