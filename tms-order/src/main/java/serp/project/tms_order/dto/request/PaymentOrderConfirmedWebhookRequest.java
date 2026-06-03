/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderConfirmedWebhookRequest {
    @JsonProperty("app_trans_id")
    @JsonAlias("appTransId")
    @NotBlank
    private String appTransId;

    @JsonProperty("order_code")
    @JsonAlias("orderCode")
    @NotBlank
    private String orderCode;

    @JsonProperty("tenant_id")
    @JsonAlias("tenantId")
    @NotNull
    private Long tenantId;

    private Long amount;

    @JsonProperty("paid_at")
    @JsonAlias("paidAt")
    private LocalDateTime paidAt;

    @JsonProperty("gateway_code")
    @JsonAlias("gatewayCode")
    private String gatewayCode;

    @JsonProperty("gateway_transaction_id")
    @JsonAlias("gatewayTransactionId")
    private String gatewayTransactionId;
}
