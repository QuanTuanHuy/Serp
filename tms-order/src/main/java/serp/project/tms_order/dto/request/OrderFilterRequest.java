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
import java.util.List;

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
    private List<String> originPostOfficeCodes;
    private String destinationPostOfficeCode;
    private OrderStatus status;
    private List<OrderStatus> statuses;
    private Boolean isConfirm;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime pickupFrom;
    private LocalDateTime pickupTo;
}
