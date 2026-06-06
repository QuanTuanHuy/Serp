/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.tms_order.enums.OrderStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterRequest {
    private String keyword;
    private String orderCode;
    private String customerOrderCode;
    private String senderPhone;
    private String receiverPhone;
    private String originPostOfficeCode;
    private String destinationPostOfficeCode;
    private OrderStatus status;
    private Boolean isConfirm;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime pickupFrom;
    private LocalDateTime pickupTo;
}
