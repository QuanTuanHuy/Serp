/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmDeliveryRequest {
    @JsonProperty("proof_photo_url")
    private String proofPhotoUrl;

    @JsonProperty("cod_collected")
    private Long codCollected;

    @JsonProperty("shipping_fee_collected")
    private Long shippingFeeCollected;

    @JsonProperty("note")
    private String note;

    @JsonProperty("delivered_at")
    private LocalDateTime deliveredAt;
}
