/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.OrderStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundOrderResponse {
    private Long orderId;
    private String orderCode;
    private OrderStatus status;
    private String destinationPostOfficeCode;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddressDetail;
    private Long codAmount;
    private Long totalShippingFee;
    private String feePayer;
}
