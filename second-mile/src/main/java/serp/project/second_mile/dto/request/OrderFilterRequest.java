/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.OrderStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterRequest {
    private String keyword;
    private String orderCode;
    private String customerOrderCode;
    private Long targetHubId;
    private String originPostOfficeCode;
    private String destinationPostOfficeCode;
    private OrderStatus status;
    private Boolean assignedToBag;
}
